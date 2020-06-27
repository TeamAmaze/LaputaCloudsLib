package com.amaze.laputacloudslib.googledrive

import com.amaze.laputacloudslib.CloudException
import java.lang.Exception

class GoogleDriveException : CloudException {
    constructor(message: String) : super(message)
    constructor(exception: Exception) : super(exception)
}