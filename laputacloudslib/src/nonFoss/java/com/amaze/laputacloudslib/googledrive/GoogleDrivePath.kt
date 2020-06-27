package com.amaze.laputacloudslib.googledrive

import com.amaze.laputacloudslib.AbstractCloudPath

class GoogleDrivePath(fileId: String, isRoot: Boolean): AbstractCloudPath<GoogleDrivePath>(fileId) {
    override val scheme: String = "googledrive:"


}