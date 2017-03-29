package de.musoft.elch.broadcastreceivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.PowerManager
import de.musoft.elch.app.R

private const val volume = 1.0f

/**
 * Call prepareAsync on mediaPlayer, catch all exceptions and report success
 *
 * @param mediaPlayer The mediaplayer object
 *
 * @return true if successfull
 */
private fun tryPrepareAsync(mediaPlayer: MediaPlayer): Boolean {
    try {
        mediaPlayer.prepareAsync()
        return true
    } catch (e: Throwable) {
        return false
    }
}

class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val uri = Uri.parse("android.resource://" + context.applicationContext.packageName + "/" + R.raw.ring)
        val mediaPlayer = MediaPlayer()
        val pm = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        val wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "AlarmHandover")

        mediaPlayer.setOnCompletionListener { it.release() }
        mediaPlayer.setWakeMode(context, PowerManager.PARTIAL_WAKE_LOCK)
        mediaPlayer.setDataSource(context, uri)
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_ALARM)
        mediaPlayer.setVolume(volume, volume)
        mediaPlayer.setOnPreparedListener {
            try {
                mediaPlayer.start()
            } finally {
                // No more need to hold the WakeLock
                // While playing, the MediaPlayer holds it's own WakeLock
                wakeLock.release()
            }
        }
        if (tryPrepareAsync(mediaPlayer)) {
            // If prepareAsync succeeded, we can be sure, the onPreparedListener will be called
            // So proceede to acquire the WakeLock, it will be released in the onPreparedListener
            wakeLock.acquire()
        }
    }

}