package com.icm2510.auth.presentation.auth

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

class AuthViewModel : ViewModel() {

    private val auth: FirebaseAuth = Firebase.auth

    // Expone el usuario actual (null si no está en sesión)
    private val _currentUser = MutableStateFlow(auth.currentUser)
    val currentUser: StateFlow<FirebaseUser?> = _currentUser

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
        if (!validateForm(isLogin = true)) return

        isLoading = true
        feedbackMessage = null
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                isLoading = false
                if (task.isSuccessful) {
                    _currentUser.value = auth.currentUser // Actualiza el usuario en sesión
                    // La UI reaccionará y realizarà la navegación
                } else {
                    feedbackMessage = "Error: ${task.exception?.localizedMessage ?: "Autenticación fallida"}"
                    _currentUser.value = null
                }
            }
    }

    fun signUpUser() {
        if (!validateForm(isLogin = false)) return

        isLoading = true
        feedbackMessage = null
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                isLoading = false
                if (task.isSuccessful) {
                    _currentUser.value = auth.currentUser // Registro exitoso = login automático
                    feedbackMessage = "Usuario registrado exitosamente."
                    // La UI reaccionará y navegará
                } else {
                    feedbackMessage = "Error Registro: ${task.exception?.localizedMessage ?: "No se pudo registrar"}"
                    _currentUser.value = null
                }
            }
    }

    fun signOut() {
        auth.signOut()
        _currentUser.value = null // Actualiza el estado a "no logueado"
        // Limpiar campos
        email = ""
        password = ""
        feedbackMessage = "Sesión cerrada."
    }

    // Validación
    private fun validateForm(isLogin: Boolean): Boolean {
        val isEmailValid = android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
        if (email.isBlank() || !isEmailValid) {
            feedbackMessage = "Error: Ingresa un correo electrónico válido."
            return false
        }
        // En login, solo validamos que no esté vacío. En el registro debe tener mínimo 6 caracteres.
        if (password.isBlank() || (!isLogin && password.length < 6)) {
            feedbackMessage = if (isLogin) "Error: Ingresa tu contraseña." else "Error: La contraseña debe tener al menos 6 caracteres."
            return false
        }
        feedbackMessage = null // Limpiar mensaje si todo está bien hasta ahora
        return true
    }
}