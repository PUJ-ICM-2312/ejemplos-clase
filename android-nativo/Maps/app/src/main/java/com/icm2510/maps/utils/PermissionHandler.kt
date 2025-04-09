package com.icm2510.maps.utils

import android.Manifest
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale


@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun LocationPermissionHandler(onPermissionGranted: () -> Unit) {
    val context = LocalContext.current
    val permissionState = rememberPermissionState(Manifest.permission.ACCESS_FINE_LOCATION)

    LaunchedEffect(permissionState.status) {
        if (permissionState.status.isGranted) {
            onPermissionGranted()
        }
    }

    when {
        permissionState.status.isGranted -> {
            onPermissionGranted()
        }

        permissionState.status.shouldShowRationale -> {
            Toast.makeText(
                context,
                "Se necesita acceso a la ubicación para mostrar el mapa.",
                Toast.LENGTH_LONG
            ).show()
        }

        else -> {
            LaunchedEffect(Unit) {
                permissionState.launchPermissionRequest()
            }
        }
    }
}
