package com.icm2510.appfirebase

import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.icm2510.appfirebase.navigation.AppNavigation
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import android.content.pm.PackageManager
import android.Manifest // Para el permiso
import android.content.Context


// Definir un ID para tu canal de notificación
const val CHAT_NOTIFICATION_CHANNEL_ID = "chat_notification_channel"

class MainActivity : ComponentActivity() {

    // --- NUEVO: Launcher para solicitar permiso de notificación ---
    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                // Permiso concedido, puedes mostrar notificaciones
                println("Notification permission granted.")
            } else {
                // Permiso denegado, informa al usuario o deshabilita la funcionalidad
                println("Notification permission denied.")
                // Podrías mostrar un Snackbar indicando que las notificaciones no funcionarán
            }
        }
    // --- FIN NUEVO ---

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // --- NUEVO: Crear canal de notificación ---
        createChatNotificationChannel()
        // --- FIN NUEVO ---

        // --- NUEVO: Solicitar permiso en Android 13+ ---
        askNotificationPermission()
        // --- FIN NUEVO ---

        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation()
                }
            }
        }
    }


    // --- NUEVO: Función para crear el canal ---
    private fun createChatNotificationChannel() {
        // Crear el NotificationChannel, pero solo en API 26+ porque
        // NotificationChannel es nuevo y no es necesario en versiones anteriores.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Chat Messages" // Nombre visible para el usuario
            val descriptionText = "Notifications for new chat messages"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHAT_NOTIFICATION_CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            // Registrar el canal con el sistema
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
            println("Notification channel created.")
        }
    }
    // --- FIN NUEVO ---

    // --- NUEVO: Función para solicitar permiso ---
    private fun askNotificationPermission() {
        // Esto solo es necesario para API 33+ (Android 13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) ==
                PackageManager.PERMISSION_GRANTED
            ) {
                // Ya tienes el permiso
                println("Notification permission already granted.")
            } else if (shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)) {
                // Opcional: Muestra una UI explicando por qué necesitas el permiso.
                // Por ahora, solo lo pedimos directamente.
                println("Showing rationale for notification permission.") // Puedes quitar esto
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            } else {
                // Pide el permiso directamente
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
    // --- FIN NUEVO ---
}