package com.amaze.laputacloudslib.box

import com.amaze.laputacloudslib.AbstractCloudPath
import com.amaze.laputacloudslib.CloudPath
import com.amaze.laputacloudslib.onedrive.OneDrivePath
import java.io.File

class BoxPath(val id: String, val isDirectory: Boolean = false, val isRoot: Boolean = false) :
    CloudPath {
    override val scheme: String = "box:"

    override val sanitizedPath = id

    override val fullPath: String = scheme + id

    /**
     *  see https://stackoverflow.com/a/19030914/3124150
     */
    fun getParentPathFromPath(): BoxPath
            = BoxPath(File(sanitizedPath).parent!!)

    fun join(fileName: String): BoxPath
            = BoxPath(sanitizedPath + AbstractCloudPath.SEPARATOR + fileName)
}