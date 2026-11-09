package com.example.carnetsanitario.ui.mascotas

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.carnetsanitario.data.local.entity.MascotaEntity
import com.example.carnetsanitario.data.local.entity.TutorEntity
import com.example.carnetsanitario.data.local.enums.Especie
import com.example.carnetsanitario.data.local.enums.EstadoReproductivo
import com.example.carnetsanitario.data.local.enums.Sexo
import com.example.carnetsanitario.data.repository.CarnetRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID

class MascotasViewModel(
    private val repository: CarnetRepository,
    val tutorId: String
) : ViewModel() {

    private val _mostrarSoloActivas = MutableStateFlow(true)
    val mostrarSoloActivas: StateFlow<Boolean> = _mostrarSoloActivas.asStateFlow()

    private val _mensajeError = MutableStateFlow<String?>(null)
    val mensajeError: StateFlow<String?> = _mensajeError.asStateFlow()

    private val _mensajeExito = MutableStateFlow<String?>(null)
    val mensajeExito: StateFlow<String?> = _mensajeExito.asStateFlow()

    val tutorActual: StateFlow<TutorEntity?> = repository.obtenerTutor(tutorId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val listaMascotas: StateFlow<List<MascotaEntity>> = _mostrarSoloActivas
        .flatMapLatest { soloActivas ->
            if (soloActivas) {
                repository.obtenerMascotasActivas(tutorId)
            } else {
                repository.obtenerMascotasInactivas(tutorId)
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun alternarFiltroActivas(activas: Boolean) {
        _mostrarSoloActivas.value = activas
    }

    fun registrarMascota(
        nombre: String,
        especie: Especie,
        sexo: Sexo,
        raza: String,
        color: String,
        fechaNacimiento: String,
        esEdadEstimada: Boolean,
        pesoTexto: String,
        estadoReproductivo: EstadoReproductivo,
        numeroMicrochip: String?,
        fotoUri: String?,
        onSuccess: () -> Unit
    ) {
        if (nombre.isBlank() || raza.isBlank() || color.isBlank() || fechaNacimiento.isBlank() || pesoTexto.isBlank()) {
            _mensajeError.value = "Por favor completa todos los campos requeridos (*)"
            return
        }

        val pesoDouble = pesoTexto.toDoubleOrNull()
        if (pesoDouble == null || pesoDouble <= 0.0) {
            _mensajeError.value = "Ingresa un peso válido en kg (mayor a 0)"
            return
        }

        viewModelScope.launch {
            if (repository.verificarNombreExistente(tutorId, nombre)) {
                _mensajeError.value = "Ya tienes una mascota registrada con el nombre '$nombre'"
                return@launch
            }

            val nuevaMascota = MascotaEntity(
                id = UUID.randomUUID().toString(),
                tutorId = tutorId,
                nombre = nombre.trim(),
                especie = especie,
                sexo = sexo,
                raza = raza.trim(),
                color = color.trim(),
                fechaNacimiento = fechaNacimiento.trim(),
                esEdadEstimada = esEdadEstimada,
                peso = pesoDouble,
                estadoReproductivo = estadoReproductivo,
                numeroMicrochip = numeroMicrochip?.trim()?.takeIf { it.isNotEmpty() },
                fotoUri = fotoUri,
                activa = true
            )

            repository.guardarMascota(nuevaMascota)
            _mensajeError.value = null
            _mensajeExito.value = "¡${nuevaMascota.nombre} registrada con éxito!"
            onSuccess()
        }
    }

    fun actualizarMascota(
        mascotaOriginal: MascotaEntity,
        nombre: String,
        sexo: Sexo,
        raza: String,
        color: String,
        fechaNacimiento: String,
        esEdadEstimada: Boolean,
        pesoTexto: String,
        estadoReproductivo: EstadoReproductivo,
        numeroMicrochip: String?,
        onSuccess: () -> Unit
    ) {
        if (nombre.isBlank() || raza.isBlank() || color.isBlank() || fechaNacimiento.isBlank() || pesoTexto.isBlank()) {
            _mensajeError.value = "Completa todos los campos obligatorios"
            return
        }

        val pesoDouble = pesoTexto.toDoubleOrNull()
        if (pesoDouble == null || pesoDouble <= 0.0) {
            _mensajeError.value = "Ingresa un peso válido en kg"
            return
        }

        viewModelScope.launch {
            if (repository.verificarNombreExistente(tutorId, nombre, mascotaOriginal.id)) {
                _mensajeError.value = "Ya tienes otra mascota registrada como '$nombre'"
                return@launch
            }

            val actualizada = mascotaOriginal.copy(
                nombre = nombre.trim(),
                sexo = sexo,
                raza = raza.trim(),
                color = color.trim(),
                fechaNacimiento = fechaNacimiento.trim(),
                esEdadEstimada = esEdadEstimada,
                peso = pesoDouble,
                estadoReproductivo = estadoReproductivo,
                numeroMicrochip = numeroMicrochip?.trim()?.takeIf { it.isNotEmpty() }
            )

            repository.actualizarMascota(actualizada)
            _mensajeError.value = null
            _mensajeExito.value = "Mascota actualizada correctamente"
            onSuccess()
        }
    }

    fun cambiarEstadoActivo(mascota: MascotaEntity) {
        viewModelScope.launch {
            repository.actualizarMascota(mascota.copy(activa = !mascota.activa))
        }
    }

    fun eliminarMascotaDefinitiva(mascota: MascotaEntity) {
        if (mascota.activa) return // Regla: Solo si ya está archivada
        viewModelScope.launch {
            repository.eliminarMascota(mascota)
            _mensajeExito.value = "${mascota.nombre} fue eliminada definitivamente"
        }
    }

    fun actualizarDatosTutor(
        nombre: String,
        apellido: String,
        rut: String,
        telefono: String,
        direccion: String?,
        region: String?,
        comuna: String?,
        onSuccess: () -> Unit
    ) {
        val actual = tutorActual.value ?: return
        if (nombre.isBlank() || apellido.isBlank() || rut.isBlank() || telefono.isBlank()) {
            _mensajeError.value = "Completa los campos obligatorios del tutor"
            return
        }

        viewModelScope.launch {
            val tutorModificado = actual.copy(
                nombre = nombre.trim(),
                apellido = apellido.trim(),
                rut = rut.trim(),
                telefono = telefono.trim(),
                direccion = direccion?.takeIf { it.isNotBlank() },
                region = region?.takeIf { it.isNotBlank() },
                comuna = comuna?.takeIf { it.isNotBlank() }
            )
            repository.registrarOActualizarTutor(tutorModificado)
            _mensajeError.value = null
            _mensajeExito.value = "Perfil de tutor actualizado"
            onSuccess()
        }
    }

    fun cerrarSesion(onLogout: () -> Unit) {
        repository.authDataSource.cerrarSesion()
        onLogout()
    }

    fun limpiarMensajes() {
        _mensajeError.value = null
        _mensajeExito.value = null
    }
}