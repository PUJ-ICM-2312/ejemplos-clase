// CameraIntentScreen.kt

package com.icm2510.camera.ui

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.icm2510.camera.R

@SuppressLint("ContextCastToActivity")
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun CameraIntentScreen() {
    var imageBitmap by remember { mutableStateOf<ImageBitmap?>(null) }
    val context = LocalContext.current as Activity

    // Estado del permiso de la cámara
    val cameraPermissionState = rememberPermissionState(
        android.Manifest.permission.CAMERA
    )

    // Lanzador para tomar una foto
    val takePictureLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val bitmap = result.data?.extras?.get("data") as Bitmap
            imageBitmap = bitmap.asImageBitmap()
        }
    }

    // Lanzador para seleccionar una imagen de la galería
    val pickImageLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            val bitmap = MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
            imageBitmap = bitmap.asImageBitmap()
        }
    }

    // Efecto para solicitar el permiso de la cámara al entrar en la pantalla
    LaunchedEffect(Unit) {
        cameraPermissionState.launchPermissionRequest()
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        imageBitmap?.let {
            Image(
                bitmap = it,
                contentDescription = null,
                modifier = Modifier.size(300.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(modifier = Modifier.padding(16.dp)) {
            Button(onClick = {
                if (cameraPermissionState.status.isGranted) { // Usar status.isGranted
                    val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                    takePictureLauncher.launch(intent)
                } else {
                    // Si no tiene permiso, solicítalo nuevamente
                    cameraPermissionState.launchPermissionRequest()
                }
            }) {
                Icon(painter = painterResource(id = R.drawable.ic_camera), contentDescription = "Take Photo")
                Text(text = "Tomar foto")
            }

            Spacer(modifier = Modifier.width(16.dp))

            Button(onClick = {
                pickImageLauncher.launch("image/*")
            }) {
                Icon(painter = painterResource(id = R.drawable.ic_gallery), contentDescription = "Gallery")
                Text(text = "Galería")
            }
        }

        // Mostrar un mensaje si el permiso no está concedido
        if (!cameraPermissionState.status.isGranted) { // Usar status.isGranted
            Text(text = "Se necesita permiso de la cámara para tomar fotos.")
        }
    }
}