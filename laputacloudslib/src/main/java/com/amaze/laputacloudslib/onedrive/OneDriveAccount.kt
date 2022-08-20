package com.amaze.laputacloudslib.onedrive

import android.app.Activity
import arrow.core.Either
import arrow.core.computations.either
import com.amaze.laputacloudslib.AbstractAccount
import com.amaze.laputacloudslib.AbstractFileStructureDriver
import com.onedrive.sdk.authentication.ADALAuthenticator
import com.onedrive.sdk.authentication.MSAAuthenticator
import com.onedrive.sdk.concurrency.ICallback
import com.onedrive.sdk.core.ClientException
import com.onedrive.sdk.core.DefaultClientConfig
import com.onedrive.sdk.core.IClientConfig
import com.onedrive.sdk.extensions.IOneDriveClient
import com.onedrive.sdk.extensions.OneDriveClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.lang.IllegalArgumentException

/**
 * Note that your msa-client-id and adal-client-id should look be in GUID format like 00000000-0000-0000-0000-000000000000.
 * For legacy MSA application, the msa-client-id should look like 0000000000000000.
 */
class OneDriveAccount(
    private val activity: Activity,
    private val msaaClientId: String?,
    private val redirectionUri: String?,
    private val adalClientId: String?
): AbstractAccount<OneDrivePath, OneDriveCloudFile, OneDriveDriver>() {
    val msaAuthenticator = object : MSAAuthenticator() {
        override fun getClientId() = msaaClientId

        override fun getScopes() = arrayOf("onedrive.readwrite")
    }

    val adalAuthenticator = object : ADALAuthenticator() {
        override fun getClientId() = adalClientId

        override fun getRedirectUrl() = redirectionUri
    }

    override suspend fun tryLogInAsync(callback: suspend (Either<Exception, OneDriveDriver>) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            val oneDriveConfig = either<Exception, IClientConfig> {
                if (msaaClientId != null && redirectionUri != null && adalClientId != null) {
                    DefaultClientConfig.createWithAuthenticators(
                        msaAuthenticator,
                        adalAuthenticator
                    )
                } else if (msaaClientId != null && redirectionUri != null) {
                    DefaultClientConfig.createWithAuthenticator(
                        msaAuthenticator
                    )
                } else if (adalClientId != null) {
                    DefaultClientConfig.createWithAuthenticator(
                        adalAuthenticator
                    )
                } else {
                    if (msaaClientId != null && redirectionUri == null) {
                        throw IllegalArgumentException("Redirection uri must not be null (MSA auth used)!")
                    }
                    throw IllegalArgumentException("At least one client id is needed!")
                }
            }

            when (oneDriveConfig) {
                is Either.Left -> {
                    callback(oneDriveConfig)
                }
                is Either.Right -> {
                    OneDriveClient.Builder()
                        .fromConfig(oneDriveConfig.value)
                        .loginAndBuildClient(activity, object :
                            ICallback<IOneDriveClient> {
                            override fun success(oneDriveClient: IOneDriveClient) {
                                CoroutineScope(Dispatchers.Main).launch {
                                    callback(Either.Right(OneDriveDriver(oneDriveClient)))
                                }
                            }

                            override fun failure(ex: ClientException) {
                                CoroutineScope(Dispatchers.Main).launch {
                                    callback(Either.Left(ex))
                                }
                            }
                        })
                }
            }
        }
    }
}