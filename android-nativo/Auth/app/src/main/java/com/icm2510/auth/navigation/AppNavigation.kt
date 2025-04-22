package com.icm2510.auth.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.icm2510.auth.presentation.auth.AuthViewModel
import com.icm2510.auth.presentation.auth.LoginScreen
import com.icm2510.auth.presentation.home.HomeScreen

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    // Obtener una única instancia del ViewModel la cual sera en toda la navegación
    val authViewModel: AuthViewModel = viewModel()

    // Observar el estado del usuario para determinar la pantalla inicial
    val currentUser by authViewModel.currentUser.collectAsState()
    val startDestination = if (currentUser != null) "home" else "login"

    NavHost(navController = navController, startDestination = startDestination) {
        composable("login") {
            LoginScreen(
                authViewModel = authViewModel, // Pasar la instancia
                onLoginSuccess = {
                    // Navegar a home y limpiar el backstack hasta login
                    navController.navigate("home") {
                        popUpTo("login") { inclusive = true }
                        launchSingleTop = true // Evitar múltiples instancias de home
                    }
                }
            )
        }
        composable("home") {
            HomeScreen(
                authViewModel = authViewModel, // Pasar la misma instancia
                onSignedOut = {
                    // Navegar a login y limpiar el backstack hasta home
                    navController.navigate("login") {
                        popUpTo("home") { inclusive = true }
                        launchSingleTop = true // Para evitar múltiples instancias de login
                    }
                }
            )
        }
    }
}