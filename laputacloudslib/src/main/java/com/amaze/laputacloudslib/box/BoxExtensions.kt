package com.amaze.laputacloudslib.box

import com.amaze.laputacloudslib.BoxFile
import com.amaze.laputacloudslib.BoxPath
import com.box.androidsdk.content.models.BoxFolder
import com.box.androidsdk.content.models.BoxItem

fun BoxItem.toFile(): BoxFile {
    return BoxFile(BoxPath(id, this is BoxFolder), this)
}