package com.amaze.laputacloudsapp.ui.home

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.amaze.laputacloudsapp.MainActivity
import com.amaze.laputacloudsapp.R
import com.amaze.laputacloudsapp.models.UploadViewModel

class HomeFragment : Fragment() {

    companion object {
        const val AS_FILE_CHOOSER_ARG = "asFileChooser"
    }

    private lateinit var homeViewModel: HomeViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        homeViewModel =
            ViewModelProviders.of(this).get(HomeViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_home, container, false)
        val textView: TextView = root.findViewById(R.id.text_home)
        homeViewModel.text.observe(this, Observer {
            textView.text = it
        })

        if(checkPermission()) {
            homeViewModel.text.value = "\uD83D\uDFE2 Has needed permissions"
        } else {
            homeViewModel.text.value = "\uD83D\uDD34 DOES NOT HAVE needed permissions"
        }

        val startedAsFolderChooser = arguments!!.getBoolean(AS_FILE_CHOOSER_ARG)

        val uploadViewModel = ViewModelProviders.of( requireActivity() as MainActivity)
            .get(UploadViewModel::class.java)

        uploadViewModel.selectingFileToUpload.value = startedAsFolderChooser

        return root
    }


    private fun checkPermission(): Boolean {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
            && ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE),
                0
            )
            return false
        }

        return true
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == 0 && grantResults.isNotEmpty() || grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            homeViewModel.text.value = "\uD83D\uDFE2 Has needed permissions"
        } else {
            homeViewModel.text.value = "\uD83D\uDD34 DOES NOT HAVE needed permissions"
            checkPermission()
        }
    }
}