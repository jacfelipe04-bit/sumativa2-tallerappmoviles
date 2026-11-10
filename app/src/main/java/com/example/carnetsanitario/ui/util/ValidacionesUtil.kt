package com.example.carnetsanitario.ui.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object ValidacionesUtil {

    // --- VALIDACIONES DE TUTOR ---

    /**
     * Valida nombres y apellidos permitiendo letras, espacios, tildes y diéresis.
     */
    fun validarNombreOApellido(valor: String, campo: String): Pair<Boolean, String?> {
        val limpio = valor.trim()
        if (limpio.isBlank()) return Pair(false, "El campo $campo es obligatorio")
        if (limpio.length < 2) return Pair(false, "$campo debe tener al menos 2 caracteres")
        if (limpio.length > 50) return Pair(false, "$campo no puede superar los 50 caracteres")

        val regex = Regex("""^[a-zA-ZáéíóúÁÉÍÓÚñÑüÜ\s'-]+$""")
        return if (!regex.matches(limpio)) {
            Pair(false, "$campo solo puede contener letras y espacios")
        } else {
            Pair(true, null)
        }
    }

    /**
     * Valida RUT chileno mediante algoritmo Módulo 11 (acepta con o sin puntos/guion).
     */
    fun validarRutChileno(rutStr: String): Pair<Boolean, String?> {
        val limpio = rutStr.trim().replace(".", "").replace("-", "").uppercase()
        if (limpio.length < 8 || limpio.length > 9) {
            return Pair(false, "Formato de RUT inválido (ej: 12345678-9)")
        }

        val cuerpo = limpio.substring(0, limpio.length - 1)
        val dvIngresado = limpio.last()

        if (!cuerpo.all { it.isDigit() }) {
            return Pair(false, "El cuerpo del RUT solo debe contener números")
        }

        // Algoritmo Módulo 11
        var suma = 0
        var multiplicador = 2
        for (i in cuerpo.indices.reversed()) {
            suma += (cuerpo[i] - '0') * multiplicador
            multiplicador = if (multiplicador == 7) 2 else multiplicador + 1
        }

        val resto = 11 - (suma % 11)
        val dvEsperado = when (resto) {
            11 -> '0'
            10 -> 'K'
            else -> (resto + '0'.code).toChar()
        }

        return if (dvIngresado == dvEsperado) {
            Pair(true, null)
        } else {
            Pair(false, "RUT chileno no válido (dígito verificador incorrecto)")
        }
    }

    /**
     * Valida número telefónico móvil o fijo chileno (+569XXXXXXXX o 9XXXXXXXX).
     */
    fun validarTelefonoChileno(telStr: String): Pair<Boolean, String?> {
        val limpio = telStr.trim().replace(" ", "").replace("-", "")
        if (limpio.isBlank()) return Pair(false, "El teléfono es obligatorio")

        val regex = Regex("""^(\+?56)?9\d{8}$""")
        return if (!regex.matches(limpio)) {
            Pair(false, "Ingresa un número móvil chileno válido (ej: +56912345678 o 912345678)")
        } else {
            Pair(true, null)
        }
    }

    // --- VALIDACIONES DE MASCOTA ---

    fun validarFechaNacimiento(fechaStr: String): Pair<Boolean, String?> {
        val limpia = fechaStr.trim()
        val regex = Regex("""^(\d{2})/(\d{2})/(\d{4})$""")
        if (!regex.matches(limpia)) {
            return Pair(false, "La fecha debe tener el formato dd/mm/yyyy")
        }

        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).apply { isLenient = false }
        return try {
            val fecha = sdf.parse(limpia) ?: return Pair(false, "Fecha inválida")
            if (fecha.after(Calendar.getInstance().time)) {
                Pair(false, "La fecha de nacimiento no puede ser futura")
            } else {
                Pair(true, null)
            }
        } catch (_: Exception) {
            Pair(false, "La fecha ingresada no corresponde a un día de calendario válido")
        }
    }

    fun parsearYValidarPeso(pesoStr: String): Pair<Double?, String?> {
        val limpio = pesoStr.trim().replace(',', '.')
        val peso = limpio.toDoubleOrNull() ?: return Pair(null, "Ingresa un peso numérico válido")
        return when {
            peso <= 0.0 -> Pair(null, "El peso debe ser mayor a 0 kg")
            peso > 120.0 -> Pair(null, "El peso supera el límite biológico permitido (máx 120 kg)")
            else -> Pair(peso, null)
        }
    }

    fun validarMicrochip(microchipStr: String?): Pair<Boolean, String?> {
        if (microchipStr.isNullOrBlank()) return Pair(true, null)
        val limpio = microchipStr.trim()
        return if (!Regex("""^\d{15}$""").matches(limpio)) {
            Pair(false, "El microchip debe tener exactamente 15 dígitos numéricos")
        } else {
            Pair(true, null)
        }
    }

    fun validarTextoRequerido(valor: String, nombreCampo: String, min: Int = 2, max: Int = 40): Pair<Boolean, String?> {
        val limpio = valor.trim()
        return when {
            limpio.isBlank() -> Pair(false, "El campo $nombreCampo es obligatorio")
            limpio.length < min -> Pair(false, "$nombreCampo debe tener al menos $min caracteres")
            limpio.length > max -> Pair(false, "$nombreCampo no puede superar los $max caracteres")
            else -> Pair(true, null)
        }
    }

    // --- VALIDACIONES DE VACUNAS Y DESPARASITACIONES ---

    /**
     * Valida que la fecha de aplicación sea válida y no esté en el futuro.
     */
    fun validarFechaAplicacion(fechaStr: String): Pair<Boolean, String?> {
        val limpia = fechaStr.trim()
        val regex = Regex("""^(\d{2})/(\d{2})/(\d{4})$""")
        if (!regex.matches(limpia)) {
            return Pair(false, "Formato requerido: dd/mm/yyyy")
        }

        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).apply { isLenient = false }
        return try {
            val fecha = sdf.parse(limpia) ?: return Pair(false, "Fecha inválida")
            if (fecha.after(Calendar.getInstance().time)) {
                Pair(false, "La fecha de aplicación no puede ser una fecha futura")
            } else {
                Pair(true, null)
            }
        } catch (_: Exception) {
            Pair(false, "Día o mes no válido en el calendario")
        }
    }

    /**
     * Valida la fecha de próxima dosis: debe tener formato correcto y ser posterior a la fecha de aplicación.
     */
    fun validarProximaDosis(fechaAplicacionStr: String, fechaProximaStr: String?): Pair<Boolean, String?> {
        if (fechaProximaStr.isNullOrBlank()) return Pair(true, null)
        val limpiaProx = fechaProximaStr.trim()

        val regex = Regex("""^(\d{2})/(\d{2})/(\d{4})$""")
        if (!regex.matches(limpiaProx)) {
            return Pair(false, "La próxima fecha debe tener formato dd/mm/yyyy")
        }

        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).apply { isLenient = false }
        return try {
            val fechaProx = sdf.parse(limpiaProx) ?: return Pair(false, "Fecha próxima inválida")
            val fechaAplicacion = sdf.parse(fechaAplicacionStr.trim()) ?: return Pair(false, "Fecha aplicación base inválida")

            if (fechaProx.before(fechaAplicacion) || fechaProx == fechaAplicacion) {
                Pair(false, "La próxima dosis debe ser posterior a la fecha de aplicación")
            } else {
                Pair(true, null)
            }
        } catch (_: Exception) {
            Pair(false, "La próxima fecha no corresponde a un día de calendario válido")
        }
    }
}