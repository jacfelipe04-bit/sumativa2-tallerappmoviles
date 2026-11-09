package com.example.carnetsanitario.data.local.enums

enum class TipoVacuna(val nombreLegible: String, val especie: Especie?) {
    // Caninos
    OCHO_VALENTE("Óctuple / Hexavalente", Especie.CANINO),
    PUPPY("Puppy (Parvovirus/Distemper)", Especie.CANINO),
    KC("KC / Tos de las Perreras (Bordetella)", Especie.CANINO),

    // Felinos
    TRIPLE_FELINA("Triple Felina", Especie.FELINO),
    LEUCEMIA_FELINA("Leucemia Felina (FeLV)", Especie.FELINO),

    // Ambas especies
    ANTIRRABICA("Antirrábica", null),
    OTRA("Otra vacuna", null);

    companion object {
        fun obtenerPorEspecie(especie: Especie): List<TipoVacuna> {
            return entries.filter { it.especie == null || it.especie == especie }
        }
    }
}