package com.amaze.laputacloudslib.box

import com.amaze.laputacloudslib.CloudPath

class BoxPath(val id: String, val isDirectory: Boolean = false, val isRoot: Boolean = false) :
    CloudPath {
    override val scheme: String = "box:"

    override val sanitizedPath = id

    override val fullPath: String = scheme + id
}