package com.icm2510.appfirebase.presentation.auth // Asegúrate que el paquete sea correcto

import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import com.icm2510.appfirebase.data.model.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

// Estado para la UI de Registro
data class RegisterState(
    val name: String = "",
    val phone: String = "",
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val selectedImageUri: Uri? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val registrationSuccess: Boolean = false
)

class RegisterViewModel : ViewModel() {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val storage: FirebaseStorage = Firebase.storage

    // Usamos StateFlow para el estado completo, siguiendo un patrón MVI ligero
    private val _uiState = MutableStateFlow(RegisterState())
    val uiState: StateFlow<RegisterState> = _uiState.asStateFlow()

    // --- Funciones para actualizar campos de texto ---
    fun onNameChange(newName: String) {
        _uiState.value = _uiState.value.copy(name = newName, error = null)
    }
    fun onPhoneChange(newPhone: String) {
        _uiState.value = _uiState.value.copy(phone = newPhone, error = null)
    }
    fun onEmailChange(newEmail: String) {
        _uiState.value = _uiState.value.copy(email = newEmail, error = null)
    }
    fun onPasswordChange(newPassword: String) {
        _uiState.value = _uiState.value.copy(password = newPassword, error = null)
    }
    fun onConfirmPasswordChange(newConfirmPassword: String) {
        _uiState.value = _uiState.value.copy(confirmPassword = newConfirmPassword, error = null)
    }
    fun onImageSelected(uri: Uri?) {
        _uiState.value = _uiState.value.copy(selectedImageUri = uri, error = null)
    }

    // --- Función principal de Registro ---
    fun registerUser() {
        val currentState = _uiState.value
        if (!validateInputs(currentState)) return

        _uiState.value = currentState.copy(isLoading = true, error = null)

        viewModelScope.launch {
            try {
                // 1. Crear usuario en Firebase Auth
                val authResult = auth.createUserWithEmailAndPassword(currentState.email, currentState.password).await()
                val userId = authResult.user?.uid ?: throw IllegalStateException("Firebase User ID not found after registration")

                // 2. Subir foto a Firebase Storage (si hay una seleccionada)
                var downloadUrl: String? = null
                if (currentState.selectedImageUri != null) {
                    // --- CORRECCIÓN AQUÍ: Elimina .await() ---
                    downloadUrl = uploadProfilePicture(userId, currentState.selectedImageUri) // Llama a la función suspend directamente
                    // --- FIN CORRECCIÓN ---

                    if (downloadUrl == null) {
                        println("Advertencia: No se pudo obtener la URL de descarga de la imagen, continuando sin ella.")
                    }
                }

                // 3. Crear objeto User para Firestore
                val user = User(
                    userId = userId,
                    name = currentState.name.trim(),
                    email = currentState.email.trim(),
                    phone = currentState.phone.trim(),
                    profilePictureUrl = downloadUrl
                )

                // 4. Guardar datos del usuario en Firestore
                firestore.collection("users").document(userId).set(user).await()

                // 5. Registro exitoso
                _uiState.value = _uiState.value.copy(isLoading = false, registrationSuccess = true)
                println("User registered successfully!")

            } catch (e: Exception) {
                println("Registration failed: ${e.message}")
                _uiState.value = _uiState.value.copy(isLoading = false, error = e.message ?: "An unknown error occurred")
            }
        }
    }

    // --- Función para subir la imagen ---
    private suspend fun uploadProfilePicture(userId: String, fileUri: Uri): String? {
        return try {
            val ref = storage.reference.child("profile_pics/$userId.jpg")
            ref.putFile(fileUri).await() // Espera a que termine la subida
            ref.downloadUrl.await()?.toString() // Espera a obtener la URL
        } catch (e: Exception) {
            println("Error uploading profile picture: ${e.message}")
            null // Devuelve null si falla la subida o la obtención de URL
        }
    }

    // --- Validación simple de entradas ---
    private fun validateInputs(state: RegisterState): Boolean {
        if (state.name.isBlank() || state.phone.isBlank() || state.email.isBlank() || state.password.isBlank() || state.confirmPassword.isBlank()) {
            _uiState.value = state.copy(error = "All fields except photo are required.")
            return false
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(state.email).matches()) {
            _uiState.value = state.copy(error = "Invalid email format.")
            return false
        }
        if (state.password.length < 6) {
            _uiState.value = state.copy(error = "Password must be at least 6 characters long.")
            return false
        }
        if (state.password != state.confirmPassword) {
            _uiState.value = state.copy(error = "Passwords do not match.")
            return false
        }
        // Añade más validaciones si es necesario (ej. formato de teléfono)
        return true
    }

    // Llama a esta función si el usuario navega fuera y quieres limpiar el error
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}