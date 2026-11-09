package com.example.carnetsanitario.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.carnetsanitario.data.local.entity.DesparasitacionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DesparasitacionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarDesparasitacion(desparasitacion: DesparasitacionEntity)

    @Update
    suspend fun actualizarDesparasitacion(desparasitacion: DesparasitacionEntity)

    @Delete
    suspend fun eliminarDesparasitacion(desparasitacion: DesparasitacionEntity)

    @Query("SELECT * FROM desparasitaciones WHERE mascotaId = :mascotaId ORDER BY fechaAplicacion DESC")
    fun obtenerDesparasitacionesPorMascota(mascotaId: String): Flow<List<DesparasitacionEntity>>
}