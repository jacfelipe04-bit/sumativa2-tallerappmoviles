package com.example.carnetsanitario.data.remote

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.tasks.await

class FirebaseAuthDataSource(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) {
    val usuarioActual: FirebaseUser?
        get() = auth.currentUser

    val tutorIdActual: String?
        get() = auth.currentUser?.uid

    suspend fun iniciarSesion(correo: String, contrasena: String): Result<FirebaseUser> {
        return try {
            val resultado = auth.signInWithEmailAndPassword(correo, contrasena).await()
            val usuario = resultado.user ?: throw Exception("Usuario no encontrado")
            Result.success(usuario)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun registrarTutor(correo: String, contrasena: String): Result<FirebaseUser> {
        return try {
            val resultado = auth.createUserWithEmailAndPassword(correo, contrasena).await()
            val usuario = resultado.user ?: throw Exception("No se pudo crear la cuenta")
            Result.success(usuario)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun cerrarSesion() {
        auth.signOut()
    }
}