package com.amaze.laputacloudsapp.ui.tools

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.amaze.laputacloudslib.AbstractCloudFile
import com.amaze.laputacloudslib.CloudPath

class FileManagerViewModel<Path: CloudPath, File: AbstractCloudFile<Path, File>> : ViewModel() {
    val selectedFile = MutableLiveData<File>()
    val moveStatus = MutableLiveData<MoveStatus<Path, File>>()

    class MoveStatus<Path: CloudPath, File: AbstractCloudFile<Path, File>>(val copiedFile: File, val deleteOriginal: Boolean)
}