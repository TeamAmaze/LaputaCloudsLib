package com.amaze.laputacloudslib.onedrive

import androidx.annotation.FloatRange
import com.amaze.laputacloudslib.AbstractCloudCopyStatus
import com.onedrive.sdk.concurrency.AsyncMonitor
import com.onedrive.sdk.extensions.Item

class OneDriveCopyStatus(val asyncMonitor: AsyncMonitor<Item>) : AbstractCloudCopyStatus() {
    override fun getStatus(): Int =
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
    override fun getPercentage(): Float {
        return asyncMonitor.status.percentageComplete.toFloat()
    }

}