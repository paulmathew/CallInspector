package com.example.callinspector.diagnostics.domain.repository

import android.os.SystemClock
import com.example.callinspector.diagnostics.domain.model.NetworkHealth
import com.example.callinspector.diagnostics.domain.model.TestStage
import com.example.callinspector.utils.loge
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.Socket
import java.net.URL
import javax.inject.Inject
import kotlin.math.abs

class RealNetworkRepository @Inject constructor() : NetworkRepository {

    private val probeHost = "clients3.google.com"
    private val probePort = 443
    private val downloadUrl = "https://proof.ovh.net/files/10Mb.dat"

    override fun runFullNetworkDiagnostic(): Flow<NetworkHealth> = flow {
        var state = NetworkHealth(stage = TestStage.PINGING)
        emit(state)

        // --- PHASE 1: DNS & PING ---
        var resolvedAddress: InetAddress? = null
        try {
            resolvedAddress = InetAddress.getByName(probeHost)
        } catch (e: Exception) {
            state = state.copy(stage = TestStage.COMPLETE, packetLossPercent = 100)
            emit(state)
            return@flow
        }

        val latencies = mutableListOf<Long>()
        var failures = 0

        for (i in 1..5) {
            val start = SystemClock.elapsedRealtime()
            var success = false
            try {
                Socket().use { socket ->
                    socket.connect(InetSocketAddress(resolvedAddress, probePort), 2000)
                }
                success = true
            } catch (e: Exception) {
                success = tryHttpHeadProbe()
            }

            val duration = SystemClock.elapsedRealtime() - start
            if (success) latencies.add(duration) else { failures++; latencies.add(2000L) }

            val currentAvg = if (latencies.isNotEmpty()) latencies.average().toLong() else 0
            state = state.copy(latencyMs = currentAvg)
            emit(state)
            delay(100)
        }

        val loss = ((failures.toDouble() / 5) * 100).toInt()
        var totalDiff = 0L
        for (k in 0 until latencies.size - 1) { totalDiff += abs(latencies[k] - latencies[k+1]) }
        val jitter = if (latencies.size > 1) totalDiff / (latencies.size - 1) else 0

        state = state.copy(
            jitterMs = jitter,
            packetLossPercent = loss,
            stage = TestStage.DOWNLOADING
        )
        emit(state)

        // --- PHASE 2: DYNAMIC DOWNLOAD SPEED ---
        if (loss < 100) {
            // CHANGED: We now capture the RETURNED state from the function
            state = runDownloadSpeedTest(state)
        }

        // --- COMPLETE ---
        // Now 'state' holds the final speed (e.g., 15.4), so this emission is correct
        emit(state.copy(stage = TestStage.COMPLETE))

    }.flowOn(Dispatchers.IO)

    private fun tryHttpHeadProbe(): Boolean {
        return try {
            val url = URL("https://$probeHost/generate_204")
            val conn = url.openConnection() as HttpURLConnection
            conn.connectTimeout = 2000; conn.readTimeout = 2000; conn.requestMethod = "HEAD"
            val code = conn.responseCode; conn.disconnect()
            code == 204
        } catch (e: Exception) { false }
    }

    // CHANGED: Returns NetworkHealth so the parent can update its reference
    private suspend fun FlowCollector<NetworkHealth>.runDownloadSpeedTest(initialState: NetworkHealth): NetworkHealth {
        var state = initialState
        try {
            val url = URL(downloadUrl)
            val connection = url.openConnection()
            connection.connectTimeout = 3000
            connection.readTimeout = 3000

            val stream: InputStream = connection.getInputStream()
            val buffer = ByteArray(1024 * 4)

            var totalBytesRead = 0L
            val startTime = SystemClock.elapsedRealtime()
            val maxDuration = 3000L

            var bytesInThisInterval = 0L
            var lastUpdateTimestamp = startTime

            while (SystemClock.elapsedRealtime() - startTime < maxDuration) {
                val read = stream.read(buffer)
                if (read == -1) break

                totalBytesRead += read
                bytesInThisInterval += read

                val now = SystemClock.elapsedRealtime()
                val timeSinceLastUpdate = now - lastUpdateTimestamp

                if (timeSinceLastUpdate >= 200) {
                    val instantBits = bytesInThisInterval * 8
                    val instantMbits = instantBits / 1_000_000.0
                    val instantSecs = timeSinceLastUpdate / 1000.0
                    val instantSpeed = instantMbits / instantSecs

                    state = state.copy(downloadSpeedMbps = String.format("%.2f", instantSpeed).toDouble())
                    emit(state)

                    lastUpdateTimestamp = now
                    bytesInThisInterval = 0
                }
            }
            stream.close()

            val totalTimeSec = (SystemClock.elapsedRealtime() - startTime) / 1000.0
            if (totalTimeSec > 0.1) {
                val totalBits = totalBytesRead * 8
                val totalMbits = totalBits / 1_000_000.0
                val avgSpeed = totalMbits / totalTimeSec

                // Final calculation
                state = state.copy(downloadSpeedMbps = String.format("%.2f", avgSpeed).toDouble())
                emit(state)
            }

        } catch (e: Exception) {
            loge("NetworkTest", "Download Error: ${e.message}")
            state = state.copy(downloadSpeedMbps = 0.0)
            emit(state)
        }

        // CHANGED: Return the final state
        return state
    }
}