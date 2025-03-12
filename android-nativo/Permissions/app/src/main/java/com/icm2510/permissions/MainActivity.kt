package com.icm2510.permissions

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.MaterialTheme
import com.google.accompanist.permissions.ExperimentalPermissionsApi

@ExperimentalPermissionsApi
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MaterialTheme {
                // Solicitar un único permiso
                RequestPermission(permission = Manifest.permission.READ_CONTACTS)
                // Solicitar múltiples permisos
//                RequestMultiplePermissions(
//                    permissions = listOf(
//                        Manifest.permission.READ_CONTACTS,
//                        Manifest.permission.CAMERA
//                    )
//                )
            }
        }
    }
}