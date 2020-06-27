package com.amaze.laputacloudsapp.ui.tools

import android.os.Bundle
import android.view.*
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ListView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.amaze.laputacloudsapp.*
import com.amaze.laputacloudsapp.appfolder.PhoneAccount
import com.amaze.laputacloudsapp.ui.tools.dialogs.FileActionsDialogFragment
import com.amaze.laputacloudslib.AbstractAccount
import com.amaze.laputacloudslib.AbstractCloudFile
import com.amaze.laputacloudslib.BoxAccount
import com.amaze.laputacloudslib.Clouds
import com.amaze.laputacloudslib.dropbox.DropBoxAccount
import com.amaze.laputacloudslib.googledrive.GoogleAccount
import com.amaze.laputacloudslib.onedrive.OneDriveAccount

class FileManagerFragment : Fragment(), AdapterView.OnItemClickListener,
    AdapterView.OnItemLongClickListener {

    companion object {
        const val CLOUD_SELECTED_ARG = "cloudName"

        const val ACCOUNT_PHONE = -1
        const val ACCOUNT_GOOGLEDRIVE = 0
        const val ACCOUNT_ONEDRIVE = 1
        const val ACCOUNT_DROPBOX = 2
        const val ACCOUNT_BOX = 4
    }

    lateinit var fileManagerViewModel: FileManagerViewModel

    private var files: List<AbstractCloudFile>? = null
    private var optionsMenu: Menu? = null
    private var cloudId: Int = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)

        fileManagerViewModel = ViewModelProviders.of(this).get(FileManagerViewModel::class.java)

        val root = inflater.inflate(R.layout.fragment_filemanager, container, false)
        val filesListView: ListView = root.findViewById(R.id.filesListView)
        val swipeRefreshLayout: SwipeRefreshLayout = root.findViewById(R.id.swipeRefreshLayout)

        cloudId = arguments!!.getInt(CLOUD_SELECTED_ARG)
        val account = getCloudAccount(cloudId)

        Clouds.init(account) { driver ->
            driver.getFile(driver.getRoot()) { file ->
                fileManagerViewModel.selectedFile.value = file
            }
        }

        fileManagerViewModel.selectedFile.observe(this, Observer { file ->
            swipeRefreshLayout.startLoad()

            if(filesListView.adapter == null) {
                filesListView.adapter = FileListAdapter(
                    requireContext(),
                    mutableListOf(),
                    (requireActivity() as MainActivity).uploadViewModel.selectingFileToUpload.value == true,
                    this::onClickUpload)

                filesListView.onItemClickListener = this
                filesListView.onItemLongClickListener = this
            }

            Clouds.init(account) { driver ->
                driver.getFiles(file.path) { files ->
                    this.files = files

                    val adapter =
                        (filesListView.adapter as ArrayAdapter<FileListAdapter.FileData>)

                    adapter.clear()
                    if (!file.isRootDirectory) {
                        adapter.add(FileListAdapter.FileData("..", true))
                    }
                    adapter.addAll(files.map {
                        FileListAdapter.FileData(it.name, it.isDirectory)
                    })

                    swipeRefreshLayout.endLoad()
                }
            }

        })

        fileManagerViewModel.moveStatus.observe(this, Observer { copiedFile ->
            optionsMenu?.findItem(R.id.item_paste)!!.isVisible = copiedFile != null
        })

        return root
    }

    fun reload() {
        fileManagerViewModel.selectedFile.value = fileManagerViewModel.selectedFile.value
    }

    fun onClickUpload() {
        (requireActivity() as MainActivity).uploadViewModel.folderLiveData.value = fileManagerViewModel.selectedFile.value
    }

    override fun onItemClick(
        parent: AdapterView<*>?,
        view: View,
        position: Int,
        id: Long
    ) {
        val selectedFile = fileManagerViewModel.selectedFile.value!!

        if(position == 0 && !selectedFile.isRootDirectory) {
            selectedFile.getParent {
                fileManagerViewModel.selectedFile.value = it
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
        if((requireActivity() as MainActivity).uploadViewModel.selectingFileToUpload.value == true) {
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
            ).show(requireFragmentManager(), "FILE")
        }

        return true
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.main_appbar, menu)

        optionsMenu = menu
    }

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

    private fun getCloudAccount(id: Int): AbstractAccount = when (id) {
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

