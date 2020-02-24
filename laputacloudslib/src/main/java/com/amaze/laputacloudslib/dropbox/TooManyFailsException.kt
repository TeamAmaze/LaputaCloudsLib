package com.amaze.laputacloudslib.dropbox

import com.amaze.laputacloudslib.dropbox.DropBoxIOException
import com.dropbox.core.DbxException

class TooManyFailsException :
    DropBoxIOException {
    constructor(message: String) : super(message)
    constructor(message: String, lastException: DbxException) : super(message, lastException)
}