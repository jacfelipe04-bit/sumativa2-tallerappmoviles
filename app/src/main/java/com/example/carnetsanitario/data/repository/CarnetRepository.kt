package com.example.carnetsanitario.data.repository

import com.example.carnetsanitario.data.local.AppDatabase
import com.example.carnetsanitario.data.local.entity.DesparasitacionEntity
import com.example.carnetsanitario.data.local.entity.MascotaEntity
import com.example.carnetsanitario.data.local.entity.TutorEntity
import com.example.carnetsanitario.data.local.entity.VacunaEntity
import com.example.carnetsanitario.data.remote.FirebaseAuthDataSource
import com.example.carnetsanitario.data.remote.FirebaseFirestoreDataSource
import kotlinx.coroutines.flow.Flow

class CarnetRepository(
    private val db: AppDatabase,
    val authDataSource: FirebaseAuthDataSource = FirebaseAuthDataSource(),
    private val firestoreDataSource: FirebaseFirestoreDataSource = FirebaseFirestoreDataSource()
) {

    // --- GESTIÓN DE TUTOR ---
    fun obtenerTutor(tutorId: String): Flow<TutorEntity?> =
        db.tutorDao().obtenerTutorPorId(tutorId)

    suspend fun registrarOActualizarTutor(tutor: TutorEntity) {
        db.tutorDao().insertarOActualizar(tutor)
        try {
            firestoreDataSource.guardarTutor(tutor)
        } catch (_: Exception) {
            // Se mantiene persistido localmente si no hay conexión
        }
    }

    // --- GESTIÓN DE MASCOTAS ---
    fun obtenerMascotasActivas(tutorId: String): Flow<List<MascotaEntity>> =
        db.mascotaDao().obtenerMascotasActivas(tutorId)

    fun obtenerMascotasInactivas(tutorId: String): Flow<List<MascotaEntity>> =
        db.mascotaDao().obtenerMascotasInactivas(tutorId)

    fun obtenerMascotaPorId(mascotaId: String): Flow<MascotaEntity?> =
        db.mascotaDao().obtenerMascotaPorId(mascotaId)

    suspend fun guardarMascota(mascota: MascotaEntity) {
        db.mascotaDao().insertarMascota(mascota)
        try {
            firestoreDataSource.guardarMascota(mascota)
        } catch (_: Exception) {
            // Guardado en Room asegurado
        }
    }

    suspend fun eliminarMascota(mascota: MascotaEntity) {
        db.mascotaDao().eliminarMascota(mascota)
        try {
            firestoreDataSource.eliminarMascota(mascota.tutorId, mascota.id)
        } catch (_: Exception) {
            // Continúa sin romper ejecución
        }
    }

    // --- GESTIÓN DE VACUNAS ---
    fun obtenerVacunas(mascotaId: String): Flow<List<VacunaEntity>> =
        db.vacunaDao().obtenerVacunasPorMascota(mascotaId)

    suspend fun registrarVacuna(tutorId: String, vacuna: VacunaEntity) {
        db.vacunaDao().insertarVacuna(vacuna)
        try {
            firestoreDataSource.guardarVacuna(tutorId, vacuna)
        } catch (_: Exception) {}
    }

    suspend fun eliminarVacuna(tutorId: String, vacuna: VacunaEntity) {
        db.vacunaDao().eliminarVacuna(vacuna)
        try {
            firestoreDataSource.eliminarVacuna(tutorId, vacuna.mascotaId, vacuna.id)
        } catch (_: Exception) {}
    }

    // --- GESTIÓN DE DESPARASITACIONES ---
    fun obtenerDesparasitaciones(mascotaId: String): Flow<List<DesparasitacionEntity>> =
        db.desparasitacionDao().obtenerDesparasitacionesPorMascota(mascotaId)

    suspend fun registrarDesparasitacion(tutorId: String, desparasitacion: DesparasitacionEntity) {
        db.desparasitacionDao().insertarDesparasitacion(desparasitacion)
        try {
            firestoreDataSource.guardarDesparasitacion(tutorId, desparasitacion)
        } catch (_: Exception) {}
    }

    suspend fun eliminarDesparasitacion(tutorId: String, desparasitacion: DesparasitacionEntity) {
        db.desparasitacionDao().eliminarDesparasitacion(desparasitacion)
        try {
            firestoreDataSource.eliminarDesparasitacion(tutorId, desparasitacion.mascotaId, desparasitacion.id)
        } catch (_: Exception) {}
    }

    // --- ACTUALIZACIÓN Y EDICIÓN ---
    suspend fun actualizarMascota(mascota: MascotaEntity) {
        db.mascotaDao().actualizarMascota(mascota)
        try {
            firestoreDataSource.guardarMascota(mascota)
        } catch (_: Exception) {}
    }

    suspend fun verificarNombreExistente(tutorId: String, nombre: String, mascotaIdExcluir: String? = null): Boolean {
        val lista = db.mascotaDao().obtenerTodasPorTutorSync(tutorId)
        val normalizado = java.text.Normalizer.normalize(nombre.trim(), java.text.Normalizer.Form.NFD)
            .replace("\\p{InCombiningDiacriticalMarks}+".toRegex(), "")
            .lowercase()

        return lista.any { m ->
            if (mascotaIdExcluir != null && m.id == mascotaIdExcluir) return@any false
            val mNormalizado = java.text.Normalizer.normalize(m.nombre.trim(), java.text.Normalizer.Form.NFD)
                .replace("\\p{InCombiningDiacriticalMarks}+".toRegex(), "")
                .lowercase()
            mNormalizado == normalizado
        }
    }

    suspend fun actualizarVacuna(tutorId: String, vacuna: VacunaEntity) {
        db.vacunaDao().actualizarVacuna(vacuna)
        try {
            firestoreDataSource.guardarVacuna(tutorId, vacuna)
        } catch (_: Exception) {}
    }

    suspend fun actualizarDesparasitacion(tutorId: String, desparasitacion: DesparasitacionEntity) {
        db.desparasitacionDao().actualizarDesparasitacion(desparasitacion)
        try {
            firestoreDataSource.guardarDesparasitacion(tutorId, desparasitacion)
        } catch (_: Exception) {}
    }
}