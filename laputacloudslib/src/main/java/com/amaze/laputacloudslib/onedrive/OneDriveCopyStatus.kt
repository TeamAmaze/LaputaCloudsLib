package com.amaze.laputacloudslib.onedrive

import androidx.annotation.FloatRange
import com.onedrive.sdk.concurrency.AsyncMonitor
import com.onedrive.sdk.extensions.Item

@Suppress("unused")
class OneDriveCopyStatus(val asyncMonitor: AsyncMonitor<Item>) {
    companion object {
        const val OTHER = -4
        const val UNKNOWN = -3
        const val FAILED = -2
        const val NOT_STARTED = -1
        const val STARTED = 0
        const val DONE = 1
    }

    fun getStatus(): Int =
        when (asyncMonitor.status.status) {
            "notStarted" -> NOT_STARTED
            "inProgress" -> STARTED
            "completed" -> DONE
            "updating" -> OTHER
            "failed" -> FAILED
            "deletePending" -> OTHER
            "deleteFailed" -> OTHER
            "waiting" -> OTHER
            else -> UNKNOWN
        }

    @FloatRange(from = 0.0, to = 100.0)
    fun getPercentage(): Float {
        return asyncMonitor.status.percentageComplete.toFloat()
    }

}