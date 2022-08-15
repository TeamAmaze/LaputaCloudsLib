package com.amaze.laputacloudslib.box

import com.box.androidsdk.content.BoxApiFile
import com.box.androidsdk.content.models.BoxFolder
import com.box.androidsdk.content.models.BoxItem

fun BoxItem.toFile(fileApiFile: BoxApiFile): BoxFile {
    return BoxFile(fileApiFile,this, BoxPath(id, this is BoxFolder))
}