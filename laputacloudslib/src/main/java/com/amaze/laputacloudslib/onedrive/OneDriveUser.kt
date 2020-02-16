package com.amaze.laputacloudslib.onedrive

import com.amaze.laputacloudslib.AbstractUser
import com.onedrive.sdk.extensions.IOneDriveClient

class OneDriveUser(val oneDriveClient: IOneDriveClient) : AbstractUser<OneDriveDriver>() {
    override fun getFileStructureDriverAsync(callback: (OneDriveDriver) -> Unit) {
        callback(OneDriveDriver(oneDriveClient))
    }
}