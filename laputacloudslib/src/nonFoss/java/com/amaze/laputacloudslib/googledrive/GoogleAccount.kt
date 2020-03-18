package com.amaze.laputacloudslib.googledrive

import com.amaze.laputacloudslib.AbstractAccount
import com.amaze.laputacloudslib.AbstractFileStructureDriver

class GoogleAccount : AbstractAccount() {
    override suspend fun tryLogInAsync(callback: suspend (AbstractFileStructureDriver) -> Unit) {
        TODO("Not yet implemented")
    }
}