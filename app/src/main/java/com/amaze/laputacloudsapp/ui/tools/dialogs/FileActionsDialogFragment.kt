package com.amaze.laputacloudsapp.ui.tools.dialogs

import android.app.Dialog
import android.os.Build
import android.os.Bundle
import android.os.Environment
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.amaze.laputacloudsapp.models.UploadViewModel
import com.amaze.laputacloudsapp.ui.tools.FileManagerFragment
import com.amaze.laputacloudsapp.ui.tools.FileManagerViewModel
import com.amaze.laputacloudslib.AbstractCloudFile
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File
import java.io.InputStream

class FileActionsDialogFragment(
    private val fileManagerFragment: FileManagerFragment,
    private val file: AbstractCloudFile,
    private val cloudId: Int
) : DialogFragment() {

    val fileManagerViewModel: FileManagerViewModel by fileManagerFragment.viewModels()

    private val DIALOG_ITEMS = listOf(
        "Copy" to {
            fileManagerViewModel.moveStatus.value =
                FileManagerViewModel.MoveStatus(file, false)
        },
        "Move" to {
            fileManagerViewModel.moveStatus.value =
                FileManagerViewModel.MoveStatus(file, true)
        },
        "Download" to {
            file.download { inputStream ->
                val downloadSnack =
                    Snackbar.make(
                        fileManagerFragment.requireView(),
                        "Downloading...",
                        Snackbar.LENGTH_INDEFINITE
                    )
                downloadSnack.show()

                val downloadFolder =
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                        Environment.getExternalStoragePublicDirectory(
                            Environment.DIRECTORY_DOWNLOADS
                        )
                    } else {
                        fileManagerFragment.requireContext().filesDir
                    }

                val downloadFile = File(downloadFolder, file.name)
                downloadFile.createNewFile()
                GlobalScope.launch {
                    downloadFile.copyInputStreamToFile(inputStream)
                    launch(Dispatchers.Main) {
                        downloadSnack.dismiss()
                    }
                }
            }
        },
        "Upload" to {
            val uploadViewModel : UploadViewModel by viewModels()
            uploadViewModel.fileToUpload = file
            uploadViewModel.selectingFileToUpload.value = true

            val action = FileActionsDialogFragmentDirections.actionGlobalNavHome(true)
            findNavController().navigate(action)
        },
        "Delete" to {
            file.delete { fileManagerFragment.reload() }
        }
    )

    private fun File.copyInputStreamToFile(inputStream: InputStream) {
        outputStream().use { fileOut ->
            inputStream.copyTo(fileOut)
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val filteredDialogItems = DIALOG_ITEMS
            .filter {
                if(cloudId == FileManagerFragment.ACCOUNT_PHONE && it.first == "Download") {
                    false
                } else if(cloudId != FileManagerFragment.ACCOUNT_PHONE && it.first == "Upload") {
                    false
                } else true
            }

        val builder = AlertDialog.Builder(requireActivity())
            .setTitle("File actions")
            .setItems(filteredDialogItems
                .map { it.first }
                .toTypedArray()
            ) { _, clickedIndex: Int ->
                filteredDialogItems[clickedIndex].second()
            }

        return builder.create()
    }
}