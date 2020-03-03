package com.amaze.laputacloudslib

import com.box.sdk.BoxAPIConnection

abstract class AbstractAccount {
    abstract suspend fun tryLogInAsync(callback: suspend (AbstractFileStructureDriver) -> Unit)
}

class BoxAccount : AbstractAccount {

    val api: BoxAPIConnection

    constructor(clientId: String, clientSecret: String, authCode: String): super() {
        api = BoxAPIConnection(clientId, clientSecret, authCode)
    }

    constructor(developerToken: String) : super() {
        api = BoxAPIConnection(developerToken)
    }

    override suspend fun tryLogInAsync(callback: suspend (AbstractFileStructureDriver) -> Unit) {
        callback(BoxDriver(api))
    }
}