package com.example.carnetsanitario.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Update
import androidx.room.Upsert
import com.example.carnetsanitario.data.local.entity.DesparasitacionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DesparasitacionDao {
    @Upsert
    suspend fun insertarDesparasitacion(desparasitacion: DesparasitacionEntity)

    @Update
    suspend fun actualizarDesparasitacion(desparasitacion: DesparasitacionEntity)

    @Delete
    suspend fun eliminarDesparasitacion(desparasitacion: DesparasitacionEntity)

    @Query("SELECT * FROM desparasitaciones WHERE mascotaId = :mascotaId ORDER BY fechaAplicacion DESC")
    fun obtenerDesparasitacionesPorMascota(mascotaId: String): Flow<List<DesparasitacionEntity>>
}