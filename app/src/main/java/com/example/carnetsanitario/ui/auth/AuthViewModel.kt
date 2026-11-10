package com.example.carnetsanitario.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.carnetsanitario.data.local.entity.TutorEntity
import com.example.carnetsanitario.data.repository.CarnetRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface AuthUiState {
    data object Idle : AuthUiState
    data object Cargando : AuthUiState
    data class Autenticado(val tutorId: String) : AuthUiState
    data class Error(val mensaje: String) : AuthUiState
}

class AuthViewModel(
    private val repository: CarnetRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    init {
        // Si ya hay una sesión activa en Firebase, entra directamente
        repository.authDataSource.tutorIdActual?.let { uid ->
            _uiState.value = AuthUiState.Autenticado(uid)
        }
    }

    fun iniciarSesion(correo: String, contrasena: String) {
        if (correo.isBlank() || contrasena.isBlank()) {
            _uiState.value = AuthUiState.Error("Ingresa tu correo y contraseña")
            return
        }

        viewModelScope.launch {
            _uiState.value = AuthUiState.Cargando
            val resultado = repository.authDataSource.iniciarSesion(correo.trim(), contrasena)
            resultado.fold(
                onSuccess = { user ->
                    _uiState.value = AuthUiState.Autenticado(user.uid)
                },
                onFailure = { error ->
                    _uiState.value = AuthUiState.Error(error.localizedMessage ?: "Credenciales incorrectas")
                }
            )
        }
    }

    fun registrarTutor(
        nombre: String,
        apellido: String,
        rut: String,
        correo: String,
        contrasena: String,
        telefono: String
    ) {
        if (nombre.isBlank() || apellido.isBlank() || rut.isBlank() || correo.isBlank() || contrasena.isBlank() || telefono.isBlank()) {
            _uiState.value = AuthUiState.Error("Por favor completa los campos obligatorios")
            return
        }

        viewModelScope.launch {
            _uiState.value = AuthUiState.Cargando
            val resultado = repository.authDataSource.registrarTutor(correo.trim(), contrasena)
            resultado.fold(
                onSuccess = { user ->
                    val nuevoTutor = TutorEntity(
                        id = user.uid,
                        nombre = nombre.trim(),
                        apellido = apellido.trim(),
                        rut = rut.trim(),
                        correoElectronico = correo.trim(),
                        telefono = telefono.trim(),
                        direccion = null,
                        region = null,
                        comuna = null
                    )
                    repository.registrarOActualizarTutor(nuevoTutor)
                    _uiState.value = AuthUiState.Autenticado(user.uid)
                },
                onFailure = { error ->
                    _uiState.value = AuthUiState.Error(error.localizedMessage ?: "Error al registrar la cuenta")
                }
            )
        }
    }

    fun reiniciarEstado() {
        _uiState.value = AuthUiState.Idle
    }
}