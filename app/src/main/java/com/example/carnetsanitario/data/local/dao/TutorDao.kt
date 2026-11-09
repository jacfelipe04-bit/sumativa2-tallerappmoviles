package com.example.carnetsanitario.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.carnetsanitario.data.local.entity.TutorEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TutorDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarOActualizar(tutor: TutorEntity)

    @Update
    suspend fun actualizar(tutor: TutorEntity)

    @Query("SELECT * FROM tutores WHERE id = :tutorId LIMIT 1")
    fun obtenerTutorPorId(tutorId: String): Flow<TutorEntity?>

    @Query("DELETE FROM tutores WHERE id = :tutorId")
    suspend fun eliminarTutor(tutorId: String)
}