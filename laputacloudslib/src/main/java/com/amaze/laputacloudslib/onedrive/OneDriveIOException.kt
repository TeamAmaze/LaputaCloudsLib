package com.amaze.laputacloudslib.onedrive

import com.amaze.laputacloudslib.CloudException
import com.onedrive.sdk.core.ClientException

class OneDriveIOException(e: ClientException): CloudException(e)