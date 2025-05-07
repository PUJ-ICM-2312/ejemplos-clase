package com.icm2510.appfirebase.presentation.chat // Asegúrate que el paquete sea correcto

import android.util.Log
// import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObjects
import com.google.firebase.ktx.Firebase
import com.google.firebase.firestore.ktx.snapshots // Asegúrate que este import esté
import com.icm2510.appfirebase.data.model.Message
import com.icm2510.appfirebase.data.model.User // Importa tu modelo User
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.async // Importa async para búsquedas paralelas

import android.app.Application // Necesario para el contexto
import android.content.Context // Importar Context si se pasa directamente
import androidx.lifecycle.AndroidViewModel // Cambiar ViewModel a AndroidViewModel
import com.icm2510.appfirebase.core.NotificationUtils // Importa tu utilidad

// --- CAMBIO: Hereda de AndroidViewModel para acceder al Application Context ---
class ChatViewModel(application: Application) : AndroidViewModel(application) {
// --- FIN CAMBIO ---

    private val db: FirebaseFirestore = Firebase.firestore
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    // Flow para el texto actual en el campo de entrada
    private val _messageText = MutableStateFlow("")
    val messageText: StateFlow<String> = _messageText.asStateFlow()

    // Flow para la lista de mensajes (se actualiza en tiempo real)
    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages: StateFlow<List<Message>> = _messages.asStateFlow()

    // Flow para errores
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    // --- NUEVO: Estado para guardar detalles de usuarios ---
    private val _userDetails = MutableStateFlow<Map<String, User>>(emptyMap())
    val userDetails: StateFlow<Map<String, User>> = _userDetails.asStateFlow()
    // Guarda los IDs de los que ya intentamos buscar para no repetir
    private val fetchedUserIds = mutableSetOf<String>()
    // --- FIN NUEVO ---


    // --- NUEVO: Estado para saber si la pantalla de chat está activa ---
    private val _isChatScreenActive = MutableStateFlow(false)
    val isChatScreenActive: StateFlow<Boolean> = _isChatScreenActive.asStateFlow()

    fun setChatScreenActive(isActive: Boolean) {
        _isChatScreenActive.value = isActive
        if(isActive) {
            Log.d("ChatViewModel", "Chat screen is NOW ACTIVE")
        } else {
            Log.d("ChatViewModel", "Chat screen is NOW INACTIVE")
        }
    }
    // --- FIN NUEVO ---

    private var previousMessageCount = 0 // Para detectar nuevos mensajes

    init {
        listenForMessages()
    }


    // Actualiza el texto del mensaje
    fun onMessageTextChange(newText: String) {
        _messageText.value = newText
    }


    private fun listenForMessages() {
        db.collection("messages")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .limitToLast(100)
            .snapshots()
            .map { snapshot -> snapshot.toObjects<Message>() }
            .onEach { currentMessageList ->
                // --- LÓGICA PARA DETECTAR NUEVOS MENSAJES Y NOTIFICAR ---
                if (previousMessageCount > 0 && currentMessageList.size > previousMessageCount) {
                    // Hay nuevos mensajes desde la última actualización
                    val newMessages = currentMessageList.takeLast(currentMessageList.size - previousMessageCount)
                    newMessages.forEach { newMessage ->
                        // No notificar si el mensaje es del usuario actual o si el chat está activo
                        if (newMessage.senderId != auth.currentUser?.uid && !_isChatScreenActive.value) {
                            val senderDetails = _userDetails.value[newMessage.senderId]
                            val senderName = senderDetails?.name?.takeIf { it.isNotBlank() }
                                ?: senderDetails?.email
                                ?: "Someone" // Fallback

                            Log.d("ChatViewModel", "New message from $senderName, chat inactive. Showing notification.")
                            NotificationUtils.showNewMessageNotification(
                                getApplication<Application>().applicationContext, // Contexto de la aplicación
                                senderName,
                                newMessage.text
                            )
                        }
                    }
                }
                previousMessageCount = currentMessageList.size // Actualizar el contador
                // --- FIN LÓGICA DE NOTIFICACIÓN ---

                _messages.value = currentMessageList
                fetchUserDetailsIfNeeded(currentMessageList) // Esto ya lo tenías
            }
            .catch { exception -> /* ... (sin cambios) ... */ }
            .launchIn(viewModelScope)
    }

    // --- NUEVO: Función para buscar detalles de usuarios ---
    private fun fetchUserDetailsIfNeeded(messages: List<Message>) {
        val currentUserId = auth.currentUser?.uid
        // Obtiene los IDs de remitentes únicos que no son el usuario actual
        // y que no hemos intentado buscar antes.
        val senderIdsToFetch = messages
            .map { it.senderId }
            .distinct()
            .filter { it.isNotBlank() && it != currentUserId && !fetchedUserIds.contains(it) }
            .toSet()

        if (senderIdsToFetch.isNotEmpty()) {
            Log.d("ChatViewModel", "Fetching details for IDs: $senderIdsToFetch")
            // Marcamos estos IDs como "intentados"
            fetchedUserIds.addAll(senderIdsToFetch)

            viewModelScope.launch {
                senderIdsToFetch.forEach { userId ->
                    // Usamos async para que las búsquedas ocurran en paralelo
                    async {
                        try {
                            val userDoc = db.collection("users").document(userId).get().await()
                            val user = userDoc.toObject(User::class.java)
                            if (user != null) {
                                // Actualiza el mapa de detalles de usuario
                                _userDetails.value = _userDetails.value + (userId to user)
                                Log.d("ChatViewModel", "Details fetched for $userId: ${user.name}")
                            } else {
                                Log.w("ChatViewModel", "User document not found for ID: $userId")
                                // Opcional: Añadir un User placeholder para evitar reintentos
                                // _userDetails.value = _userDetails.value + (userId to User(userId = userId, name = "Unknown"))
                            }
                        } catch (e: Exception) {
                            Log.e("ChatViewModel", "Error fetching user details for $userId", e)
                            // Quitar de fetchedUserIds para permitir reintento más tarde
                            fetchedUserIds.remove(userId)
                        }
                    } // fin async
                } // fin forEach
            } // fin launch
        } // fin if
    }
    // --- FIN NUEVO ---


    // Envía un nuevo mensaje a Firestore
    fun sendMessage() {
        val textToSend = _messageText.value.trim()
        val currentUserId = auth.currentUser?.uid

        if (textToSend.isBlank()) {
            // Ya no asignamos a _error aquí si tenemos Snackbar en UI
            // _error.value = "Message cannot be empty."
            return
        }
        if (currentUserId == null) {
            // _error.value = "User not logged in. Cannot send message."
            return
        }

        _messageText.value = "" // Limpiar input
        // _error.value = null // Se maneja en la UI al mostrar Snackbar

        val newMessage = Message(
            text = textToSend,
            senderId = currentUserId
        )

        viewModelScope.launch {
            try {
                db.collection("messages").add(newMessage).await()
                Log.d("ChatViewModel", "Message sent successfully!")
            } catch (e: Exception) {
                Log.e("ChatViewModel", "Error sending message", e)
                _error.value = "Error sending message: ${e.message}" // Para el Snackbar
                // _messageText.value = textToSend // Opcional: Restaurar texto
            }
        }
    }

    fun clearError() {
        _error.value = null
    }
}