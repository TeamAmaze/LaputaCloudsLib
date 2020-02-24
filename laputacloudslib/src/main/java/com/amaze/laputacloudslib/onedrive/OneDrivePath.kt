package com.amaze.laputacloudslib.onedrive

import com.amaze.laputacloudslib.AbstractCloudPath

class OneDrivePath(path: String) : AbstractCloudPath<OneDrivePath>(path) {
    override val scheme: String = OneDriveDriver.SCHEME

    override fun createInstanceOfSubclass(path: String): OneDrivePath {
        return OneDrivePath(path)
    }
}