package com.icm2510.appfirebase.presentation.auth // Asegúrate que el paquete sea correcto

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.icm2510.appfirebase.R // Asegúrate de tener un icono/placeholder en drawable

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    registerViewModel: RegisterViewModel = viewModel(),
    onRegisterSuccess: () -> Unit,
    onNavigateBack: () -> Unit
) {
    val uiState by registerViewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    // Efecto para navegar cuando el registro sea exitoso
    LaunchedEffect(key1 = uiState.registrationSuccess) {
        if (uiState.registrationSuccess) {
            onRegisterSuccess()
        }
    }

    // Launcher para seleccionar imagen
    val pickMediaLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            registerViewModel.onImageSelected(uri)
        }
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Register") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()), // Para pantallas pequeñas
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            // Selector de Imagen
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .border(1.dp, MaterialTheme.colorScheme.primary, CircleShape)
                    .clickable {
                        pickMediaLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                if (uiState.selectedImageUri != null) {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(uiState.selectedImageUri)
                            .crossfade(true)
                            .build(),
                        contentDescription = "Selected profile picture",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    // Puedes poner un icono o texto placeholder
                    Image(
                        painter = painterResource(id = R.drawable.ic_launcher_foreground), // Reemplaza con tu placeholder
                        contentDescription = "Select profile picture",
                        modifier = Modifier.size(60.dp),
                        alpha = 0.5f
                    )
                }
            }

            // Campos de Texto
            OutlinedTextField(
                value = uiState.name,
                onValueChange = registerViewModel::onNameChange,
                label = { Text("Name") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            OutlinedTextField(
                value = uiState.phone,
                onValueChange = registerViewModel::onPhoneChange,
                label = { Text("Phone") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                singleLine = true
            )
            OutlinedTextField(
                value = uiState.email,
                onValueChange = registerViewModel::onEmailChange,
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                singleLine = true
            )
            OutlinedTextField(
                value = uiState.password,
                onValueChange = registerViewModel::onPasswordChange,
                label = { Text("Password") },
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                singleLine = true
            )
            OutlinedTextField(
                value = uiState.confirmPassword,
                onValueChange = registerViewModel::onConfirmPasswordChange,
                label = { Text("Confirm Password") },
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                singleLine = true
            )

            // Botón de Registro y Mensaje de Error/Carga
            Spacer(modifier = Modifier.height(8.dp))

            if (uiState.isLoading) {
                CircularProgressIndicator()
            } else {
                Button(
                    onClick = { registerViewModel.registerUser() },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !uiState.isLoading // Deshabilitar si está cargando
                ) {
                    Text("Register")
                }
            }

            uiState.error?.let { error ->
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = 8.dp)
                )
                // Limpiar el error después de un tiempo o al cambiar campos
                LaunchedEffect(error) {
                    kotlinx.coroutines.delay(3000)
                    registerViewModel.clearError()
                }
            }
        }
    }
}