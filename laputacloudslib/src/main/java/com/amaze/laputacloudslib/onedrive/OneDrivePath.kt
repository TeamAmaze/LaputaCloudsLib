package com.amaze.laputacloudslib.onedrive

import com.amaze.laputacloudslib.AbstractCloudPath
import java.io.File

class OneDrivePath(path: String) : AbstractCloudPath<OneDrivePath>(path) {
    override val scheme: String = OneDriveDriver.SCHEME

    /**
     *  see https://stackoverflow.com/a/19030914/3124150
     */
    fun getParentFromPath(): OneDrivePath
            = OneDrivePath(File(sanitizedPath).parent!!)

    fun join(fileName: String): OneDrivePath
            = OneDrivePath(sanitizedPath + SEPARATOR + fileName)
}