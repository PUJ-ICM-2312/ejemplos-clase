package com.icm2510.appfirebase.navigation // Asegúrate que el paquete sea correcto

// Define las rutas para los grafos de navegación
object Graph {
    const val AUTH = "auth_graph"
    const val MAIN = "main_graph"
}

// Define las pantallas individuales
sealed class AppScreen(val route: String) {
    object Login : AppScreen("login_screen")
    object Register : AppScreen("register_screen") // Nueva pantalla
    object Home : AppScreen("home_screen")
    object Chat : AppScreen("chat_screen") // Nueva pantalla
}
