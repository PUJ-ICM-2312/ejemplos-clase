package com.icm2510.appfirebase.core // Asegúrate que el paquete sea correcto

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.icm2510.appfirebase.MainActivity // Importa tu Activity principal
import com.icm2510.appfirebase.R // Para el ícono de notificación
import com.icm2510.appfirebase.CHAT_NOTIFICATION_CHANNEL_ID // Importa el ID del canal

object NotificationUtils {

    private var lastNotificationId = 1000 // Para generar IDs únicos para cada notificación

    // Basado en Diapositiva 7 y 8 del PDF
    fun showNewMessageNotification(
        context: Context,
        senderName: String,
        messageText: String,
        // Opcional: podrías pasar un ID de conversación para agrupar o navegar
    ) {
        // Crear un Intent para abrir MainActivity al hacer clic en la notificación
        // (Diapositiva 8)
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            // Opcional: Añadir extras para saber que se abrió desde una notificación de chat
            putExtra("notification_action", "open_chat")
            // putExtra("chat_id", conversationId) // Si tuvieras IDs de conversación
        }
        val pendingIntent: PendingIntent = PendingIntent.getActivity(
            context,
            0, // Puedes usar un requestCode único si tienes varios PendingIntents
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT // Recomendado
        )

        val notificationId = lastNotificationId++ // ID único para esta notificación

        // Construir la notificación (Diapositiva 7)
        val builder = NotificationCompat.Builder(context, CHAT_NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground) // ¡REEMPLAZA con tu ícono de notificación!
            .setContentTitle(senderName) // Nombre del remitente
            .setContentText(messageText) // Contenido del mensaje
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent) // Acción al hacer clic (Diapositiva 8)
            .setAutoCancel(true) // La notificación se cierra al hacer clic (Diapositiva 8)
        // Opcional: Añadir vibración, sonido, etc.
        // .setDefaults(Notification.DEFAULT_ALL)

        // Mostrar la notificación (Diapositiva 9)
        // El permiso ya se verifica/solicita en MainActivity
        with(NotificationManagerCompat.from(context)) {
            try {
                notify(notificationId, builder.build())
                println("Notification shown with ID: $notificationId")
            } catch (e: SecurityException) {
                // Esto podría pasar si el permiso es revocado después de la verificación inicial
                println("SecurityException: Could not show notification. Permission may be missing. ${e.message}")
            }
        }
    }
}