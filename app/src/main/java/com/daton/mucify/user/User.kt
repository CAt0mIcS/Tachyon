package com.daton.mucify.user

import android.content.Context
import android.util.Log
import com.auth0.android.Auth0
import com.auth0.android.authentication.AuthenticationException
import com.auth0.android.callback.Callback
import com.auth0.android.provider.WebAuthProvider
import com.auth0.android.result.Credentials
import com.auth0.android.result.UserProfile
import com.daton.mucify.R
import com.daton.mucify.ui.ActivityMain

object User {
    /**
     * Should be called in the first activity to initialize the user
     */
    fun create(appContext: Context) {
        this.appContext = appContext

        Auth0(
            appContext.getString(R.string.auth0_client_id),
            appContext.getString(R.string.com_auth0_domain)
        )
    }

    private lateinit var appContext: Context

    /**
     * Auth0
     */
    private lateinit var account: Auth0
    private var cachedCredentials: Credentials? = null
    private var cachedUserProfile: UserProfile? = null

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

                    // TODO: Download user settings from user metadata and store them locally
                    // TODO: in case the client goes offline

                    // TODO: Check if remote settings are older than local settings and update accordingly

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
}