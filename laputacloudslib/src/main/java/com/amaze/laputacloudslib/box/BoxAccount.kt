package com.amaze.laputacloudslib.box

import android.content.Context
import arrow.core.Either
import arrow.core.computations.either
import com.amaze.laputacloudslib.AbstractAccount
import com.box.androidsdk.content.BoxApiFile
import com.box.androidsdk.content.BoxApiFolder
import com.box.androidsdk.content.BoxConfig
import com.box.androidsdk.content.auth.BoxAuthentication
import com.box.androidsdk.content.models.BoxSession
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BoxAccount(
    private val context: Context,
    clientId: String,
    clientSecret: String
) : AbstractAccount<BoxPath, BoxFile, BoxDriver>(), BoxAuthentication.AuthListener {

    init {
        BoxConfig.CLIENT_ID = clientId
        BoxConfig.CLIENT_SECRET = clientSecret
    }

    val session = BoxSession(context).also {
        it.setSessionAuthListener(this)
    }

    lateinit var callback: suspend (Either<Exception, BoxDriver>) -> Unit

    override suspend fun tryLogInAsync(callback: suspend (Either<Exception, BoxDriver>) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            this@BoxAccount.callback = callback

            try {
                session.authenticate(context)
            } catch (e: Exception) {
                callback(Either.Left(e))
            }
        }
    }

    override fun onLoggedOut(info: BoxAuthentication.BoxAuthenticationInfo?, ex: Exception?) {
        CoroutineScope(Dispatchers.Main).launch {
            val result = either<Exception, BoxDriver> {
                if (info != null && ex != null) {
                    throw BoxAccountException("Logged out of Box: " + info.toJson(), ex)
                } else if (info == null && ex != null) {
                    throw BoxAccountException("Logged out of Box", ex)
                } else if (info != null && ex == null) {
                    throw BoxAccountException("Logged out of Box: " + info.toJson())
                } else {// info == null && ex == null
                    throw BoxAccountException("Logged out of Box")
                }
            }

            callback(result)
        }
    }

    override fun onAuthCreated(info: BoxAuthentication.BoxAuthenticationInfo?) {
        CoroutineScope(Dispatchers.IO).launch {
            val result = either<Exception, BoxDriver> {
                val folderApi = BoxApiFolder(session)
                val fileApi = BoxApiFile(session)
                BoxDriver(folderApi, fileApi)
            }

            CoroutineScope(Dispatchers.Main).launch {
                callback(result)
            }
        }
    }

    override fun onRefreshed(info: BoxAuthentication.BoxAuthenticationInfo?) {
        CoroutineScope(Dispatchers.Main).launch {
            val result = either<Exception, BoxDriver> {
                if (info != null) {
                    throw BoxAccountException("Connection was restarted: " + info.toJson())
                } else {
                    throw BoxAccountException("Connection was restarted")
                }
            }

            callback(result)
        }
    }

    override fun onAuthFailure(info: BoxAuthentication.BoxAuthenticationInfo?, ex: Exception?) {
        CoroutineScope(Dispatchers.Main).launch {
            val result = either<Exception, BoxDriver> {
                if (info != null && ex != null) {
                    throw BoxAccountException("Auth failure: " + info.toJson(), ex)
                } else if (info == null && ex != null) {
                    throw BoxAccountException("Auth failure", ex)
                } else if (info != null && ex == null) {
                    throw BoxAccountException("Auth failure: " + info.toJson())
                } else {// info == null && ex == null
                    throw BoxAccountException("Auth failure")
                }
            }

            callback(result)
        }
    }
}