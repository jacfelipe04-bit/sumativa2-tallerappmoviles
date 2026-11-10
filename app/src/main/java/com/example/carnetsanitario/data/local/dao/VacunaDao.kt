package com.example.carnetsanitario.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Update
import androidx.room.Upsert
import com.example.carnetsanitario.data.local.entity.VacunaEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface VacunaDao {
    @Upsert
    suspend fun insertarVacuna(vacuna: VacunaEntity)

    @Update
    suspend fun actualizarVacuna(vacuna: VacunaEntity)

    @Delete
    suspend fun eliminarVacuna(vacuna: VacunaEntity)

    @Query("SELECT * FROM vacunas WHERE mascotaId = :mascotaId ORDER BY fechaAplicacion DESC")
    fun obtenerVacunasPorMascota(mascotaId: String): Flow<List<VacunaEntity>>
}