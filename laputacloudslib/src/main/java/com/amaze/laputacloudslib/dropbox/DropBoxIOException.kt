package com.amaze.laputacloudslib.dropbox

import com.amaze.laputacloudslib.CloudException
import com.dropbox.core.DbxException

open class DropBoxIOException: CloudException {
    constructor(e: DbxException): super(e)
    constructor(message: String): super(message)
    constructor(message: String, e: DbxException): super(message, e)
}