package com.daton.mucify.user

import android.content.Context
import android.util.Log
import com.auth0.android.Auth0
import com.auth0.android.authentication.AuthenticationException
import com.auth0.android.callback.Callback
import com.auth0.android.management.ManagementException
import com.auth0.android.management.UsersAPIClient
import com.auth0.android.provider.WebAuthProvider
import com.auth0.android.result.Credentials
import com.auth0.android.result.UserProfile
import com.daton.mucify.R
import com.daton.mucify.ext.toMap
import com.daton.mucify.ui.ActivityMain
import kotlinx.serialization.json.Json
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import org.json.JSONObject
import java.io.File


object User {
    const val TAG = "User"

    private lateinit var appContext: Context

    /**
     * Auth0
     */
    private lateinit var account: Auth0
    private var cachedCredentials: Credentials? = null
    private var cachedUserProfile: UserProfile? = null

    lateinit var metadata: UserMetadata
        private set

    /**
     * Should be called in the first activity to initialize the user
     */
    fun create(context: Context) {
        appContext = context.applicationContext

        Auth0(
            context.getString(R.string.auth0_client_id),
            context.getString(R.string.com_auth0_domain)
        )

        metadata = UserMetadata(
            File(context.filesDir.absolutePath.toString() + "/Settings.txt"),
            System.currentTimeMillis()
        )
        metadata.loadFromLocal()
    }

    fun login() {
        WebAuthProvider
            .login(account)
            .withScheme(appContext.getString(R.string.com_auth0_scheme))
            .withScope(appContext.getString(R.string.auth0_login_scopes))
            .withAudience(
                appContext.getString(
                    R.string.auth0_login_audience,
                    appContext.getString(R.string.com_auth0_domain)
                )
            )
            .start(appContext, object : Callback<Credentials, AuthenticationException> {
                override fun onFailure(error: AuthenticationException) {
                    Log.e(ActivityMain.TAG, "Failed to authenticate with auth0 ${error.statusCode}")
                }

                override fun onSuccess(result: Credentials) {
                    cachedCredentials = result

                    syncUserSettings()

                    // TODO: When changing user settings local and remote metadata needs to be updated
                }

            })
    }

    fun logout() {
        WebAuthProvider
            .logout(account)
            .withScheme(appContext.getString(R.string.com_auth0_scheme))
            .start(appContext, object : Callback<Void?, AuthenticationException> {

                override fun onFailure(error: AuthenticationException) {
                    Log.e(ActivityMain.TAG, "Failed to log out ${error.statusCode}")
                }

                override fun onSuccess(result: Void?) {
                    cachedCredentials = null
                    cachedUserProfile = null
                }
            })
    }

    fun requestMetadata(onReady: (UserMetadata) -> Unit) {
        val usersClient = UsersAPIClient(account, cachedCredentials!!.accessToken)
        usersClient
            .getProfile(cachedUserProfile!!.getId()!!)
            .start(object : Callback<UserProfile, ManagementException> {

                override fun onFailure(error: ManagementException) {
                    Log.e(TAG, "Failed to get user metadata ${error.statusCode}")
                }

                override fun onSuccess(result: UserProfile) {
                    cachedUserProfile = result
                    // TODO: Is this fast?
                    onReady(
                        Json.decodeFromString(
                            JSONObject(result.getUserMetadata()).toString()
                        )
                    )
                }
            })
    }

    fun updateMetadata(newMetadata: UserMetadata = metadata) {
        val usersClient = UsersAPIClient(account, cachedCredentials!!.accessToken)
        val metadata = JSONObject(Json.encodeToString(newMetadata)).toMap()

        usersClient
            .updateMetadata(cachedUserProfile!!.getId()!!, metadata)
            .start(object : Callback<UserProfile, ManagementException> {

                override fun onFailure(error: ManagementException) {
                    Log.e(TAG, "Updating metadata failed ${error.statusCode}")
                }

                override fun onSuccess(result: UserProfile) {
                    cachedUserProfile = result
                    Log.d(TAG, "Successfully updated metadata")
                }

            })
    }

    /**
     * If the online settings are older than the offline ones, upload offline to online
     * If the offline settings are older than the online ones, download online to offline
     */
    private fun syncUserSettings() {
        // TODO: Conflicting settings
        //      Changing settings on Windows
        //      Changing settings on Android without synchronizing
        //      Going back online and trying to synchronize
        //  Which settings to use?

        requestMetadata { remoteMetadata ->
            // Offline is newer than online --> Upload offline
            if (remoteMetadata.timestamp < metadata.timestamp) {
                updateMetadata(metadata)
            }
            // Online is newer than offline --> Download online
            else if (remoteMetadata.timestamp > metadata.timestamp) {
                metadata = remoteMetadata
                metadata.saveToLocal()
            }
        }
    }
}