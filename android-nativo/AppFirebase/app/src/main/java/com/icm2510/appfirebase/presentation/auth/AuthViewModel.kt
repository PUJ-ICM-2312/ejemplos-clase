package com.icm2510.appfirebase.presentation.auth

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await // Para usar await() en lugar de listeners

class AuthViewModel : ViewModel() {

    private val auth: FirebaseAuth = Firebase.auth

    // Expone el usuario actual (null si no está en sesión)
    val currentUser: StateFlow<FirebaseUser?> = auth.authStateFlow()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = auth.currentUser
        )

    // Estados para UI
    var email by mutableStateOf("")
        private set
    var password by mutableStateOf("")
        private set
    var isLoading by mutableStateOf(false)
        private set
    var feedbackMessage by mutableStateOf<String?>(null)
        private set

    // Funciones para actualizar el estado desde la UI
    fun onEmailChange(newEmail: String) {
        email = newEmail
        feedbackMessage = null // Limpiar al escribir
    }

    fun onPasswordChange(newPassword: String) {
        password = newPassword
        feedbackMessage = null // Limpiar al escribir
    }

    fun clearFeedbackMessage() {
        feedbackMessage = null
    }


    // Funciones de Autenticación

    fun signInUser() {
        // Mantenemos la validación si quieres (puedes simplificar validateForm)
        if (!validateForm(isLogin = true)) return

        isLoading = true
        feedbackMessage = null
        viewModelScope.launch { // Usa viewModelScope y launch
            try {
                auth.signInWithEmailAndPassword(email, password).await() // Usa await()
                // NO necesitas actualizar _currentUser manualmente aquí
                // authStateFlow lo hará automáticamente
                println("Login Successful via AuthViewModel!")
                // La navegación se maneja en la UI basada en el cambio de 'currentUser' StateFlow

            } catch (e: Exception) {
                feedbackMessage = "Error: ${e.localizedMessage ?: "Autenticación fallida"}"
                println("Login error: ${e.message}")
                // NO necesitas poner _currentUser.value = null aquí
            } finally {
                isLoading = false
            }
        }
    }


    fun signOut() {
        auth.signOut()
        // Limpiar campos
        email = ""
        password = ""
        feedbackMessage = "Sesión cerrada." // Puedes mantener o quitar este mensaje
        println("Logout Successful via AuthViewModel!")
    }

    // Validación
    private fun validateForm(isLogin: Boolean): Boolean { // isLogin ya no es tan necesario aquí
        val isEmailValid = android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
        if (email.isBlank() || !isEmailValid) {
            feedbackMessage = "Error: Ingresa un correo electrónico válido."
            return false
        }
        if (password.isBlank()) {
            feedbackMessage = "Error: Ingresa tu contraseña."
            return false
        }
        feedbackMessage = null
        return true
    }
}

fun FirebaseAuth.authStateFlow(): Flow<FirebaseUser?> = callbackFlow {
    val listener = FirebaseAuth.AuthStateListener { auth ->
        trySend(auth.currentUser).isSuccess
    }
    addAuthStateListener(listener)
    awaitClose { removeAuthStateListener(listener) }
}