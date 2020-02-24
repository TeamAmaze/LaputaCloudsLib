package com.amaze.laputacloudslib

import java.io.IOException

abstract class CloudException : IOException {
    constructor(e: Exception): super(e)
    constructor(message: String): super(message)
    constructor(message: String, e: Exception): super(message, e)
}

