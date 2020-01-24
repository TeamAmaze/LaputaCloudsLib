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
import com.amaze.laputacloudslib.OneDriveIOException
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

        uploadViewModel.events.observe(this, Observer<Event<String>> {
            val parentLayout: View = findViewById(android.R.id.content)

            when(it!!.peekContent()) {
                UploadViewModel.UPLOAD_STARTED -> {
                    uploadingSnackbar = Snackbar.make(parentLayout, "Uploading...", Snackbar.LENGTH_INDEFINITE)
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
            UploadFileTask(uploadViewModel, folder!!, uploadViewModel.fileToUpload!!).execute()
        })
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    class UploadFileTask(
        val uploadViewModel: UploadViewModel,
        val folder: AbstractCloudFile,
        val fileToUpload: AbstractCloudFile
    ) : AsyncTask<Unit, Unit, AbstractCloudFile>() {

        override fun doInBackground(vararg params: Unit?): AbstractCloudFile {
            val handler = CoroutineExceptionHandler { _, exception ->
                throw exception
            }
            return runBlocking(handler) {
                suspendCancellableCoroutine<AbstractCloudFile> { cont ->
                    try {
                        folder.uploadHere(fileToUpload) {
                            cont.resume(it)
                        }
                    } catch (e: OneDriveIOException) {
                        cont.resumeWithException(e)
                    }
                }
            }
        }

        override fun onPostExecute(result: AbstractCloudFile) {
            uploadViewModel.setUploadEnded()
        }
    }
}
