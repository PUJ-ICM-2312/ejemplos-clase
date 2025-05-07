package com.icm2510.appfirebase.navigation // Asegúrate que el paquete sea correcto

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navigation
import com.icm2510.appfirebase.presentation.auth.AuthViewModel // Cambia el paquete si es necesario
import com.icm2510.appfirebase.presentation.auth.LoginScreen // Cambia el paquete si es necesario
// Importa las nuevas pantallas cuando las creemos
// import com.icm2510.appfirebase.presentation.auth.RegisterScreen
// import com.icm2510.appfirebase.presentation.chat.ChatScreen
import com.icm2510.appfirebase.presentation.home.HomeScreen // Cambia el paquete si es necesario
import com.icm2510.appfirebase.presentation.auth.RegisterScreen
import com.icm2510.appfirebase.presentation.chat.ChatScreen // Importa la pantalla



@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController(),
    authViewModel: AuthViewModel = viewModel() // Obtiene el AuthViewModel
) {
    // Observa el estado del usuario actual desde AuthViewModel
    val currentUser by authViewModel.currentUser.collectAsState()

    // Determina el grafo de inicio basado en si el usuario está logueado
    val startDestination = if (currentUser == null) Graph.AUTH else Graph.MAIN

    NavHost(
        navController = navController,
        startDestination = startDestination // Inicia en el grafo AUTH o MAIN
    ) {
        // Grafo de Autenticación (Pantallas que NO requieren login)
        navigation(
            startDestination = AppScreen.Login.route,
            route = Graph.AUTH // Nombre/ruta de este grafo anidado
        ) {
            // Pantalla de Login
            composable(route = AppScreen.Login.route) {
                LoginScreen(
                    authViewModel = authViewModel, // Pasa el mismo ViewModel
                    onLoginSuccess = {
                        // Navega al grafo principal y limpia el backstack de autenticación
                        navController.navigate(Graph.MAIN) {
                            popUpTo(Graph.AUTH) { inclusive = true }
                            launchSingleTop = true
                        }
                    },
                    onNavigateToRegister = {
                        // Navega a la pantalla de registro dentro del mismo grafo de Auth
                        navController.navigate(AppScreen.Register.route)
                    }
                )
            }

            // Pantalla de Registro
            composable(route = AppScreen.Register.route) {
                RegisterScreen(
                    onRegisterSuccess = {
                        // Al registrarse exitosamente (que también loguea), ir al grafo principal
                        navController.navigate(Graph.MAIN) {
                            popUpTo(Graph.AUTH) { inclusive = true }
                            launchSingleTop = true
                        }
                    },
                    onNavigateBack = { navController.popBackStack() } // Vuelve a la pantalla anterior (Login)
                )
            }
        }

        // Grafo Principal (Pantallas que SÍ requieren login)
        navigation(
            startDestination = AppScreen.Home.route,
            route = Graph.MAIN // Nombre/ruta de este grafo anidado
        ) {
            // Pantalla Home
            composable(route = AppScreen.Home.route) {
                HomeScreen(
                    authViewModel = authViewModel, // Pasa el mismo ViewModel
                    onLogout = {
                        // Al hacer logout, navega al grafo de autenticación
                        navController.navigate(Graph.AUTH) {
                            popUpTo(Graph.MAIN) { inclusive = true }
                            launchSingleTop = true
                        }
                    },
                    onNavigateToChat = {
                        // Navega a la pantalla de chat dentro del grafo principal
                        navController.navigate(AppScreen.Chat.route)
                    }
                )
            }

            /// Pantalla de Chat
            composable(route = AppScreen.Chat.route) {
                // Descomentado y actualizado:
                ChatScreen(
                    onNavigateBack = { navController.popBackStack() } // Para volver a Home
                )
            }
        }
    }
}