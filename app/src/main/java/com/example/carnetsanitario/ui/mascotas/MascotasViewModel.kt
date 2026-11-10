package com.example.carnetsanitario.ui.mascotas

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.carnetsanitario.data.local.entity.MascotaEntity
import com.example.carnetsanitario.data.local.entity.TutorEntity
import com.example.carnetsanitario.data.local.enums.Especie
import com.example.carnetsanitario.data.local.enums.EstadoReproductivo
import com.example.carnetsanitario.data.local.enums.Sexo
import com.example.carnetsanitario.data.repository.CarnetRepository
import com.example.carnetsanitario.ui.util.ImageUtils
import com.example.carnetsanitario.ui.util.ValidacionesUtil
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

    init {
        // Inicia la sincronización en tiempo real desde Firestore hacia Room
        repository.iniciarSincronizacionMascotas(tutorId, viewModelScope)
    }

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
        context: Context,
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
        // 1. Validar textos
        val (nombreOk, errorNombre) = ValidacionesUtil.validarTextoRequerido(nombre, "Nombre de la mascota")
        if (!nombreOk) { _mensajeError.value = errorNombre; return }

        val (razaOk, errorRaza) = ValidacionesUtil.validarTextoRequerido(raza, "Raza")
        if (!razaOk) { _mensajeError.value = errorRaza; return }

        val (colorOk, errorColor) = ValidacionesUtil.validarTextoRequerido(color, "Color")
        if (!colorOk) { _mensajeError.value = errorColor; return }

        // 2. Validar fecha
        val (fechaOk, errorFecha) = ValidacionesUtil.validarFechaNacimiento(fechaNacimiento)
        if (!fechaOk) { _mensajeError.value = errorFecha; return }

        // 3. Validar peso
        val (pesoValido, errorPeso) = ValidacionesUtil.parsearYValidarPeso(pesoTexto)
        if (pesoValido == null) { _mensajeError.value = errorPeso; return }

        // 4. Validar microchip
        val (chipOk, errorChip) = ValidacionesUtil.validarMicrochip(numeroMicrochip)
        if (!chipOk) { _mensajeError.value = errorChip; return }

        viewModelScope.launch {
            if (repository.verificarNombreExistente(tutorId, nombre)) {
                _mensajeError.value = "Ya tienes una mascota registrada con el nombre '$nombre'"
                return@launch
            }

            val fotoPersistible = ImageUtils.uriABase64(context, fotoUri)

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
                peso = pesoValido,
                estadoReproductivo = estadoReproductivo,
                numeroMicrochip = numeroMicrochip?.trim()?.takeIf { it.isNotEmpty() },
                fotoUri = fotoPersistible,
                activa = true
            )

            repository.guardarMascota(nuevaMascota)
            _mensajeError.value = null
            _mensajeExito.value = "¡${nuevaMascota.nombre} registrada con éxito!"
            onSuccess()
        }
    }

    fun actualizarMascota(
        context: Context,
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
        fotoUri: String?,
        onSuccess: () -> Unit
    ) {
        val (nombreOk, errorNombre) = ValidacionesUtil.validarTextoRequerido(nombre, "Nombre de la mascota")
        if (!nombreOk) { _mensajeError.value = errorNombre; return }

        val (razaOk, errorRaza) = ValidacionesUtil.validarTextoRequerido(raza, "Raza")
        if (!razaOk) { _mensajeError.value = errorRaza; return }

        val (colorOk, errorColor) = ValidacionesUtil.validarTextoRequerido(color, "Color")
        if (!colorOk) { _mensajeError.value = errorColor; return }

        val (fechaOk, errorFecha) = ValidacionesUtil.validarFechaNacimiento(fechaNacimiento)
        if (!fechaOk) { _mensajeError.value = errorFecha; return }

        val (pesoValido, errorPeso) = ValidacionesUtil.parsearYValidarPeso(pesoTexto)
        if (pesoValido == null) { _mensajeError.value = errorPeso; return }

        val (chipOk, errorChip) = ValidacionesUtil.validarMicrochip(numeroMicrochip)
        if (!chipOk) { _mensajeError.value = errorChip; return }

        viewModelScope.launch {
            if (repository.verificarNombreExistente(tutorId, nombre, mascotaOriginal.id)) {
                _mensajeError.value = "Ya tienes otra mascota registrada como '$nombre'"
                return@launch
            }

            val fotoPersistible = if (fotoUri != mascotaOriginal.fotoUri) {
                ImageUtils.uriABase64(context, fotoUri)
            } else {
                mascotaOriginal.fotoUri
            }

            val actualizada = mascotaOriginal.copy(
                nombre = nombre.trim(),
                sexo = sexo,
                raza = raza.trim(),
                color = color.trim(),
                fechaNacimiento = fechaNacimiento.trim(),
                esEdadEstimada = esEdadEstimada,
                peso = pesoValido,
                estadoReproductivo = estadoReproductivo,
                numeroMicrochip = numeroMicrochip?.trim()?.takeIf { it.isNotEmpty() },
                fotoUri = fotoPersistible
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
        if (mascota.activa) return
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

        val (nombreOk, errorNombre) = ValidacionesUtil.validarNombreOApellido(nombre, "El nombre")
        if (!nombreOk) { _mensajeError.value = errorNombre; return }

        val (apellidoOk, errorApellido) = ValidacionesUtil.validarNombreOApellido(apellido, "El apellido")
        if (!apellidoOk) { _mensajeError.value = errorApellido; return }

        val (rutOk, errorRut) = ValidacionesUtil.validarRutChileno(rut)
        if (!rutOk) { _mensajeError.value = errorRut; return }

        val (telOk, errorTel) = ValidacionesUtil.validarTelefonoChileno(telefono)
        if (!telOk) { _mensajeError.value = errorTel; return }

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
        repository.detenerSincronizacionMascotas()
        _mensajeError.value = null
        _mensajeExito.value = null
        repository.authDataSource.cerrarSesion()
        onLogout()
    }

    fun limpiarMensajes() {
        _mensajeError.value = null
        _mensajeExito.value = null
    }

    override fun onCleared() {
        super.onCleared()
        repository.detenerSincronizacionMascotas()
    }
}