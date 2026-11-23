package com.jhaiian.kate.data

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

data class TrackPlays(val count: Int, val lastPlayedAt: Long)

object PlayCounts {
    var stats by mutableStateOf<Map<String, TrackPlays>>(emptyMap())
        private set
    var loaded by mutableStateOf(false)
        private set
    private var loadedVersion = -1
    private var job: Job? = null
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    fun of(trackId: String): TrackPlays? = stats[trackId]

    fun refresh(context: Context) {
        val appContext = context.applicationContext
        ListeningHistory.ensureLoaded(appContext)
        val version = ListeningHistory.version
        if (version == loadedVersion && loaded) return
        if (job?.isActive == true) return
        job = scope.launch {
            val result = ListeningHistory.trackPlayStats(appContext)
            stats = result
            loadedVersion = version
            loaded = true
            job = null
            if (ListeningHistory.version != version) refresh(appContext)
        }
    }
}
