package com.example.carnetsanitario.ui.carnet

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.carnetsanitario.data.local.entity.DesparasitacionEntity
import com.example.carnetsanitario.data.local.entity.MascotaEntity
import com.example.carnetsanitario.data.local.entity.VacunaEntity
import com.example.carnetsanitario.data.repository.CarnetRepository
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

    fun registrarVacuna(
        nombreVacuna: String,
        fechaAplicacion: String,
        proximaDosis: String?,
        onSuccess: () -> Unit
    ) {
        if (nombreVacuna.isBlank() || fechaAplicacion.isBlank()) return

        val nuevaVacuna = VacunaEntity(
            id = UUID.randomUUID().toString(),
            mascotaId = mascotaId,
            fechaAplicacion = fechaAplicacion.trim(),
            nombreVacuna = nombreVacuna.trim(),
            proximaDosis = proximaDosis?.trim()?.takeIf { it.isNotEmpty() }
        )

        viewModelScope.launch {
            repository.registrarVacuna(tutorId, nuevaVacuna)
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
        if (nombreAntiparasitario.isBlank() || fechaAplicacion.isBlank()) return

        val nuevaDesparasitacion = DesparasitacionEntity(
            id = UUID.randomUUID().toString(),
            mascotaId = mascotaId,
            fechaAplicacion = fechaAplicacion.trim(),
            nombreAntiparasitario = nombreAntiparasitario.trim(),
            proximaDosis = proximaDosis?.trim()?.takeIf { it.isNotEmpty() }
        )

        viewModelScope.launch {
            repository.registrarDesparasitacion(tutorId, nuevaDesparasitacion)
            onSuccess()
        }
    }

    fun eliminarDesparasitacion(desparasitacion: DesparasitacionEntity) {
        viewModelScope.launch {
            repository.eliminarDesparasitacion(tutorId, desparasitacion)
        }
    }

    fun editarVacuna(vacunaActualizada: VacunaEntity) {
        viewModelScope.launch {
            repository.actualizarVacuna(tutorId, vacunaActualizada)
        }
    }

    fun editarDesparasitacion(desparasitacionActualizada: DesparasitacionEntity) {
        viewModelScope.launch {
            repository.actualizarDesparasitacion(tutorId, desparasitacionActualizada)
        }
    }
}