package com.example.carnetsanitario.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "vacunas",
    foreignKeys = [
        ForeignKey(
            entity = MascotaEntity::class,
            parentColumns = ["id"],
            childColumns = ["mascotaId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["mascotaId"])]
)
data class VacunaEntity(
    @PrimaryKey
    val id: String,
    val mascotaId: String,
    val fechaAplicacion: String, // Formato "dd/MM/yyyy"
    val nombreVacuna: String,
    val proximaDosis: String? = null // Formato "dd/MM/yyyy"
)