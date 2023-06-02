package com.ozcanalasalvar.camerax.utils

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat


object Permissions {
    var requestList = arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO)


    public fun isPermissionTaken(activity: Activity): Boolean {
        return requestList.all {
                isPermissionGranted(activity, it)
        }
    }

    private fun isPermissionGranted(context: Context, permission: String): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            permission,
        ) == PackageManager.PERMISSION_GRANTED
    }


}