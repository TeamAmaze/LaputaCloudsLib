package com.amaze.laputacloudslib.googledrive

import com.amaze.laputacloudslib.CloudPath

class GoogleDrivePath(
    override val scheme: String,
    override val sanitizedPath: String,
    override val fullPath: String
) : CloudPath {
}