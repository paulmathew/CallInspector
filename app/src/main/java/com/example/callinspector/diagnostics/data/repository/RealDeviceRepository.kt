package com.example.callinspector.diagnostics.data.repository

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.Sensor
import android.hardware.SensorManager
import android.os.BatteryManager
import android.os.Build
import android.os.Environment
import android.os.StatFs
import android.app.ActivityManager
import com.example.callinspector.diagnostics.domain.model.DeviceHealth
import com.example.callinspector.diagnostics.domain.repository.DeviceRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject
import kotlin.math.pow

class RealDeviceRepository @Inject constructor(
    @ApplicationContext private val context: Context
) : DeviceRepository {

    override suspend fun getDeviceDetails(): DeviceHealth {
        return DeviceHealth(
            brand = Build.MANUFACTURER.replaceFirstChar { it.uppercase() },
            model = Build.MODEL,
            androidVersion = Build.VERSION.RELEASE,
            coreCount = Runtime.getRuntime().availableProcessors(),
            ramTotalGb = getTotalRam(),
            ramAvailableGb = getAvailableRam(),
            storageTotalGb = getTotalStorage(),
            storageFreeGb = getFreeStorage(),
            batteryLevel = getBatteryLevel(),
            isCharging = isDeviceCharging(),
            hasGyroscope = hasSensor(Sensor.TYPE_GYROSCOPE),
            hasAccelerometer = hasSensor(Sensor.TYPE_ACCELEROMETER),
            hasMagnetometer = hasSensor(Sensor.TYPE_MAGNETIC_FIELD)
        )
    }

    // --- Helpers ---

    private fun getTotalRam(): Double {
        val actManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val memInfo = ActivityManager.MemoryInfo()
        actManager.getMemoryInfo(memInfo)
        return bytesToGb(memInfo.totalMem)
    }

    private fun getAvailableRam(): Double {
        val actManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val memInfo = ActivityManager.MemoryInfo()
        actManager.getMemoryInfo(memInfo)
        return bytesToGb(memInfo.availMem)
    }

    private fun getTotalStorage(): Double {
        val path = Environment.getDataDirectory()
        val stat = StatFs(path.path)
        val blockSize = stat.blockSizeLong
        val totalBlocks = stat.blockCountLong
        return bytesToGb(totalBlocks * blockSize)
    }

    private fun getFreeStorage(): Double {
        val path = Environment.getDataDirectory()
        val stat = StatFs(path.path)
        val blockSize = stat.blockSizeLong
        val availableBlocks = stat.availableBlocksLong
        return bytesToGb(availableBlocks * blockSize)
    }

    private fun getBatteryLevel(): Int {
        val batteryStatus: Intent? = IntentFilter(Intent.ACTION_BATTERY_CHANGED).let { ifilter ->
            context.registerReceiver(null, ifilter)
        }
        val level: Int = batteryStatus?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val scale: Int = batteryStatus?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
        return if (level != -1 && scale != -1) {
            (level * 100 / scale.toFloat()).toInt()
        } else {
            0
        }
    }

    private fun isDeviceCharging(): Boolean {
        val batteryStatus: Intent? = IntentFilter(Intent.ACTION_BATTERY_CHANGED).let { ifilter ->
            context.registerReceiver(null, ifilter)
        }
        val status: Int = batteryStatus?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
        return status == BatteryManager.BATTERY_STATUS_CHARGING ||
                status == BatteryManager.BATTERY_STATUS_FULL
    }

    private fun hasSensor(type: Int): Boolean {
        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        return sensorManager.getDefaultSensor(type) != null
    }

    private fun bytesToGb(bytes: Long): Double {
        val gb = bytes.toDouble() / (1024.0.pow(3.0))
        return String.format("%.2f", gb).toDouble()
    }
}