package de.musoft.elch.kmp.app

import com.russhwolf.settings.MapSettings
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.* // For TestScope, runTest, etc.
import kotlin.test.*
import kotlin.time.Duration.Companion.seconds

// Mock platform alarm functions for testing CommonAlarmTimer in isolation
// These would typically go in a test utility file or directly in the test file if small.
// For this subtask, they are defined here for simplicity.
// They need to be in the same package as the expect declarations.
internal actual fun platformSetExactAlarm(alarmTimeMillis: Long) {
    println("Test: platformSetExactAlarm called with $alarmTimeMillis")
    // No actual scheduling, just log or track calls if needed for specific tests
}

internal actual fun platformCancelAlarm() {
    println("Test: platformCancelAlarm called")
    // No actual cancellation, just log or track
}

// Actual for settings context (not directly used by these tests if MapSettings is injected,
// but needed for CommonAlarmTimer to compile if it still expects it via createSettings() indirectly)
// However, TimerViewModel uses createSettings(), CommonAlarmTimer takes Settings via constructor.
// So this might not be strictly needed for these specific tests if we DI MapSettings.
// For safety, if createSettings() is called anywhere:
internal actual fun createSettings() = MapSettings() // Provide a test version
internal actual val applicationContextForSettings: Any = Any() // Dummy for Android context expect


@OptIn(ExperimentalCoroutinesApi::class)
class CommonAlarmTimerTests {

    private lateinit var testSettings: MapSettings
    private lateinit var testScope: TestScope // Use TestScope for better control over coroutines
    private lateinit var alarmTimer: CommonAlarmTimer

    private val defaultSpielzeitMinutes = 8L
    private val defaultSpielzeitMillis = defaultSpielzeitMinutes * 60 * 1000

    @BeforeTest
    fun setup() {
        testSettings = MapSettings()
        // StandardTestDispatcher is good for controlling time via advanceTimeBy
        testScope = TestScope(StandardTestDispatcher())
        alarmTimer = CommonAlarmTimer(settings = testSettings, coroutineScope = testScope)
    }

    @AfterTest
    fun tearDown() {
        // Cleanup might be needed if testScope doesn't handle everything, e.g. cancel jobs
        // testScope.cleanupTestCoroutines() // Handled by TestScope itself typically
    }

    @Test
    fun testInitialState() = testScope.runTest {
        var remainingSeconds = -1L
        alarmTimer.addSecondsListener { remainingSeconds = it }
        alarmTimer.triggerInitialUpdate() // Ensure initial state is propagated

        assertEquals(defaultSpielzeitMinutes * 60, remainingSeconds, "Initial remaining seconds should be default spielzeit")
        assertFalse(alarmTimer.isTimerRunning(), "Timer should not be running initially")
        assertEquals(defaultSpielzeitMillis, testSettings.getLong("REMAINING_TIME_MS", 0L), "Initial remaining time in settings")
    }

    @Test
    fun testStartTimer() = testScope.runTest {
        alarmTimer.startOrPauseTimer() // Start
        assertTrue(alarmTimer.isTimerRunning(), "Timer should be running after start")
        assertTrue(testSettings.getBoolean("TIMER_RUNNING", false), "TIMER_RUNNING in settings should be true")
        // alarmTimeMS should be set to current monotonic time + remaining
        // This is hard to test precisely without knowing the exact monotonic start,
        // but we can check it's non-zero.
        assertTrue(testSettings.getLong("ALARM_TIME_MS", 0L) > 0, "ALARM_TIME_MS should be set in settings")
    }

    @Test
    fun testPauseTimer() = testScope.runTest {
        alarmTimer.startOrPauseTimer() // Start
        advanceTimeBy(2.seconds.inWholeMilliseconds) // Let some time pass
        alarmTimer.startOrPauseTimer() // Pause

        assertFalse(alarmTimer.isTimerRunning(), "Timer should be paused")
        assertFalse(testSettings.getBoolean("TIMER_RUNNING", false), "TIMER_RUNNING in settings should be false after pause")
        // remainingTimeMS in settings should be less than default, but not zero
        val remainingInSettings = testSettings.getLong("REMAINING_TIME_MS", 0L)
        assertTrue(remainingInSettings < defaultSpielzeitMillis && remainingInSettings > 0, "Paused remaining time should be updated in settings. Was $remainingInSettings")
    }

    @Test
    fun testResetTimer() = testScope.runTest {
        alarmTimer.startOrPauseTimer() // Start
        advanceTimeBy(5.seconds.inWholeMilliseconds)
        alarmTimer.startOrPauseTimer() // Pause
        alarmTimer.resetTimer()

        var latestSeconds = 0L
        // Add listener *after* reset to get the state post-reset
        alarmTimer.addSecondsListener { latestSeconds = it }
        alarmTimer.triggerInitialUpdate()


        assertFalse(alarmTimer.isTimerRunning(), "Timer should not be running after reset")
        assertFalse(testSettings.getBoolean("TIMER_RUNNING", false), "TIMER_RUNNING in settings should be false after reset")
        assertEquals(defaultSpielzeitMillis, testSettings.getLong("REMAINING_TIME_MS", 0L), "Remaining time in settings should be reset")
        assertEquals(defaultSpielzeitMinutes * 60, latestSeconds, "Remaining seconds display should be reset")
    }

    @Test
    fun testTimeUpdates() = testScope.runTest {
        val receivedTimes = mutableListOf<Long>()
        alarmTimer.addSecondsListener { remainingSeconds ->
            receivedTimes.add(remainingSeconds)
        }
        alarmTimer.triggerInitialUpdate() // Get initial time: 480

        alarmTimer.startOrPauseTimer() // Start timer

        // advanceTimeBy triggers the execution of tasks scheduled up to that point.
        // Our flow emits, then delays 1s.
        // Initial: 480
        advanceTimeBy(1000) // Tick 1: 479 (Listener for 480 already called by triggerInitialUpdate)
        advanceTimeBy(1000) // Tick 2: 478
        advanceTimeBy(1000) // Tick 3: 477

        // Expected: [480, 479, 478, 477]
        // Check size and content
        assertTrue(receivedTimes.size >= 4, "Should have received initial + 3 ticks. Got ${receivedTimes.size}, values: ${receivedTimes.joinToString()}")
        assertEquals(defaultSpielzeitMinutes * 60, receivedTimes[0], "Initial time incorrect.")
        // Due to TestScope's virtual time, subsequent ticks happen right after the delay.
        assertEquals(defaultSpielzeitMinutes * 60 - 1, receivedTimes[1], "Tick 1 incorrect.")
        assertEquals(defaultSpielzeitMinutes * 60 - 2, receivedTimes[2], "Tick 2 incorrect.")
        assertEquals(defaultSpielzeitMinutes * 60 - 3, receivedTimes[3], "Tick 3 incorrect.")

        alarmTimer.startOrPauseTimer() // Pause
    }

    @Test
    fun testTimerExpires() = testScope.runTest {
         var lastReportedTime = -1L
         alarmTimer.addSecondsListener { remainingSeconds ->
             lastReportedTime = remainingSeconds
         }
         alarmTimer.triggerInitialUpdate()
         alarmTimer.startOrPauseTimer() // Start

         advanceTimeBy(defaultSpielzeitMillis + 2000) // Advance past expiry

         assertEquals(0L, lastReportedTime, "Timer should report 0 seconds when expired. Was $lastReportedTime")
         assertFalse(alarmTimer.isTimerRunning(), "Timer should stop (pause) itself when it expires")
    }
}
