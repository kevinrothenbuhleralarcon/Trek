package ch.kra.trek.helper

import androidx.lifecycle.MutableLiveData
import ch.kra.trek.other.Constants
import ch.kra.trek.services.TrackingService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class Chrono {
    private var isChronoEnabled = false
    private var lapTime = 0L
    private var totalTime = 0L
    private var timeStarted = 0L
    private var lastSecondTimestamp = 0L

    val timeInMs = MutableLiveData<Long>()
    val timeInS = MutableLiveData<Long>()

    init {
        resetTimer()
    }

    fun startTimer() {
        timeStarted = System.currentTimeMillis()
        timeInMs.postValue(0L)
        timeInS.postValue(0L)
        isChronoEnabled = true
        CoroutineScope(Dispatchers.Main).launch {
            while (isChronoEnabled) {
                lapTime = System.currentTimeMillis() - timeStarted
                TrackingService.timeInMs.postValue(totalTime + lapTime)
                if (TrackingService.timeInMs.value!! >= lastSecondTimestamp + 1000L) {
                    timeInS.postValue(timeInS.value!! + 1)
                    lastSecondTimestamp += 1000L
                }
                delay(Constants.TIMER_UPDATE_INTERVAL)
            }
            totalTime += lapTime
        }
    }

    fun pauseRestartTimer() {
        isChronoEnabled = !isChronoEnabled
    }

    fun stopTimer() {
        isChronoEnabled = false
    }

    fun resetTimer() {
        isChronoEnabled = false
        lapTime = 0L
        totalTime = 0L
        timeStarted = 0L
        lastSecondTimestamp = 0L
        timeInMs.postValue(0L)
        timeInS.postValue(0L)
    }
}