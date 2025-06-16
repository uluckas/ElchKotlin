package de.musoft.elch.kmp.app // Match commonMain package

import platform.UserNotifications.*
import platform.Foundation.*
import kotlin.time.Duration.Companion.milliseconds

// For converting monotonic time to a delay for UNTimeIntervalNotificationTrigger
// Requires TimeSource.Monotonic to be consistent with how alarmTimeMillis is calculated.
// alarmTimeMillis is an absolute point in monotonic time.
// We need the duration FROM NOW until that point.
// This actual fun is called when the timer is *started* or *resumed*.
// The alarmTimeMillis is the target future point in monotonic time.
private fun currentMonotonicTimeEpochMilliseconds(): Long {
    // This is tricky. Kotlin/Native doesn't directly expose a monotonic clock
    // that aligns perfectly with Swift's CACurrentMediaTime() or similar
    // in a way that's easily convertible to epoch for UNNotificationTrigger.
    // However, alarmTimeMillis IS already from TimeSource.Monotonic.markNow().elapsedNow().inWholeMilliseconds
    // So, if platformSetExactAlarm receives alarmTimeMillis as this target monotonic timestamp,
    // we need to calculate the DELAY from the current monotonic time.

    // Let's assume alarmTimeMillis is the absolute future point on the monotonic clock.
    // We calculate the delay: delay = alarmTimeMillis - currentMonotonicTimeMs()
    // This means CommonAlarmTimer should pass the absolute future monotonic time.
    // CommonAlarmTimer's startTimer(newAlarmTime) passes newAlarmTimeMS which is currentMonotonicTimeMs() + remainingTimeMS.
    // This is correct.
    return NSDate().timeIntervalSince1970() * 1000 // This is wall clock, not monotonic.
    // A better way for delay calculation:
    // The 'alarmTimeMillis' IS the target on the monotonic scale.
    // The 'platformSetExactAlarm' needs to know 'how long from now'.
    // So, the 'CommonAlarmTimer' should calculate 'delayDuration = alarmTimeMillis - currentMonotonicTimeMs()'
    // and pass this 'delayDuration' to platformSetExactAlarm.
    //
    // Let's adjust the expect function signature to take a delay:
    // expect fun platformSetExactAlarm(delayMillis: Long)
    // Then this function is simpler.
    // For now, I will stick to the current expect signature and do the calculation here,
    // assuming alarmTimeMillis is the future point on the monotonic clock.
    // This requires a way to get current monotonic time here.
    // Kotlin/Native TimeSource.Monotonic.markNow().elapsedNow().inWholeMilliseconds should work.
    // This will be used to calculate the interval.
}


private const val ELCH_ALARM_NOTIFICATION_ID = "ElchAlarmNotification"

internal actual fun platformSetExactAlarm(alarmTimeMillis: Long) {
    val notificationCenter = UNUserNotificationCenter.currentNotificationCenter()

    // Calculate delay: targetMonotonicTime - currentMonotonicTime
    val currentTimeMonotonic = kotlin.time.TimeSource.Monotonic.markNow().elapsedNow().inWholeMilliseconds
    var delayMillis = alarmTimeMillis - currentTimeMonotonic

    if (delayMillis < 0) delayMillis = 0 // Cannot schedule in the past

    val content = UNMutableNotificationContent().apply {
        setTitle("Elch Alarm") // Directly use NSString
        setBody("Time is up!")   // Directly use NSString
        // Sound: Default sound for now. User needs to add 'ring.caf' (or similar) to iOS bundle
        // and then specify: sound = UNNotificationSound.soundNamed("ring.caf")
        setSound(UNNotificationSound.defaultSound())
    }

    // Ensure minimum trigger interval if delayMillis is very small (e.g., 1 second)
    // as iOS might not fire for extremely short intervals.
    val triggerInterval = if (delayMillis < 1000 && delayMillis > 0) 1.0 else delayMillis / 1000.0


    if (triggerInterval <= 0) { // If delay is zero or negative, fire immediately or don't schedule.
        // For simplicity, we won't fire an immediate notification here if time is already up.
        // The app's UI should reflect this.
        // If you wanted an immediate notification:
        // val request = UNNotificationRequest.requestWithIdentifier(NSUUID().UUIDString(), content, null)
        // notificationCenter.addNotificationRequest(request, null)
        return
    }

    val trigger = UNTimeIntervalNotificationTrigger.triggerWithTimeInterval(triggerInterval, repeats = false)
    val request = UNNotificationRequest.requestWithIdentifier(ELCH_ALARM_NOTIFICATION_ID, content, trigger)

    notificationCenter.addNotificationRequest(request) { error ->
        if (error != null) {
            println("Error scheduling iOS notification: ${error.localizedDescription}")
        }
    }
}

internal actual fun platformCancelAlarm() {
    val notificationCenter = UNUserNotificationCenter.currentNotificationCenter()
    // Cancel a specific pending local notification
    notificationCenter.removePendingNotificationRequestsWithIdentifiers(listOf(ELCH_ALARM_NOTIFICATION_ID))
}
