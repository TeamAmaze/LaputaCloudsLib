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
    private val _uploadEvents = MutableLiveData<Event<UploadEvent>>()

    val events : LiveData<Event<UploadEvent>>
        get() = _uploadEvents


    fun setUploadStarted() {
        _uploadEvents.value = Event(UploadEvent(UPLOAD_STARTED, 0f))
    }

    fun setUploadProgressed(progress: Float) {
        _uploadEvents.value = Event(UploadEvent(UPLOAD_PROGRESS, progress))
    }

    fun setUploadEnded() {
        _uploadEvents.value = Event(UploadEvent(UPLOAD_ENDED, 100f))
    }

    companion object {
        const val UPLOAD_STARTED = "uploadStart"
        const val UPLOAD_PROGRESS = "uploadProgress"
        const val UPLOAD_ENDED = "uploadEnd"
    }

    data class UploadEvent(val type: String, val progress: Float)
}