package com.example.carnetsanitario.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.carnetsanitario.data.local.enums.Especie
import com.example.carnetsanitario.data.local.enums.EstadoReproductivo
import com.example.carnetsanitario.data.local.enums.Sexo

@Entity(
    tableName = "mascotas",
    foreignKeys = [
        ForeignKey(
            entity = TutorEntity::class,
            parentColumns = ["id"],
            childColumns = ["tutorId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["tutorId"])]
)
data class MascotaEntity(
    @PrimaryKey
    val id: String, // Identificador único (UUID)
    val tutorId: String,
    val nombre: String,
    val especie: Especie,
    val sexo: Sexo,
    val raza: String,
    val color: String,
    val fechaNacimiento: String = "", // formato dd/MM/yyyy
    val esEdadEstimada: Boolean = false,
    val peso: Double = 0.0,
    val estadoReproductivo: EstadoReproductivo,
    val numeroMicrochip: String? = null,
    val fotoUri: String? = null,
    val activa: Boolean = true
)