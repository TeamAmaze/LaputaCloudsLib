package com.amaze.laputacloudslib.dropbox

import com.amaze.laputacloudslib.AbstractCloudPath
import java.io.File

class DropBoxPath(path: String) : AbstractCloudPath<DropBoxPath>(path) {
    override val scheme: String =
        DropBoxDriver.SCHEME

    val sanitizedPathOrRoot: String
        get() = if(super.sanitizedPath == SEPARATOR) "" //For root
                else super.sanitizedPath
    
    /**
     *  see https://www.dropboxforum.com/t5/API-Support-Feedback/Get-parent-folder/td-p/247874
     */
    fun getParentFromPath(): DropBoxPath
            = DropBoxPath(File(sanitizedPath).parent!!)

    fun join(fileName: String): DropBoxPath
            = DropBoxPath(sanitizedPath + SEPARATOR + fileName)
}