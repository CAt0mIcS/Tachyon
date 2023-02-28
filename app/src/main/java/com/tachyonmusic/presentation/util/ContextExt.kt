package com.tachyonmusic.presentation.util

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Binder

fun Context.hasUriPermission(uri: Uri) = checkUriPermission(
    uri,
    Binder.getCallingPid(),
    Binder.getCallingUid(),
    Intent.FLAG_GRANT_READ_URI_PERMISSION
) == PackageManager.PERMISSION_GRANTED
