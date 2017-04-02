package de.musoft.elch.media

import android.content.Context
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.PowerManager
import de.musoft.elch.app.R
import de.musoft.elch.extensions.powerManager
import de.musoft.elch.extensions.uriForRessource
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch
import java.util.logging.Level
import java.util.logging.Logger
import kotlin.coroutines.experimental.suspendCoroutine

/**
 * Created by luckas on 15.09.16.
 */

private const val volume = 1.0f

suspend fun MediaPlayer.prepareSuspending(): Unit =
        suspendCoroutine<Unit> { cont ->
            try {
                setOnPreparedListener {
                    cont.resume(Unit)
                }
                prepareAsync()
            } catch (t: Throwable) {
                cont.resumeWithException(t)
            }
        }

fun ring(context: Context) =
        launch(UI) {
            val pm = context.powerManager
            val wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "AlarmHandover")
            wakeLock.acquire()
            try {
                val uri = context.uriForRessource(R.raw.ring)
                val mediaPlayer = MediaPlayer()

                mediaPlayer.setOnCompletionListener { it.release() }
                mediaPlayer.setWakeMode(context, PowerManager.PARTIAL_WAKE_LOCK)
                mediaPlayer.setDataSource(context, uri)
                mediaPlayer.setAudioStreamType(AudioManager.STREAM_ALARM)
                mediaPlayer.setVolume(volume, volume)
                mediaPlayer.prepareSuspending()
                mediaPlayer.start()
            } catch (t: Throwable) {
                val logger = Logger.getLogger(this::class.java.name)
                logger.log(Level.SEVERE, "Could not play ring tone", t)
            } finally {
                wakeLock.release()
            }
        }

