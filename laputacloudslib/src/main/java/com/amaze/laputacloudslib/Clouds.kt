package com.amaze.laputacloudslib

import arrow.core.Either
import arrow.core.right
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object Clouds {

    fun <Account: AbstractAccount<Path, File, Driver>, Driver: AbstractFileStructureDriver<Path, File>, Path: CloudPath, File: AbstractCloudFile<Path, File>> init(
        account: Account,
        callback: suspend (Either<Exception, Driver>) -> Unit
    ) {
        CoroutineScope(Dispatchers.Main).launch {
            account.tryLogInAsync { errorOrDriver ->
                callback(errorOrDriver)
            }
        }
    }

}