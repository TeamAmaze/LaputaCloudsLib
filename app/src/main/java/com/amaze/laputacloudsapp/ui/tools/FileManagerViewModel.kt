package com.amaze.laputacloudsapp.ui.tools

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.amaze.laputacloudslib.AbstractCloudFile

class FileManagerViewModel : ViewModel() {
    val selectedFile = MutableLiveData<AbstractCloudFile>()
    val moveStatus = MutableLiveData<MoveStatus>()

    class MoveStatus(val copiedFile: AbstractCloudFile, val deleteOriginal: Boolean)
}