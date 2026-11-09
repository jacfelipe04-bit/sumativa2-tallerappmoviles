package com.example.carnetsanitario.data.remote

import com.example.carnetsanitario.data.local.entity.DesparasitacionEntity
import com.example.carnetsanitario.data.local.entity.MascotaEntity
import com.example.carnetsanitario.data.local.entity.TutorEntity
import com.example.carnetsanitario.data.local.entity.VacunaEntity
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class FirebaseFirestoreDataSource(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    // --- TUTOR ---
    suspend fun guardarTutor(tutor: TutorEntity) {
        firestore.collection("tutores")
            .document(tutor.id)
            .set(tutor)
            .await()
    }

    // --- MASCOTAS ---
    suspend fun guardarMascota(mascota: MascotaEntity) {
        firestore.collection("tutores")
            .document(mascota.tutorId)
            .collection("mascotas")
            .document(mascota.id)
            .set(mascota)
            .await()
    }

    suspend fun eliminarMascota(tutorId: String, mascotaId: String) {
        firestore.collection("tutores")
            .document(tutorId)
            .collection("mascotas")
            .document(mascotaId)
            .delete()
            .await()
    }

    // --- VACUNAS ---
    suspend fun guardarVacuna(tutorId: String, vacuna: VacunaEntity) {
        firestore.collection("tutores")
            .document(tutorId)
            .collection("mascotas")
            .document(vacuna.mascotaId)
            .collection("vacunas")
            .document(vacuna.id)
            .set(vacuna)
            .await()
    }

    suspend fun eliminarVacuna(tutorId: String, mascotaId: String, vacunaId: String) {
        firestore.collection("tutores")
            .document(tutorId)
            .collection("mascotas")
            .document(mascotaId)
            .collection("vacunas")
            .document(vacunaId)
            .delete()
            .await()
    }

    // --- DESPARASITACIONES ---
    suspend fun guardarDesparasitacion(tutorId: String, desparasitacion: DesparasitacionEntity) {
        firestore.collection("tutores")
            .document(tutorId)
            .collection("mascotas")
            .document(desparasitacion.mascotaId)
            .collection("desparasitaciones")
            .document(desparasitacion.id)
            .set(desparasitacion)
            .await()
    }

    suspend fun eliminarDesparasitacion(tutorId: String, mascotaId: String, desparasitacionId: String) {
        firestore.collection("tutores")
            .document(tutorId)
            .collection("mascotas")
            .document(mascotaId)
            .collection("desparasitaciones")
            .document(desparasitacionId)
            .delete()
            .await()
    }
}