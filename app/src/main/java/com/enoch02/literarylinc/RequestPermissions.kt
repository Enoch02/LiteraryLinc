package com.enoch02.literarylinc

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

@Composable
fun RequestNotificationPermission() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        val context = LocalContext.current
        val activity = LocalContext.current as? Activity
        val permission = Manifest.permission.POST_NOTIFICATIONS
        val permissionState = remember { mutableStateOf(false) }

        // Check if the permission is already granted
        if (ContextCompat.checkSelfPermission(
                context,
                permission
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            permissionState.value = true
        }

        // If not granted, check if we should request the permission
        if (!permissionState.value && activity != null) {
            // Only request the permission if the user hasn't denied it permanently
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)) {
                // User denied permission before, do not ask again
                return
            }

            // Request permission
            LaunchedEffect(Unit) {
                ActivityCompat.requestPermissions(activity, arrayOf(permission), 0)
            }
        }
    }
}