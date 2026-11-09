package com.example.carnetsanitario.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tutores")
data class TutorEntity(
    @PrimaryKey
    val id: String, // Coincide con el UID de Firebase Auth
    val nombre: String,
    val apellido: String,
    val rut: String,
    val correoElectronico: String,
    val telefono: String,
    val direccion: String? = null,
    val region: String? = null,
    val comuna: String? = null
)