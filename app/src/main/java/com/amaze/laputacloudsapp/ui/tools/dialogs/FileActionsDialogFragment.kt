package com.amaze.laputacloudsapp.ui.tools.dialogs

import android.app.Dialog
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import arrow.core.Either
import com.amaze.laputacloudsapp.models.UploadViewModel
import com.amaze.laputacloudsapp.ui.tools.FileManagerFragment
import com.amaze.laputacloudsapp.ui.tools.FileManagerViewModel
import com.amaze.laputacloudslib.AbstractAccount
import com.amaze.laputacloudslib.AbstractCloudFile
import com.amaze.laputacloudslib.AbstractFileStructureDriver
import com.amaze.laputacloudslib.CloudPath
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File
import java.io.InputStream

class FileActionsDialogFragment<Path: CloudPath, CloudFile: AbstractCloudFile<Path, CloudFile>, Driver: AbstractFileStructureDriver<Path, CloudFile>, Account: AbstractAccount<Path, CloudFile, Driver>>(
    private val fileManagerFragment: FileManagerFragment<Path, CloudFile, Driver, Account>,
    private val file: CloudFile,
    private val cloudId: Int
) : DialogFragment() {

    val fileManagerViewModel: FileManagerViewModel<Path, CloudFile> by fileManagerFragment.viewModels()

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
                when(inputStream) {
                    is Either.Left -> Toast.makeText(context, "Error: ${inputStream.value.message}", Toast.LENGTH_LONG).show()
                    is Either.Right -> {
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
                        CoroutineScope(Dispatchers.Main).launch {
                            downloadFile.copyInputStreamToFile(inputStream.value)
                            launch(Dispatchers.Main) {
                                downloadSnack.dismiss()
                            }
                        }
                    }
                }
            }
        },
        "Upload" to {
            val uploadViewModel : UploadViewModel<Path, CloudFile> by viewModels()
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
                } else !(cloudId != FileManagerFragment.ACCOUNT_PHONE && it.first == "Upload")
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