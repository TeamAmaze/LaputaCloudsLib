package com.amaze.laputacloudslib.googledrive

import java.util.*

object GoogleDriveAuth {
    val RESPONSE_TYPE = "code"

    /**
     * Full, permissive scope to access all of a user's files, excluding the Application Data folder.
     * See https://developers.google.com/drive/api/v3/about-auth
     */
    val SCOPE = "https://www.googleapis.com/auth/drive"

    /**
     * Per OAuth2 specs, auth code exchange should include a state token for CSRF validation
     *
     * @return a randomly generated String to use as a state token
     */
    fun generateStateToken(): String {
        return UUID.randomUUID().toString()
    }
}