package com.amaze.laputacloudsapp.ui.tools

import android.os.Bundle
import android.view.*
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.get
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import arrow.core.Either
import com.amaze.laputacloudsapp.HiddenConstants
import com.amaze.laputacloudsapp.MainActivity
import com.amaze.laputacloudsapp.R
import com.amaze.laputacloudsapp.appfolder.PhoneAccount
import com.amaze.laputacloudsapp.models.UploadViewModel
import com.amaze.laputacloudsapp.ui.tools.dialogs.FileActionsDialogFragment
import com.amaze.laputacloudslib.*
import com.amaze.laputacloudslib.box.BoxAccount
import com.amaze.laputacloudslib.dropbox.DropBoxAccount
import com.amaze.laputacloudslib.googledrive.GoogleAccount
import com.amaze.laputacloudslib.onedrive.OneDriveAccount

open class FileManagerFragment<Path: CloudPath, File: AbstractCloudFile<Path, File>, Driver: AbstractFileStructureDriver<Path, File>, Account: AbstractAccount<Path, File, Driver>> : Fragment(), AdapterView.OnItemClickListener,
    AdapterView.OnItemLongClickListener {

    companion object {
        const val CLOUD_SELECTED_ARG = "cloudName"

        const val ACCOUNT_PHONE = -1
        const val ACCOUNT_GOOGLEDRIVE = 0
        const val ACCOUNT_ONEDRIVE = 1
        const val ACCOUNT_DROPBOX = 2
        const val ACCOUNT_BOX = 4
    }

    private val fileManagerViewModel: FileManagerViewModel<Path, File> by viewModels()
    private lateinit var uploadViewModel : UploadViewModel<Path, File>

    private var files: List<File>? = null
    private var optionsMenu: Menu? = null
    private var cloudId: Int = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)

        val root = inflater.inflate(R.layout.fragment_filemanager, container, false)
        val filesListView: ListView = root.findViewById(R.id.filesListView)
        val swipeRefreshLayout: SwipeRefreshLayout = root.findViewById(R.id.swipeRefreshLayout)

        cloudId = requireArguments().getInt(CLOUD_SELECTED_ARG)
        val account = getCloudAccount(cloudId) as Account

        Clouds.init(account) { driver ->
            when(driver) {
                is Either.Left -> Toast.makeText(context, "Error: ${driver.value.message}", Toast.LENGTH_LONG).show()
                is Either.Right -> driver.value.getFile(driver.value.getRoot()) { file ->
                    when(file) {
                        is Either.Left -> Toast.makeText(context, "Error: ${file.value.message}", Toast.LENGTH_LONG).show()
                        is Either.Right -> fileManagerViewModel.selectedFile.value = file.value
                    }
                }
            }
        }

        fileManagerViewModel.selectedFile.observe(viewLifecycleOwner) { file ->
            swipeRefreshLayout.startLoad()

            if (filesListView.adapter == null) {
                filesListView.adapter = FileListAdapter(
                    requireContext(),
                    mutableListOf(),
                    uploadViewModel.selectingFileToUpload.value == true,
                    this::onClickUpload
                )

                filesListView.onItemClickListener = this
                filesListView.onItemLongClickListener = this
            }

            Clouds.init(account) { driver ->
                when(driver) {
                    is Either.Left -> Toast.makeText(context, "Error: ${driver.value.message}", Toast.LENGTH_LONG).show()
                    is Either.Right -> driver.value.getFiles(file.path) { files ->
                        when (files) {
                            is Either.Left -> Toast.makeText(
                                context,
                                "Error: ${files.value.message}",
                                Toast.LENGTH_LONG
                            ).show()
                            is Either.Right -> {
                                this.files = files.value

                                val adapter =
                                    (filesListView.adapter as ArrayAdapter<FileListAdapter.FileData>)

                                adapter.clear()
                                if (!file.isRootDirectory) {
                                    adapter.add(FileListAdapter.FileData("..", true))
                                }
                                adapter.addAll(files.value.map {
                                    FileListAdapter.FileData(it.name, it.isDirectory)
                                })
                            }
                        }

                        swipeRefreshLayout.endLoad()
                    }
                }
            }

        }

        fileManagerViewModel.moveStatus.observe(viewLifecycleOwner) { copiedFile ->
            optionsMenu?.findItem(R.id.item_paste)!!.isVisible = copiedFile != null
        }

        uploadViewModel = ViewModelProvider(requireActivity() as MainActivity).get()

        return root
    }

    fun reload() {
        fileManagerViewModel.selectedFile.value = fileManagerViewModel.selectedFile.value
    }

    fun onClickUpload() {
        uploadViewModel.folderLiveData.value = fileManagerViewModel.selectedFile.value
    }

    override fun onItemClick(
        parent: AdapterView<*>?,
        view: View,
        position: Int,
        id: Long
    ) {
        val selectedFile = fileManagerViewModel.selectedFile.value!!

        if(position == 0 && !selectedFile.isRootDirectory) {
            selectedFile.getParent { parentFile ->
                when(parentFile) {
                    is Either.Left -> Toast.makeText(context, "Error: ${parentFile.value.message}", Toast.LENGTH_LONG).show()
                    is Either.Right -> fileManagerViewModel.selectedFile.value = parentFile.value
                }
            }
            return
        }

        val file = if(selectedFile.isRootDirectory) files!![position] else files!![position - 1]

        if(file.isDirectory) {
            fileManagerViewModel.selectedFile.value = file
        }
    }

    override fun onItemLongClick(
        parent: AdapterView<*>?,
        view: View?,
        position: Int,
        id: Long
    ): Boolean {
        if(uploadViewModel.selectingFileToUpload.value == true) {
            return false
        }

        val selectedFile = fileManagerViewModel.selectedFile.value!!

        if(!selectedFile.isRootDirectory && position == 0) {
            return false
        }

        val file = if(selectedFile.isRootDirectory) files!![position] else files!![position - 1]

        if(!file.isDirectory) {
            FileActionsDialogFragment(
                this,
                file,
                cloudId
            ).show(parentFragmentManager, "FILE")
        }

        return true
    }

    @Deprecated("Deprecated in Java")
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.main_appbar, menu)

        optionsMenu = menu
    }

    @Deprecated("Deprecated in Java")
    override fun onOptionsItemSelected(item: MenuItem): Boolean =
        when(item.itemId) {
            R.id.item_paste -> {
                val copiedFile = fileManagerViewModel.moveStatus.value!!.copiedFile
                val deleteOriginal = fileManagerViewModel.moveStatus.value!!.deleteOriginal
                val currentFolder = fileManagerViewModel.selectedFile.value!!

                if(deleteOriginal) {
                    copiedFile.moveTo("(move) " + copiedFile.name, currentFolder) {
                        fileManagerViewModel.moveStatus.value = null
                        reload()
                    }
                } else {
                    copiedFile.copyTo("(copy) " + copiedFile.name, currentFolder) {
                        fileManagerViewModel.moveStatus.value = null
                        reload()
                    }
                }

                true
            }
            else -> false
        }

    private fun getCloudAccount(id: Int): AbstractAccount<*, *, *> = when (id) {
        ACCOUNT_PHONE -> PhoneAccount()
        ACCOUNT_ONEDRIVE -> OneDriveAccount(
            requireActivity(),
            HiddenConstants.OneDrive.MSAAClientId,
            HiddenConstants.OneDrive.redirectionUri,
            null
        )
        ACCOUNT_DROPBOX -> DropBoxAccount(
            HiddenConstants.DropBox.accessToken
        )
        ACCOUNT_GOOGLEDRIVE -> GoogleAccount(
            requireContext(),
            "sampleApp",
            HiddenConstants.GoogleDrive.clientId,
            ""
        )
        ACCOUNT_BOX -> BoxAccount(
            requireContext(),
            HiddenConstants.Box.clientId,
            HiddenConstants.Box.clientSecret
        )
        else -> throw IllegalArgumentException()
    }

    private fun SwipeRefreshLayout.startLoad() {
        isEnabled = true
        isRefreshing = true
    }

    private fun SwipeRefreshLayout.endLoad() {
        isRefreshing = false
        isEnabled = false
    }

}

