package com.amaze.laputacloudsapp.models

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.amaze.laputacloudsapp.Event
import com.amaze.laputacloudslib.AbstractCloudFile

/**
 * Maintains the current state when the user is selecting a file to upload
 */
class UploadViewModel : ViewModel() {
    val selectingFileToUpload: MutableLiveData<Boolean> = MutableLiveData()
    var fileToUpload: AbstractCloudFile? = null
    val folderLiveData: MutableLiveData<AbstractCloudFile> = MutableLiveData()
    private val _uploadEvents = MutableLiveData<Event<String>>()

    val events : LiveData<Event<String>>
        get() = _uploadEvents


    fun setUploadStarted() {
        _uploadEvents.value = Event(UPLOAD_STARTED)
    }

    fun setUploadEnded() {
        _uploadEvents.value = Event(UPLOAD_ENDED)
    }

    companion object {
        const val UPLOAD_STARTED = "uploadStart"
        const val UPLOAD_ENDED = "uploadEnd"
    }
}