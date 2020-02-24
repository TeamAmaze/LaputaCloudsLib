package com.amaze.laputacloudsapp

import android.os.AsyncTask
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.amaze.laputacloudsapp.models.UploadViewModel
import com.amaze.laputacloudslib.AbstractCloudFile
import com.amaze.laputacloudslib.onedrive.OneDriveIOException
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException


class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    lateinit var uploadViewModel: UploadViewModel
    var uploadingSnackbar: Snackbar? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        val fab: FloatingActionButton = findViewById(R.id.fab)
        fab.setOnClickListener { view ->
            Snackbar.make(view, "Not implemented yet :(", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show()
        }
        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        val navView: NavigationView = findViewById(R.id.nav_view)
        val navController = findNavController(R.id.nav_host_fragment)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_home, R.id.nav_gdrive, R.id.nav_onedrive, R.id.nav_dropbox,
                R.id.nav_pdrive, R.id.nav_box
            ),
            drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        uploadViewModel = ViewModelProviders.of(this).get(UploadViewModel::class.java)

        uploadViewModel.events.observe(this, Observer<Event<UploadViewModel.UploadEvent>> {
            val parentLayout: View = findViewById(android.R.id.content)
            val content = it!!.peekContent()

            when(content.type) {
                UploadViewModel.UPLOAD_STARTED -> {
                    uploadingSnackbar = Snackbar.make(parentLayout, "Uploading...", Snackbar.LENGTH_INDEFINITE)
                    uploadingSnackbar!!.show()
                }
                UploadViewModel.UPLOAD_PROGRESS -> {

                    uploadingSnackbar = Snackbar.make(parentLayout, "Uploading... " + content.progress + "%", Snackbar.LENGTH_INDEFINITE)
                    uploadingSnackbar!!.show()
                }
                UploadViewModel.UPLOAD_ENDED -> {
                    uploadViewModel.selectingFileToUpload.value = false

                    uploadingSnackbar?.dismiss()
                    Snackbar.make(parentLayout, "Upload completed!", Snackbar.LENGTH_SHORT).show()

                    val action = MobileNavigationDirections.actionGlobalNavHome(false)
                    findNavController(R.id.nav_host_fragment).navigate(action)
                }
            }
        })

        uploadViewModel.folderLiveData.observe(this, Observer<AbstractCloudFile> { folder ->
            uploadViewModel.setUploadStarted()
            val size = uploadViewModel.fileToUpload!!.byteSize

            folder.uploadHere(uploadViewModel.fileToUpload!!, {bytesUploaded: Long ->
                uploadViewModel.setUploadProgressed(bytesUploaded / size.toFloat() * 100)
            }) {
                uploadViewModel.setUploadEnded()
            }
        })
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }
}
