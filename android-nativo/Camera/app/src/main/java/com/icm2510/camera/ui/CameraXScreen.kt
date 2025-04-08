
// CameraXScreen.kt

package com.icm2510.camera.ui

import android.Manifest
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.provider.MediaStore
import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.icm2510.camera.R
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.Executor

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun CameraXScreen() {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // Estado del permiso de la cámara
    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)

    // Efecto para solicitar el permiso de la cámara al entrar en la pantalla
    LaunchedEffect(Unit) {
        cameraPermissionState.launchPermissionRequest()
    }

    // Si el permiso no está concedido, mostrar un mensaje
    if (!cameraPermissionState.status.isGranted) {
        Text("Se necesita permiso de la cámara para continuar.")
        return
    }

    // Configuración de CameraX
    val previewView = remember { PreviewView(context) }
    val imageCapture = remember { ImageCapture.Builder().build() }
    val cameraExecutor = remember { ContextCompat.getMainExecutor(context) }

    // Inicializar CameraX
    LaunchedEffect(previewView) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            // Configurar la vista de previsualización
            val preview = androidx.camera.core.Preview.Builder().build().also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }

            // Seleccionar la cámara trasera
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                // Limpiar cualquier caso de uso anterior
                cameraProvider.unbindAll()

                // Vincular la cámara al ciclo de vida
                cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    cameraSelector,
                    preview,
                    imageCapture
                )
            } catch (e: Exception) {
                Log.e("CameraXScreen", "Error al inicializar CameraX", e)
            }
        }, cameraExecutor)
    }

    // Función para capturar una foto y guardarla en la galería
    fun takePhoto(executor: Executor) {
        val photoFile = File.createTempFile(
            "photo_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(System.currentTimeMillis())}",
            ".jpg",
            context.externalCacheDir
        )

        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        imageCapture.takePicture(
            outputOptions,
            executor,
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    val bitmap = BitmapFactory.decodeFile(photoFile.absolutePath)
                    saveImageToGallery(context, bitmap)
                }

                override fun onError(exc: ImageCaptureException) {
                    Log.e("CameraXScreen", "Error al capturar la foto", exc)
                }
            }
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Visor de la cámara
        AndroidView(
            factory = { previewView },
            modifier = Modifier.fillMaxSize()
        )

        // Botón flotante para tomar foto
        FloatingActionButton(
            onClick = { takePhoto(cameraExecutor) },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
                .size(56.dp),
            shape = CircleShape
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_camera),
                contentDescription = "Tomar foto"
            )
        }
    }
}

// Función para guardar la imagen en la galería
private fun saveImageToGallery(context: Context, bitmap: Bitmap) {
    val imageUri = MediaStore.Images.Media.insertImage(
        context.contentResolver,
        bitmap,
        "Photo_${System.currentTimeMillis()}",
        "Foto tomada con CameraX"
    )

    if (imageUri != null) {
        Log.d("CameraXScreen", "Imagen guardada en la galería: $imageUri")
    } else {
        Log.e("CameraXScreen", "Error al guardar la imagen en la galería")
    }
}
