package com.example.carnetsanitario.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Update
import androidx.room.Upsert
import com.example.carnetsanitario.data.local.entity.MascotaEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MascotaDao {

    @Upsert
    suspend fun insertarMascota(mascota: MascotaEntity)

    // Inserción en lote para sincronizar desde Firestore
    @Upsert
    suspend fun insertarMascotas(mascotas: List<MascotaEntity>)

    @Update
    suspend fun actualizarMascota(mascota: MascotaEntity)

    @Delete
    suspend fun eliminarMascota(mascota: MascotaEntity)

    // Mascotas activas del tutor (pantalla principal)
    @Query("SELECT * FROM mascotas WHERE tutorId = :tutorId AND activa = 1 ORDER BY nombre ASC")
    fun obtenerMascotasActivas(tutorId: String): Flow<List<MascotaEntity>>

    // Mascotas archivadas/inactivas
    @Query("SELECT * FROM mascotas WHERE tutorId = :tutorId AND activa = 0 ORDER BY nombre ASC")
    fun obtenerMascotasInactivas(tutorId: String): Flow<List<MascotaEntity>>

    // Detalle de una mascota específica
    @Query("SELECT * FROM mascotas WHERE id = :mascotaId LIMIT 1")
    fun obtenerMascotaPorId(mascotaId: String): Flow<MascotaEntity?>

    // Todas las mascotas de un tutor (para validaciones de duplicados)
    @Query("SELECT * FROM mascotas WHERE tutorId = :tutorId")
    suspend fun obtenerTodasPorTutorSync(tutorId: String): List<MascotaEntity>

    // Elimina de Room mascotas que ya no existen en la lista remota
    @Query("DELETE FROM mascotas WHERE tutorId = :tutorId AND id NOT IN (:idsActuales)")
    suspend fun eliminarMascotasNoPresentes(tutorId: String, idsActuales: List<String>)

    // Borrado total de un tutor si su colección en Firestore quedó vacía
    @Query("DELETE FROM mascotas WHERE tutorId = :tutorId")
    suspend fun eliminarTodasPorTutor(tutorId: String)
}