package com.icm2510.appfirebase.data.model // Asegúrate que el paquete sea correcto

import com.google.firebase.firestore.ServerTimestamp // Opcional para timestamp
import java.util.Date // Opcional para timestamp

data class User(
    val userId: String = "", // ID del usuario (será el UID de Firebase Auth)
    val name: String = "",
    val email: String = "", // Guardamos el email también aquí para facilidad de consulta
    val phone: String = "",
    val profilePictureUrl: String? = null, // URL de la foto en Firebase Storage (puede ser null)

    // Opcional: añadir un timestamp de cuándo se creó el usuario en Firestore
    @ServerTimestamp
    val createdAt: Date? = null
)