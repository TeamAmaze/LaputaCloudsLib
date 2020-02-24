package com.amaze.laputacloudslib

import com.dropbox.core.DbxException
import java.io.IOException

abstract class CloudException : IOException {
    constructor(e: Exception): super(e)
    constructor(message: String): super(message)
    constructor(message: String, e: Exception): super(message, e)
}

open class DropBoxIOException: CloudException {
    constructor(e: DbxException): super(e)
    constructor(message: String): super(message)
    constructor(message: String, e: DbxException): super(message, e)
}

class TooManyFailsException : DropBoxIOException {
    constructor(message: String) : super(message)
    constructor(message: String, lastException: DbxException) : super(message, lastException)
}