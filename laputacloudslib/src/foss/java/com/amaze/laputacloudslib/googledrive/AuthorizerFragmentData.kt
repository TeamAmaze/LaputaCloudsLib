package com.amaze.laputacloudslib.googledrive

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
class AuthorizerFragmentData(val clientId: String, val redirectUrl: String) : Parcelable