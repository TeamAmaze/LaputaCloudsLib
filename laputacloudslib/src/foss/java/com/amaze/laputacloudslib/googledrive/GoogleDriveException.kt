package com.amaze.laputacloudslib.googledrive

import com.amaze.laputacloudslib.CloudException

class InvalidUrlException(url: String): CloudException("Invalid url: $url")