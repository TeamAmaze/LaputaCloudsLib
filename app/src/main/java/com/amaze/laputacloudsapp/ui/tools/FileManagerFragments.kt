package com.amaze.laputacloudsapp.ui.tools

import com.amaze.laputacloudsapp.appfolder.PhoneAccount
import com.amaze.laputacloudsapp.appfolder.PhoneDriver
import com.amaze.laputacloudsapp.appfolder.PhoneFile
import com.amaze.laputacloudsapp.appfolder.PhonePath
import com.amaze.laputacloudslib.box.BoxAccount
import com.amaze.laputacloudslib.box.BoxDriver
import com.amaze.laputacloudslib.box.BoxFile
import com.amaze.laputacloudslib.box.BoxPath
import com.amaze.laputacloudslib.dropbox.DropBoxAccount
import com.amaze.laputacloudslib.dropbox.DropBoxDriver
import com.amaze.laputacloudslib.dropbox.DropBoxFile
import com.amaze.laputacloudslib.dropbox.DropBoxPath
import com.amaze.laputacloudslib.googledrive.GoogleAccount
import com.amaze.laputacloudslib.googledrive.GoogleDriveDriver
import com.amaze.laputacloudslib.googledrive.GoogleDriveFile
import com.amaze.laputacloudslib.googledrive.GoogleDrivePath
import com.amaze.laputacloudslib.onedrive.OneDriveAccount
import com.amaze.laputacloudslib.onedrive.OneDriveCloudFile
import com.amaze.laputacloudslib.onedrive.OneDriveDriver
import com.amaze.laputacloudslib.onedrive.OneDrivePath

class PhoneFileManagerFragment: FileManagerFragment<PhonePath, PhoneFile, PhoneDriver, PhoneAccount>()
class GoogleDriveFileManagerFragment: FileManagerFragment<GoogleDrivePath, GoogleDriveFile, GoogleDriveDriver, GoogleAccount>()
class OneDriveFileManagerFragment: FileManagerFragment<OneDrivePath, OneDriveCloudFile, OneDriveDriver, OneDriveAccount>()
class DropBoxFileManagerFragment: FileManagerFragment<DropBoxPath, DropBoxFile, DropBoxDriver, DropBoxAccount>()
class BoxFileManagerFragment: FileManagerFragment<BoxPath, BoxFile, BoxDriver, BoxAccount>()