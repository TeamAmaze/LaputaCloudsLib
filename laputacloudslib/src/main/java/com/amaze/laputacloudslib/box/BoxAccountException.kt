package com.amaze.laputacloudslib.box

import com.amaze.laputacloudslib.CloudException

open class BoxAccountException: CloudException {
    constructor(e: Exception): super(e)
    constructor(message: String): super(message)
    constructor(message: String, e: Exception): super(message, e)
}