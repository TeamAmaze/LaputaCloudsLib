package com.amaze.laputacloudsapp.ui.tools

import android.app.Activity
import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.databinding.DataBindingUtil
import com.amaze.laputacloudsapp.R
import com.amaze.laputacloudsapp.databinding.FolderItemBinding
import com.amaze.laputacloudslib.AbstractCloudFile

class FileListAdapter(
    context: Context,
    list: List<FileData>,
    val asFolderSelector: Boolean = false,
    val uploadClickListener: () -> Unit
) : ArrayAdapter<FileListAdapter.FileData>(context, R.layout.folder_item, list) {
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val inflater = (parent.context as Activity).layoutInflater

        val binding: FolderItemBinding =
            if(convertView != null) DataBindingUtil.getBinding(convertView)!!
            else DataBindingUtil.inflate(inflater, R.layout.folder_item, parent, false)

        binding.file = getItem(position)
        binding.asFolderSelector = asFolderSelector
        binding.uploadClickListener = object : UploadClickListener {
            override fun onClickUpload() = uploadClickListener()
        }
        binding.executePendingBindings()
        return binding.root
    }

    interface UploadClickListener {
        fun onClickUpload()
    }

    data class FileData(val name: String, val isDirectory: Boolean)
}