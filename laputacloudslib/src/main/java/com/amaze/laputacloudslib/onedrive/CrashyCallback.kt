package com.amaze.laputacloudslib.onedrive

import arrow.core.Either
import com.onedrive.sdk.concurrency.ICallback
import com.onedrive.sdk.core.ClientException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

fun <Result> crashOnFailure(callback: suspend (Either<Exception, Result>) -> Unit) = object : ICallback<Result> {
    override fun failure(ex: ClientException) {
        CoroutineScope(Dispatchers.Main).launch {
            callback(Either.Left(OneDriveIOException(ex)))
        }
    }

    override fun success(result: Result) {
        CoroutineScope(Dispatchers.Main).launch {
            callback(Either.Right(result))
        }
    }
}
