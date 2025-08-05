// model/TimerMechanism.kt
package com.aightech.projecttimer.model

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.aightech.projecttimer.util.NotificationHelper
import com.aightech.projecttimer.util.PersistenceManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class TimerMechanism(app: Application) : AndroidViewModel(app) {
    private val scope = MainScope()
    private var timerJob: Job? = null
    private var currentProject: Project? = null

    fun toggleSession(project: Project) {
        if (currentProject == project) stop() else start(project)
    }

    private fun start(p: Project) {
        currentProject = p
        NotificationHelper.showOngoing(getApplication(), p.name, 0L)
        timerJob = scope.launch {
            var elapsed = 0L
            while (true) {
                delay(1000)
                elapsed += 1000
                NotificationHelper.update(getApplication(), p.name, elapsed)
            }
        }
    }

    private fun stop() {
        timerJob?.cancel()
        NotificationHelper.cancel(getApplication())
        PersistenceManager.saveActiveSession(currentProject to 0L)
        currentProject = null
    }

    override fun onCleared() {
        super.onCleared()
        PersistenceManager.saveActiveSession(currentProject to 0L)
    }
}