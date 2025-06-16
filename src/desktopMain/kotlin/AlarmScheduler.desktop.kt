package de.musoft.elch.kmp.app // Match commonMain package

import kotlinx.coroutines.*
import java.io.BufferedInputStream
import javax.sound.sampled.AudioSystem
import javax.sound.sampled.Clip
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.resource // For loading common resources

private val alarmScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
private var alarmJob: Job? = null

@OptIn(ExperimentalResourceApi::class)
internal actual fun platformSetExactAlarm(alarmTimeMillis: Long) {
    alarmJob?.cancel() // Cancel previous alarm if any

    val currentTimeMonotonic = kotlin.time.TimeSource.Monotonic.markNow().elapsedNow().inWholeMilliseconds
    var delayMillis = alarmTimeMillis - currentTimeMonotonic

    if (delayMillis < 0) delayMillis = 0

    alarmJob = alarmScope.launch {
        delay(delayMillis)
        if (isActive) { // Check if job was cancelled during delay
            try {
                // Load sound resource from commonMain/composeResources/raw/ring.wav
                // The resource() function returns a Resource object, get an InputStream from it.
                // This requires the new resource library (org.jetbrains.compose.resources).
                val resourceBytes = resource("raw/ring.wav").readBytes()
                val inputStream = BufferedInputStream(resourceBytes.inputStream())

                AudioSystem.getAudioInputStream(inputStream).use { audioInputStream ->
                    val clip = AudioSystem.getClip()
                    clip.open(audioInputStream)
                    clip.start()
                    // Wait for the clip to finish playing.
                    // Add a listener or sleep for the duration of the clip.
                    // For simplicity, if clip.microsecondLength is available:
                    // delay(clip.microsecondLength / 1000)
                    // Or use a listener:
                    clip.addLineListener { event ->
                        if (event.type == javax.sound.sampled.LineEvent.Type.STOP) {
                            clip.close()
                        }
                    }
                    // If the clip doesn't close itself, ensure it's closed after playing.
                    // This might require keeping the job alive until sound finishes.
                    // For a simple fire-and-forget sound, this might be okay.
                    // More robust playback would handle clip lifecycle carefully.
                }
            } catch (e: Exception) {
                println("Error playing sound on desktop: ${e.message}")
                e.printStackTrace()
                // Fallback: simple print if sound fails
                println("Desktop Alarm: Time is up!")
            }
        }
    }
}

internal actual fun platformCancelAlarm() {
    alarmJob?.cancel()
    alarmJob = null
}
