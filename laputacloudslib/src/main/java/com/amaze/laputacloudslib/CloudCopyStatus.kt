package com.amaze.laputacloudslib

import androidx.annotation.FloatRange

abstract class AbstractCloudCopyStatus {
    companion object {
        const val OTHER = -4
        const val UNKNOWN = -3
        const val FAILED = -2
        const val NOT_STARTED = -1
        const val STARTED = 0
        const val DONE = 1
    }

    abstract fun getStatus(): Int

    @FloatRange(from = 0.0, to = 100.0)
    abstract fun getPercentage(): Float
}

