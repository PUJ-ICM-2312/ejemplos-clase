package com.icm2510.auth.presentation.home

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.icm2510.auth.presentation.auth.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class) // Para TopAppBar y Scaffold
@Composable
fun HomeScreen(
    authViewModel: AuthViewModel = viewModel(), // Obtener instancia compartida
    onSignedOut: () -> Unit // Lambda para navegar al login
) {
    // Observar el estado del usuario. Si cambia a null, navegar.
    val currentUser by authViewModel.currentUser.collectAsState()
    LaunchedEffect(currentUser) {
        if (currentUser == null) {
            onSignedOut()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Pantalla Principal") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer, // Color de fondo
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer // Color del título
                ),
                actions = {
                    IconButton(onClick = { authViewModel.signOut() }) { // Llama a signOut
                        Icon(
                            imageVector = Icons.Filled.ExitToApp,
                            contentDescription = "Cerrar Sesión",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer // Color del icono
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues) // Aplicar padding del Scaffold
                .padding(16.dp), // Padding adicional interno
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("¡Bienvenido!", style = MaterialTheme.typography.headlineMedium)
            Spacer(modifier = Modifier.height(16.dp))
            currentUser?.email?.let { email ->
                Text("Sesión iniciada como:", style = MaterialTheme.typography.bodyLarge)
                Text(email, style = MaterialTheme.typography.bodyLarge, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
            }
            // Aquí iría el resto del contenido de tu aplicación principal
        }
    }
}