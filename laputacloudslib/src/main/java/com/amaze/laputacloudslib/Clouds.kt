package com.amaze.laputacloudslib

object Clouds {

    fun init(account: AbstractAccount, callback: (AbstractFileStructureDriver) -> Unit) {
        account.tryLogInAsync { user ->
            user.getFileStructureDriverAsync { fileStructureDriver ->
                callback(fileStructureDriver)
            }
        }
    }

}