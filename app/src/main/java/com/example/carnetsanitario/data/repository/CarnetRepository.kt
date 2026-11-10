package com.example.carnetsanitario.data.repository

import com.example.carnetsanitario.data.local.AppDatabase
import com.example.carnetsanitario.data.local.entity.DesparasitacionEntity
import com.example.carnetsanitario.data.local.entity.MascotaEntity
import com.example.carnetsanitario.data.local.entity.TutorEntity
import com.example.carnetsanitario.data.local.entity.VacunaEntity
import com.example.carnetsanitario.data.remote.FirebaseAuthDataSource
import com.example.carnetsanitario.data.remote.FirebaseFirestoreDataSource
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import java.text.Normalizer

class CarnetRepository(
    private val db: AppDatabase,
    val authDataSource: FirebaseAuthDataSource = FirebaseAuthDataSource(),
    private val firestoreDataSource: FirebaseFirestoreDataSource = FirebaseFirestoreDataSource()
) {

    private var listenerMascotas: ListenerRegistration? = null
    private var listenerTutor: ListenerRegistration? = null
    private val listenersRegistros = mutableListOf<ListenerRegistration>()
    private val mascotasConListener = mutableSetOf<String>()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    // --- SINCRONIZACIÓN EN TIEMPO REAL (FIRESTORE -> ROOM) ---

    fun iniciarSincronizacionMascotas(tutorId: String, scope: CoroutineScope) {
        detenerSincronizacionMascotas()

        // 1. SINCRONIZAR TUTOR (Nube -> Room)
        listenerTutor = firestore.collection("tutores").document(tutorId)
            .addSnapshotListener { doc, error ->
                if (error != null || doc == null || !doc.exists()) {
                    android.util.Log.e("CarnetSync", "Error o tutor no encontrado: ${error?.message}")
                    return@addSnapshotListener
                }

                try {
                    val tutor = TutorEntity(
                        id = doc.getString("id") ?: tutorId,
                        nombre = doc.getString("nombre") ?: "",
                        apellido = doc.getString("apellido") ?: "",
                        rut = doc.getString("rut") ?: "",
                        correoElectronico = doc.getString("correoElectronico") ?: "",
                        telefono = doc.getString("telefono") ?: "",
                        direccion = doc.getString("direccion"),
                        region = doc.getString("region"),
                        comuna = doc.getString("comuna")
                    )

                    scope.launch {
                        db.tutorDao().insertarOActualizar(tutor)
                        android.util.Log.d("CarnetSync", "Tutor sincronizado en Room: ${tutor.nombre} ${tutor.apellido}")
                    }
                } catch (e: Exception) {
                    android.util.Log.e("CarnetSync", "Fallo al procesar tutor: ${e.message}")
                }
            }

        // 2. SINCRONIZAR MASCOTAS (Nube -> Room)
        listenerMascotas = firestore.collection("tutores")
            .document(tutorId)
            .collection("mascotas")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    android.util.Log.e("CarnetSync", "Error en snapshot mascotas: ${error.message}")
                    return@addSnapshotListener
                }
                if (snapshot == null) return@addSnapshotListener

                android.util.Log.d("CarnetSync", "Total documentos mascotas recibidos: ${snapshot.size()}")

                val mascotasRemotas = snapshot.documents.mapNotNull { doc ->
                    try {
                        val id = doc.getString("id") ?: doc.id
                        val nombre = doc.getString("nombre") ?: return@mapNotNull null

                        val especieStr = doc.getString("especie") ?: ""
                        val especie = try {
                            com.example.carnetsanitario.data.local.enums.Especie.valueOf(especieStr)
                        } catch (_: Exception) {
                            com.example.carnetsanitario.data.local.enums.Especie.entries.first()
                        }

                        val sexoStr = doc.getString("sexo") ?: ""
                        val sexo = try {
                            com.example.carnetsanitario.data.local.enums.Sexo.valueOf(sexoStr)
                        } catch (_: Exception) {
                            com.example.carnetsanitario.data.local.enums.Sexo.entries.first()
                        }

                        val estadoRepStr = doc.getString("estadoReproductivo") ?: ""
                        val estadoRep = try {
                            com.example.carnetsanitario.data.local.enums.EstadoReproductivo.valueOf(estadoRepStr)
                        } catch (_: Exception) {
                            com.example.carnetsanitario.data.local.enums.EstadoReproductivo.entries.first()
                        }

                        val peso = doc.getDouble("peso")
                            ?: doc.getLong("peso")?.toDouble()
                            ?: 0.0

                        val activa = doc.getBoolean("activa") ?: true
                        val esEdadEstimada = doc.getBoolean("esEdadEstimada") ?: false

                        MascotaEntity(
                            id = id,
                            tutorId = doc.getString("tutorId") ?: tutorId,
                            nombre = nombre,
                            especie = especie,
                            sexo = sexo,
                            raza = doc.getString("raza") ?: "",
                            color = doc.getString("color") ?: "",
                            fechaNacimiento = doc.getString("fechaNacimiento") ?: "",
                            esEdadEstimada = esEdadEstimada,
                            peso = peso,
                            estadoReproductivo = estadoRep,
                            numeroMicrochip = doc.getString("numeroMicrochip"),
                            fotoUri = doc.getString("fotoUri"), // Admite tanto Path local como String Base64
                            activa = activa
                        )
                    } catch (e: Exception) {
                        android.util.Log.e("CarnetSync", "Error mapeando mascota ${doc.id}: ${e.message}")
                        null
                    }
                }

                scope.launch {
                    try {
                        if (mascotasRemotas.isNotEmpty()) {
                            db.mascotaDao().insertarMascotas(mascotasRemotas)
                            val idsRemotos = mascotasRemotas.map { it.id }
                            db.mascotaDao().eliminarMascotasNoPresentes(tutorId, idsRemotos)
                            android.util.Log.d("CarnetSync", "Insertadas ${mascotasRemotas.size} mascotas en Room")

                            // 3. SINCRONIZAR SUBCOLECCIONES DE CADA MASCOTA (Vacunas y Desparasitaciones)
                            mascotasRemotas.forEach { mascota ->
                                sincronizarRegistrosSanitarios(tutorId, mascota.id, scope)
                            }
                        } else {
                            db.mascotaDao().eliminarTodasPorTutor(tutorId)
                        }
                    } catch (e: Exception) {
                        android.util.Log.e("CarnetSync", "Error al escribir en Room MascotaDao: ${e.message}")
                    }
                }
            }
    }
    private fun sincronizarRegistrosSanitarios(tutorId: String, mascotaId: String, scope: CoroutineScope) {
        if (!mascotasConListener.add(mascotaId)) return

        val refMascota = firestore.collection("tutores")
            .document(tutorId)
            .collection("mascotas")
            .document(mascotaId)

        // Listener en tiempo real para Vacunas
        val regVacunas = refMascota.collection("vacunas")
            .addSnapshotListener { snap, err ->
                if (err != null || snap == null) return@addSnapshotListener
                val vacunas = snap.documents.mapNotNull { d ->
                    try {
                        val id = d.getString("id") ?: d.id
                        val fechaAplicacion = d.getString("fechaAplicacion") ?: return@mapNotNull null
                        val nombreVacuna = d.getString("nombreVacuna") ?: return@mapNotNull null
                        val proximaDosis = d.getString("proximaDosis")

                        VacunaEntity(
                            id = id,
                            mascotaId = d.getString("mascotaId") ?: mascotaId,
                            fechaAplicacion = fechaAplicacion,
                            nombreVacuna = nombreVacuna,
                            proximaDosis = proximaDosis
                        )
                    } catch (_: Exception) {
                        null
                    }
                }
                scope.launch {
                    vacunas.forEach { db.vacunaDao().insertarVacuna(it) }
                }
            }
        listenersRegistros.add(regVacunas)

        // Listener en tiempo real para Desparasitaciones
        val regDesp = refMascota.collection("desparasitaciones")
            .addSnapshotListener { snap, err ->
                if (err != null || snap == null) return@addSnapshotListener
                val desparasitaciones = snap.documents.mapNotNull { d ->
                    try {
                        val id = d.getString("id") ?: d.id
                        val fechaAplicacion = d.getString("fechaAplicacion") ?: return@mapNotNull null
                        val nombreAntiparasitario = d.getString("nombreAntiparasitario") ?: return@mapNotNull null
                        val proximaDosis = d.getString("proximaDosis")

                        DesparasitacionEntity(
                            id = id,
                            mascotaId = d.getString("mascotaId") ?: mascotaId,
                            fechaAplicacion = fechaAplicacion,
                            nombreAntiparasitario = nombreAntiparasitario,
                            proximaDosis = proximaDosis
                        )
                    } catch (_: Exception) {
                        null
                    }
                }
                scope.launch {
                    desparasitaciones.forEach { db.desparasitacionDao().insertarDesparasitacion(it) }
                }
            }
        listenersRegistros.add(regDesp)
    }

    fun detenerSincronizacionMascotas() {
        listenerTutor?.remove()
        listenerTutor = null

        listenerMascotas?.remove()
        listenerMascotas = null

        listenersRegistros.forEach { it.remove() }
        listenersRegistros.clear()
        mascotasConListener.clear()
    }

    // --- GESTIÓN DE TUTOR ---

    fun obtenerTutor(tutorId: String): Flow<TutorEntity?> =
        db.tutorDao().obtenerTutorPorId(tutorId)

    suspend fun registrarOActualizarTutor(tutor: TutorEntity) {
        db.tutorDao().insertarOActualizar(tutor)
        try {
            firestoreDataSource.guardarTutor(tutor)
        } catch (_: Exception) {
            // Se mantiene persistido localmente si no hay conexión
        }
    }

    // --- GESTIÓN DE MASCOTAS ---

    fun obtenerMascotasActivas(tutorId: String): Flow<List<MascotaEntity>> =
        db.mascotaDao().obtenerMascotasActivas(tutorId)

    fun obtenerMascotasInactivas(tutorId: String): Flow<List<MascotaEntity>> =
        db.mascotaDao().obtenerMascotasInactivas(tutorId)

    fun obtenerMascotaPorId(mascotaId: String): Flow<MascotaEntity?> =
        db.mascotaDao().obtenerMascotaPorId(mascotaId)

    suspend fun guardarMascota(mascota: MascotaEntity) {
        db.mascotaDao().insertarMascota(mascota)
        try {
            firestoreDataSource.guardarMascota(mascota)
        } catch (_: Exception) {
            // Guardado en Room asegurado
        }
    }

    suspend fun eliminarMascota(mascota: MascotaEntity) {
        db.mascotaDao().eliminarMascota(mascota)
        try {
            firestoreDataSource.eliminarMascota(mascota.tutorId, mascota.id)
        } catch (_: Exception) {
            // Continúa sin romper ejecución
        }
    }

    // --- GESTIÓN DE VACUNAS ---

    fun obtenerVacunas(mascotaId: String): Flow<List<VacunaEntity>> =
        db.vacunaDao().obtenerVacunasPorMascota(mascotaId)

    suspend fun registrarVacuna(tutorId: String, vacuna: VacunaEntity) {
        db.vacunaDao().insertarVacuna(vacuna)
        try {
            firestoreDataSource.guardarVacuna(tutorId, vacuna)
        } catch (_: Exception) {}
    }

    suspend fun eliminarVacuna(tutorId: String, vacuna: VacunaEntity) {
        db.vacunaDao().eliminarVacuna(vacuna)
        try {
            firestoreDataSource.eliminarVacuna(tutorId, vacuna.mascotaId, vacuna.id)
        } catch (_: Exception) {}
    }

    // --- GESTIÓN DE DESPARASITACIONES ---

    fun obtenerDesparasitaciones(mascotaId: String): Flow<List<DesparasitacionEntity>> =
        db.desparasitacionDao().obtenerDesparasitacionesPorMascota(mascotaId)

    suspend fun registrarDesparasitacion(tutorId: String, desparasitacion: DesparasitacionEntity) {
        db.desparasitacionDao().insertarDesparasitacion(desparasitacion)
        try {
            firestoreDataSource.guardarDesparasitacion(tutorId, desparasitacion)
        } catch (_: Exception) {}
    }

    suspend fun eliminarDesparasitacion(tutorId: String, desparasitacion: DesparasitacionEntity) {
        db.desparasitacionDao().eliminarDesparasitacion(desparasitacion)
        try {
            firestoreDataSource.eliminarDesparasitacion(tutorId, desparasitacion.mascotaId, desparasitacion.id)
        } catch (_: Exception) {}
    }

    // --- ACTUALIZACIÓN Y EDICIÓN ---

    suspend fun actualizarMascota(mascota: MascotaEntity) {
        db.mascotaDao().actualizarMascota(mascota)
        try {
            firestoreDataSource.guardarMascota(mascota)
        } catch (_: Exception) {}
    }

    suspend fun verificarNombreExistente(tutorId: String, nombre: String, mascotaIdExcluir: String? = null): Boolean {
        val lista = db.mascotaDao().obtenerTodasPorTutorSync(tutorId)
        val normalizado = Normalizer.normalize(nombre.trim(), Normalizer.Form.NFD)
            .replace("\\p{InCombiningDiacriticalMarks}+".toRegex(), "")
            .lowercase()

        return lista.any { m ->
            if (mascotaIdExcluir != null && m.id == mascotaIdExcluir) return@any false
            val mNormalizado = Normalizer.normalize(m.nombre.trim(), Normalizer.Form.NFD)
                .replace("\\p{InCombiningDiacriticalMarks}+".toRegex(), "")
                .lowercase()
            mNormalizado == normalizado
        }
    }

    suspend fun actualizarVacuna(tutorId: String, vacuna: VacunaEntity) {
        db.vacunaDao().actualizarVacuna(vacuna)
        try {
            firestoreDataSource.guardarVacuna(tutorId, vacuna)
        } catch (_: Exception) {}
    }

    suspend fun actualizarDesparasitacion(tutorId: String, desparasitacion: DesparasitacionEntity) {
        db.desparasitacionDao().actualizarDesparasitacion(desparasitacion)
        try {
            firestoreDataSource.guardarDesparasitacion(tutorId, desparasitacion)
        } catch (_: Exception) {}
    }
}