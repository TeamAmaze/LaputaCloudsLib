package com.amaze.laputacloudslib

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object Clouds {

    fun <Account: AbstractAccount<Path, File, Driver>, Driver: AbstractFileStructureDriver<Path, File>, Path: CloudPath, File: AbstractCloudFile<Path, File>> init(
        account: Account,
        callback: suspend (Driver) -> Unit
    ) {
        CoroutineScope(Dispatchers.Main).launch {
            account.tryLogInAsync { driver ->
                callback(driver)
            }
        }
    }

}