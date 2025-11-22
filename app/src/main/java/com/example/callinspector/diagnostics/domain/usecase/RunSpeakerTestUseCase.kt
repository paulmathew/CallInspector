package com.example.callinspector.diagnostics.domain.usecase

import android.content.Context
import android.media.AudioManager
import android.media.MediaPlayer
import com.example.callinspector.R
import com.example.callinspector.diagnostics.domain.model.SpeakerTestResult
import kotlinx.coroutines.delay
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import kotlin.coroutines.resume

class RunSpeakerTestUseCase @Inject constructor(
    private val context: Context
) {
    suspend operator fun invoke(): SpeakerTestResult = suspendCancellableCoroutine { continuation ->

        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
        val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)


        val mediaPlayer = MediaPlayer.create(context, R.raw.speaker_test_tone)

        if (mediaPlayer == null) {
            continuation.resume(
                SpeakerTestResult(
                    playbackSucceeded = false,
                    currentVolume = currentVolume,
                    maxVolume = maxVolume
                )
            )
            return@suspendCancellableCoroutine
        }
        mediaPlayer.setOnCompletionListener {
            mediaPlayer.release()
            continuation.resume(
                SpeakerTestResult(
                    playbackSucceeded = true,
                    currentVolume = currentVolume,
                    maxVolume = maxVolume
                )
            )
        }

        mediaPlayer.setOnErrorListener { mp, what, extra ->
            mp.release()
            continuation.resume(
                SpeakerTestResult(
                    playbackSucceeded = false,
                    currentVolume = currentVolume,
                    maxVolume = maxVolume
                )
            )
            true
        }
        try {
            mediaPlayer.start()
        } catch (e: Exception) {
            mediaPlayer.release()
            continuation.resume(
                SpeakerTestResult(
                    playbackSucceeded = false,
                    currentVolume = currentVolume,
                    maxVolume = maxVolume
                )
            )
        }

        // Auto-release on coroutine cancel
        continuation.invokeOnCancellation {
            mediaPlayer.release()
        }
    }
}