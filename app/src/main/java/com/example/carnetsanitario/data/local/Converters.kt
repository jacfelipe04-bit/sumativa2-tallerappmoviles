package com.example.carnetsanitario.data.local

import androidx.room.TypeConverter
import com.example.carnetsanitario.data.local.enums.Especie
import com.example.carnetsanitario.data.local.enums.EstadoReproductivo
import com.example.carnetsanitario.data.local.enums.Sexo

class Converters {
    @TypeConverter
    fun fromEspecie(value: Especie?): String? = value?.name

    @TypeConverter
    fun toEspecie(value: String?): Especie? = value?.let { Especie.valueOf(it) }

    @TypeConverter
    fun fromSexo(value: Sexo?): String? = value?.name

    @TypeConverter
    fun toSexo(value: String?): Sexo? = value?.let { Sexo.valueOf(it) }

    @TypeConverter
    fun fromEstadoReproductivo(value: EstadoReproductivo?): String? = value?.name

    @TypeConverter
    fun toEstadoReproductivo(value: String?): EstadoReproductivo? = value?.let { EstadoReproductivo.valueOf(it) }
}