package com.amaze.laputacloudslib.onedrive

import com.onedrive.sdk.concurrency.ICallback
import com.onedrive.sdk.core.ClientException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

abstract class CrashyCallback<Result> : ICallback<Result> {
    override fun failure(ex: ClientException) {
        throw OneDriveIOException(ex)
    }
}

fun <Result> crashOnFailure(callback: suspend (Result) -> Unit) = object : CrashyCallback<Result>() {
    override fun success(result: Result) {
        CoroutineScope(Dispatchers.Main).launch {
            callback(result)
        }
    }
}
