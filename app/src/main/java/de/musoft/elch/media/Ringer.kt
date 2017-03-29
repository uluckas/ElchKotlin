package de.musoft.elch.media

import android.content.Context
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.PowerManager
import de.musoft.elch.app.R
import de.musoft.elch.extensions.powerManager
import de.musoft.elch.extensions.uriForRessource

/**
 * Created by luckas on 15.09.16.
 */

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

fun ring(context: Context) {
    val uri = context.uriForRessource(R.raw.ring)
    val mediaPlayer = MediaPlayer()
    val pm = context.powerManager
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
