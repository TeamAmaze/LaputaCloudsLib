package com.amaze.laputacloudslib.dropbox

import com.amaze.laputacloudslib.AbstractCloudPath

class DropBoxPath(path: String) : AbstractCloudPath<DropBoxPath>(path) {
    override val scheme: String =
        DropBoxDriver.SCHEME

    val sanitizedPathOrRoot: String
        get() = if(super.sanitizedPath == SEPARATOR) "" //For root
                else super.sanitizedPath

    override fun createInstanceOfSubclass(path: String): DropBoxPath {
        return DropBoxPath(path)
    }
}