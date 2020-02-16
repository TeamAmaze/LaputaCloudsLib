package com.amaze.laputacloudslib.onedrive

import com.onedrive.sdk.concurrency.ICallback
import com.onedrive.sdk.core.ClientException

abstract class CrashyCallback<Result> : ICallback<Result> {
    override fun failure(ex: ClientException) {
        throw OneDriveIOException(ex)
    }
}

fun <Result> crashOnFailure(callback: (Result) -> Unit) = object : CrashyCallback<Result>() {
    override fun success(result: Result) {
        callback(result)
    }
}
