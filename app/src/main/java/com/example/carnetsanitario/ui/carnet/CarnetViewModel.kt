package com.example.carnetsanitario.ui.carnet

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.carnetsanitario.data.local.entity.DesparasitacionEntity
import com.example.carnetsanitario.data.local.entity.MascotaEntity
import com.example.carnetsanitario.data.local.entity.VacunaEntity
import com.example.carnetsanitario.data.repository.CarnetRepository
import com.example.carnetsanitario.ui.util.ValidacionesUtil
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID

class CarnetViewModel(
    private val repository: CarnetRepository,
    val mascotaId: String,
    private val tutorId: String
) : ViewModel() {

    val mascota: StateFlow<MascotaEntity?> = repository.obtenerMascotaPorId(mascotaId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val vacunas: StateFlow<List<VacunaEntity>> = repository.obtenerVacunas(mascotaId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val desparasitaciones: StateFlow<List<DesparasitacionEntity>> = repository.obtenerDesparasitaciones(mascotaId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _mensajeError = MutableStateFlow<String?>(null)
    val mensajeError: StateFlow<String?> = _mensajeError.asStateFlow()

    fun limpiarError() { _mensajeError.value = null }

    /** Devuelve el mensaje de error, o null si los datos son válidos. */
    private fun validarRegistro(nombre: String, campoNombre: String, fecha: String, proxima: String?): String? {
        val (nombreOk, errorNombre) = ValidacionesUtil.validarTextoRequerido(nombre, campoNombre, min = 2, max = 60)
        if (!nombreOk) return errorNombre
        val (fechaOk, errorFecha) = ValidacionesUtil.validarFechaAplicacion(fecha)
        if (!fechaOk) return errorFecha
        val (proxOk, errorProx) = ValidacionesUtil.validarProximaDosis(fecha, proxima)
        if (!proxOk) return errorProx
        return null
    }

    fun registrarVacuna(
        nombreVacuna: String,
        fechaAplicacion: String,
        proximaDosis: String?,
        onSuccess: () -> Unit
    ) {
        validarRegistro(nombreVacuna, "Nombre de la vacuna", fechaAplicacion, proximaDosis)?.let {
            _mensajeError.value = it
            return
        }

        val nuevaVacuna = VacunaEntity(
            id = UUID.randomUUID().toString(),
            mascotaId = mascotaId,
            fechaAplicacion = fechaAplicacion.trim(),
            nombreVacuna = nombreVacuna.trim(),
            proximaDosis = proximaDosis?.trim()?.takeIf { it.isNotEmpty() }
        )

        viewModelScope.launch {
            repository.registrarVacuna(tutorId, nuevaVacuna)
            _mensajeError.value = null
            onSuccess()
        }
    }

    fun eliminarVacuna(vacuna: VacunaEntity) {
        viewModelScope.launch {
            repository.eliminarVacuna(tutorId, vacuna)
        }
    }

    fun registrarDesparasitacion(
        nombreAntiparasitario: String,
        fechaAplicacion: String,
        proximaDosis: String?,
        onSuccess: () -> Unit
    ) {
        validarRegistro(nombreAntiparasitario, "Producto antiparasitario", fechaAplicacion, proximaDosis)?.let {
            _mensajeError.value = it
            return
        }

        val nuevaDesparasitacion = DesparasitacionEntity(
            id = UUID.randomUUID().toString(),
            mascotaId = mascotaId,
            fechaAplicacion = fechaAplicacion.trim(),
            nombreAntiparasitario = nombreAntiparasitario.trim(),
            proximaDosis = proximaDosis?.trim()?.takeIf { it.isNotEmpty() }
        )

        viewModelScope.launch {
            repository.registrarDesparasitacion(tutorId, nuevaDesparasitacion)
            _mensajeError.value = null
            onSuccess()
        }
    }

    fun eliminarDesparasitacion(desparasitacion: DesparasitacionEntity) {
        viewModelScope.launch {
            repository.eliminarDesparasitacion(tutorId, desparasitacion)
        }
    }

    fun editarVacuna(
        vacunaOriginal: VacunaEntity,
        nombreVacuna: String,
        fechaAplicacion: String,
        proximaDosis: String?,
        onSuccess: () -> Unit
    ) {
        validarRegistro(nombreVacuna, "Nombre de la vacuna", fechaAplicacion, proximaDosis)?.let {
            _mensajeError.value = it
            return
        }

        viewModelScope.launch {
            repository.actualizarVacuna(
                tutorId,
                vacunaOriginal.copy(
                    nombreVacuna = nombreVacuna.trim(),
                    fechaAplicacion = fechaAplicacion.trim(),
                    proximaDosis = proximaDosis?.trim()?.takeIf { it.isNotEmpty() }
                )
            )
            _mensajeError.value = null
            onSuccess()
        }
    }

    fun editarDesparasitacion(
        original: DesparasitacionEntity,
        nombreAntiparasitario: String,
        fechaAplicacion: String,
        proximaDosis: String?,
        onSuccess: () -> Unit
    ) {
        validarRegistro(nombreAntiparasitario, "Producto antiparasitario", fechaAplicacion, proximaDosis)?.let {
            _mensajeError.value = it
            return
        }

        viewModelScope.launch {
            repository.actualizarDesparasitacion(
                tutorId,
                original.copy(
                    nombreAntiparasitario = nombreAntiparasitario.trim(),
                    fechaAplicacion = fechaAplicacion.trim(),
                    proximaDosis = proximaDosis?.trim()?.takeIf { it.isNotEmpty() }
                )
            )
            _mensajeError.value = null
            onSuccess()
        }
    }
}