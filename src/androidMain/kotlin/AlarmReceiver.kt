package de.musoft.elch.kmp.app

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.PowerManager
// Assuming R.raw.ring will be available. User needs to add ring.wav to src/main/res/raw.

class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        // Ensure this action matches the one used in getPendingIntent
        if (intent.action == "de.musoft.elch.kmp.app.ALARM_FIRED") {
            val soundUri = Uri.parse(
                "android.resource://" + context.packageName + "/" + R.raw.ring // If R class is generated in kmp.app package
                // If R class is in a different package, adjust path or find programmatically.
                // For KMP, R class might be de.musoft.elch.kmp.app.R
            )

            val mediaPlayer = MediaPlayer()
            val pm = context.getSystemService(Context.POWER_SERVICE) as PowerManager
            // Use applicationContext for WakeLock to avoid leaking Activity context if any
            val wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "ElchAppKMP::AlarmRingWakeLock")

            mediaPlayer.setOnCompletionListener {
                it.release()
                if (wakeLock.isHeld) {
                    wakeLock.release()
                }
            }
            mediaPlayer.setOnErrorListener { mp, what, extra ->
                if (wakeLock.isHeld) {
                    wakeLock.release()
                }
                true // True if the method handled the error, false if it didn't.
            }

            try {
                // Acquire WakeLock before preparing MediaPlayer to ensure CPU stays on.
                // Timeout for wakelock acquire, e.g., 10 seconds.
                wakeLock.acquire(10*1000L)

                mediaPlayer.setDataSource(context, soundUri)
                mediaPlayer.setAudioStreamType(AudioManager.STREAM_ALARM) // Use STREAM_ALARM
                mediaPlayer.isLooping = false // Ensure it doesn't loop by default
                mediaPlayer.setVolume(1.0f, 1.0f) // Max volume

                mediaPlayer.setOnPreparedListener {
                    it.start()
                    // MediaPlayer holds its own WakeLock while playing.
                    // Release our initial WakeLock once MediaPlayer is prepared and started.
                    if (wakeLock.isHeld) {
                        wakeLock.release()
                    }
                }
                mediaPlayer.prepareAsync()
            } catch (e: Exception) {
                e.printStackTrace()
                if (wakeLock.isHeld) {
                    wakeLock.release()
                }
                mediaPlayer.release() // Clean up MediaPlayer on error
            }
        }
    }
}
