package com.icm2510.camera.ui

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Environment
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import coil3.compose.AsyncImage
import com.icm2510.camera.R
import java.io.File
import java.text.SimpleDateFormat
import java.util.*


@Composable
fun CameraIntentScreen() {
    val context = LocalContext.current
    // Estado para almacenar la URI de la imagen (cámara o galería)
    var imageUri by remember { mutableStateOf<Uri?>(null) }

    // Estado para almacenar temporalmente la URI generada para la cámara
    var tempCameraUri by remember { mutableStateOf<Uri?>(null) }

    // Launcher para la Galería (PickVisualMedia)
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri: Uri? ->
            if (uri != null) {
                Log.d("PhotoPicker", "Selected URI: $uri")
                imageUri = uri // Actualiza la URI a mostrar
            } else {
                Log.d("PhotoPicker", "No media selected")
            }
        }
    )

    // Launcher para la Cámara (TakePicture)
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture(),
        onResult = { success: Boolean ->
            if (success) {
                Log.d("Camera", "Photo capture succeeded: $tempCameraUri")
                imageUri = tempCameraUri // Usa la URI temporal que se generó
            } else {
                Log.e("Camera", "Photo capture failed or was cancelled")
                // Aquí se puede limpiar tempCameraUri si falla
                // tempCameraUri = null
            }
        }
    )

    // Launcher para solicitar permiso de Cámara
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted: Boolean ->
            if (isGranted) {
                Log.d("Permission", "Camera permission Granted")
                // Si el permiso es otorgado, abrir la cámara
                val newUri = createImageUri(context)
                tempCameraUri = newUri // Guarda la URI antes de abrir
                if (newUri != null) {
                    cameraLauncher.launch(newUri)
                } else {
                    Log.e("Camera", "Failed to create URI for camera")
                    // Mostrar mensaje al usuario si falla la creación de URI
                }
            } else {
                Log.e("Permission", "Camera permission Denied")
                // Mostrar mensaje al usuario indicando que el permiso es necesario
            }
        }
    )

    // UI
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Muestra la imagen si hay una URI válida
        if (imageUri != null) {
            AsyncImage(
                model = imageUri,
                contentDescription = "Imagen seleccionada o capturada",
                modifier = Modifier
                    .size(250.dp) // Tamaño de ejemplo
                    .padding(bottom = 16.dp),
                contentScale = ContentScale.Crop // O ContentScale.Fit
            )
        } else {
            // Cargar una imegn desde el disco
            Image(
                painter = painterResource(id = R.drawable.image_placeholder),
                contentDescription = "No hay imagen seleccionada",
                modifier = Modifier
                    .size(250.dp) // Tamaño de ejemplo
                    .padding(bottom = 16.dp)
            )
            Text("No hay imagen seleccionada", modifier = Modifier.padding(bottom = 16.dp))
        }
        Row {
            // Botón para abrir la Cámara
            Button(
                onClick = {
                    // Verificar permiso antes de abrir
                    when (PackageManager.PERMISSION_GRANTED) {
                        ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) -> {
                            // Si el permiso ya fue otorgado, abrir cámara directamente
                            Log.d("Permission", "Camera permission already granted")
                            val newUri = createImageUri(context)
                            tempCameraUri = newUri // Guarda la URI antes de abrir
                            if (newUri != null) {
                                cameraLauncher.launch(newUri)
                            } else {
                                Log.e("Camera", "Failed to create URI for camera")
                                // Mostrar mensaje al usuario si falla la creación de URI
                            }
                        }

                        else -> {
                            // Solicitar permiso
                            Log.d("Permission", "Requesting camera permission")
                            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                        }
                    }
                },
                modifier = Modifier.width(180.dp)
            ) {
                Text("Tomar Foto")
                Icon(
                    painter = painterResource(id = R.drawable.ic_camera),
                    contentDescription = "Abrir Cámara",
                    modifier = Modifier.padding(start = 10.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Botón para abrir la Galería
            ElevatedButton(
                onClick = {
                    photoPickerLauncher.launch(
                        // Se seleccionab únicamente imágenes
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                },
                modifier = Modifier.width(180.dp)
            ) {
                Text("Seleccionar Foto")
                Icon(
                    painter = painterResource(id = R.drawable.ic_gallery),
                    contentDescription = "Abrir Cámara",
                    modifier = Modifier.padding(start = 10.dp)
                )
            }
        }
    }
}

/**
 * Función auxiliar para crear una URI segura usando FileProvider.
 */
private fun createImageUri(context: Context): Uri? {
    try {
        // Crear un nombre de archivo único basado en la fecha/hora
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val imageFileName = "JPEG_${timeStamp}_"

        // Obtener el directorio de imágenes específico de la app (recomendado)
        // Este directorio se asocia a la ruta definida en file_paths.xml ("Pictures")
        val storageDir: File? = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)

        if (storageDir == null) {
            Log.e("createImageUri", "ExternalFilesDir is null, cannot create image file.")
            return null
        }

        // Crear el archivo temporal
        val imageFile = File.createTempFile(
            imageFileName, /* prefijo */
            ".jpg",        /* sufijo */
            storageDir     /* directorio */
        )

        // Obtener la URI para el archivo usando FileProvider
        // La autoridad debe coincidir exactamente con la definida en AndroidManifest.xml
        val authority = "${context.packageName}.fileprovider"
        Log.d("createImageUri", "FileProvider Authority: $authority")
        Log.d("createImageUri", "Image file path: ${imageFile.absolutePath}")

        val uri = FileProvider.getUriForFile(
            Objects.requireNonNull(context),
            authority,
            imageFile
        )
        Log.d("createImageUri", "Generated URI: $uri")
        return uri

    } catch (e: Exception) {
        Log.e("createImageUri", "Error creating image URI", e)
        return null
    }
}