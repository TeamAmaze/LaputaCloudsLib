package com.amaze.laputacloudslib.dropbox

import arrow.core.Either
import arrow.core.computations.either
import com.amaze.laputacloudslib.AbstractAccount
import com.amaze.laputacloudslib.AbstractFileStructureDriver
import com.dropbox.core.DbxRequestConfig
import com.dropbox.core.v2.DbxClientV2
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class DropBoxAccount(val accessToken: String) : AbstractAccount<DropBoxPath, DropBoxFile, DropBoxDriver>() {
    override suspend fun tryLogInAsync(
        callback: suspend (Either<Exception, DropBoxDriver>) -> Unit
    ) {
        withContext(Dispatchers.IO) {
            val result = either<Exception, DropBoxDriver> {
                val config =
                    DbxRequestConfig.newBuilder("LaputaCloudsLibSample")
                        .build()
                val client = DbxClientV2(config, accessToken)
                DropBoxDriver(client)
            }

            withContext(Dispatchers.Main) {
                callback(result)
            }
        }
    }

}