package com.icm2510.appfirebase.data.model // Asegúrate que el paquete sea correcto

import com.google.firebase.firestore.ServerTimestamp
import java.util.Date // Usamos Date con @ServerTimestamp

data class Message(
    val id: String = "", // Firestore asignará un ID si usamos .add()
    val text: String = "",
    val senderId: String = "", // UID del usuario que envía
    @ServerTimestamp // Firestore asignará la hora del servidor automáticamente
    val timestamp: Date? = null // Firestore convertirá esto a Timestamp internamente
) {
    // Constructor sin argumentos requerido por Firestore para toObjects()
    constructor() : this("", "", "", null)
}