package com.amaze.laputacloudslib.googledrive

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
class AuthorizerFragmentData(val clientId: String, val redirectUrl: String) : Parcelable