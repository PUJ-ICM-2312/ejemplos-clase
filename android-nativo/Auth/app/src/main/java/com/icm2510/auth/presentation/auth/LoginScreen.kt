package com.icm2510.auth.presentation.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun LoginScreen(
    authViewModel: AuthViewModel = viewModel(), // Obtener instancia compartida
    onLoginSuccess: () -> Unit // Lambda para navegar al home
) {
    // Observar el estado del usuario. Si cambia a != null, navegar.
    val currentUser by authViewModel.currentUser.collectAsState()
    LaunchedEffect(currentUser) {
        if (currentUser != null) {
            onLoginSuccess()
        }
    }

    // Limpiar mensaje de feedback cuando la pantalla se recompone en el caso que ya no aplique
    DisposableEffect(authViewModel.feedbackMessage) {
        onDispose {
            // No limpiar aquí directamente para que el mensaje persista hasta la interacción
        }
    }


    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Bienvenido", style = MaterialTheme.typography.headlineLarge)
        Spacer(modifier = Modifier.height(48.dp))

        // Campo Email
        OutlinedTextField(
            value = authViewModel.email,
            onValueChange = { authViewModel.onEmailChange(it) },
            label = { Text("Correo Electrónico") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            singleLine = true,
            isError = authViewModel.feedbackMessage?.contains("correo", ignoreCase = true) == true // Resaltar si el error es de email
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Campo Contraseña
        OutlinedTextField(
            value = authViewModel.password,
            onValueChange = { authViewModel.onPasswordChange(it) },
            label = { Text("Contraseña") },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            singleLine = true,
            isError = authViewModel.feedbackMessage?.contains("contraseña", ignoreCase = true) == true // Resaltar si el error es de password
        )
        Spacer(modifier = Modifier.height(8.dp))

        // Mensaje de Feedback/Error
        Box(modifier = Modifier.height(24.dp).fillMaxWidth()) { // Contenedor para estabilizar layout
            authViewModel.feedbackMessage?.let { message ->
                Text(
                    text = message,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.align(Alignment.CenterStart)
                )
            }
        }
        Spacer(modifier = Modifier.height(24.dp))

        // Botón Iniciar Sesión
        Button(
            onClick = { authViewModel.signInUser() },
            enabled = !authViewModel.isLoading,
            modifier = Modifier.fillMaxWidth().height(48.dp)
        ) {
            if (authViewModel.isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
            } else {
                Text("Iniciar Sesión")
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        // Botón Registrarse
        OutlinedButton(
            onClick = { authViewModel.signUpUser() },
            enabled = !authViewModel.isLoading,
            modifier = Modifier.fillMaxWidth().height(48.dp)
        ) {
            if (authViewModel.isLoading) {
                Text("...") // Evitar ProgressIndicator duplicado si el "isLoading" es global
            } else {
                Text("Registrarse")
            }
        }
    }
}