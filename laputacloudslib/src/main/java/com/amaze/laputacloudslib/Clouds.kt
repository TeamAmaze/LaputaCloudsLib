package com.amaze.laputacloudslib

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object Clouds {

    fun init(
        account: AbstractAccount,
        callback: suspend (AbstractFileStructureDriver) -> Unit
    ) {
        CoroutineScope(Dispatchers.Main).launch {
            account.tryLogInAsync { driver ->
                callback(driver)
            }
        }
    }

}