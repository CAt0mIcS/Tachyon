package com.daton.mucify.user

import android.content.Context
import android.util.Log
import com.auth0.android.Auth0
import com.auth0.android.authentication.AuthenticationAPIClient
import com.auth0.android.authentication.AuthenticationException
import com.auth0.android.callback.Callback
import com.auth0.android.management.ManagementException
import com.auth0.android.management.UsersAPIClient
import com.auth0.android.provider.WebAuthProvider
import com.auth0.android.result.Credentials
import com.auth0.android.result.UserProfile
import com.daton.mucify.R
import com.daton.mucify.ext.toMap
import kotlinx.serialization.json.Json
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import org.json.JSONObject
import java.io.File


object User {
    const val TAG = "User"
    lateinit var settingsFile: File
        private set

    /**
     * Auth0
     */
    private lateinit var account: Auth0
    private var cachedCredentials: Credentials? = null
    private var cachedUserProfile: UserProfile? = null

    private var onLogin: (() -> Unit)? = null

    var metadata = UserMetadata()
        private set

    /**
     * Should be called in the first activity to initialize the user
     */
    fun create(context: Context) {
        settingsFile = File(context.filesDir.absolutePath.toString() + "/Settings.txt")

        account = Auth0(
            context.getString(R.string.auth0_client_id),
            context.getString(R.string.com_auth0_domain)
        )

        metadata = metadata.loadFromLocal()
    }

    fun onLogin(onLogin: () -> Unit) {
        // User already logged in
        if (cachedCredentials != null)
            onLogin()
        else
            this.onLogin = onLogin
    }

    fun login(context: Context) {
        WebAuthProvider
            .login(account)
            .withScheme(context.getString(R.string.com_auth0_scheme))
            .withScope(context.getString(R.string.auth0_login_scopes))
            .withAudience(
                context.getString(
                    R.string.auth0_login_audience,
                    context.getString(R.string.com_auth0_domain)
                )
            )
            .start(context, object : Callback<Credentials, AuthenticationException> {
                override fun onFailure(error: AuthenticationException) {
                    Log.e(TAG, "Failed to authenticate with auth0 ${error.statusCode}")
                }

                override fun onSuccess(result: Credentials) {
                    cachedCredentials = result

                    updateUserProfile {
                        syncUserSettings()
                        onLogin?.invoke()
                    }

                    // TODO: When changing user settings local and remote metadata needs to be updated
                }

            })
    }

    fun logout(context: Context) {
        WebAuthProvider
            .logout(account)
            .withScheme(context.getString(R.string.com_auth0_scheme))
            .start(context, object : Callback<Void?, AuthenticationException> {

                override fun onFailure(error: AuthenticationException) {
                    Log.e(TAG, "Failed to log out ${error.statusCode}")
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

    fun uploadMetadata(newMetadata: UserMetadata = metadata) {
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
                uploadMetadata(metadata)
            }
            // Online is newer than offline --> Download online
            else if (remoteMetadata.timestamp > metadata.timestamp) {
                metadata = remoteMetadata
                metadata.saveToLocal()
            }
        }
    }

    /**
     * Fetches user profile data which is cached to request metadata
     */
    private fun updateUserProfile(onUpdated: () -> Unit) {
        val client = AuthenticationAPIClient(account)
        client
            .userInfo(cachedCredentials!!.accessToken)
            .start(object : Callback<UserProfile, AuthenticationException> {

                override fun onFailure(error: AuthenticationException) {
                    Log.e(TAG, "Failed to get user profile ${error.statusCode}")
                }

                override fun onSuccess(result: UserProfile) {
                    cachedUserProfile = result
                    onUpdated()
                }
            })
    }
}