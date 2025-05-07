package com.icm2510.appfirebase.presentation.home // Asegúrate que el paquete sea correcto

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.icm2510.appfirebase.presentation.auth.AuthViewModel // Asegúrate que el paquete sea correcto

// --- ¡MODIFICACIÓN IMPORTANTE! Añade los parámetros lambda aquí ---
@Composable
fun HomeScreen(
    authViewModel: AuthViewModel,
    onLogout: () -> Unit,          // Parámetro para la acción de Logout
    onNavigateToChat: () -> Unit   // Parámetro para navegar al Chat
) {
    val currentUser by authViewModel.currentUser.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Welcome!",
            style = MaterialTheme.typography.headlineMedium
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "You are logged in as: ${currentUser?.email ?: "Unknown User"}",
            style = MaterialTheme.typography.bodyLarge
        )
        Spacer(modifier = Modifier.height(32.dp))

        // --- Botón para ir al Chat ---
        Button(
            onClick = onNavigateToChat, // Llama al lambda para navegar al chat
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Go to Chat")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // --- Botón de Logout ---
        Button(
            onClick = {
                // Primero llama al logout del ViewModel
                authViewModel.signOut()
                // Luego llama al lambda para manejar la navegación (proporcionado por AppNavigation)
                onLogout()
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error) // Color rojo para logout
        ) {
            Text("Logout")
        }
    }
}