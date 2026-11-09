package com.example.carnetsanitario.ui.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.carnetsanitario.data.repository.CarnetRepository
import com.example.carnetsanitario.ui.auth.AuthScreen
import com.example.carnetsanitario.ui.auth.AuthViewModel
import com.example.carnetsanitario.ui.carnet.CarnetScreen
import com.example.carnetsanitario.ui.carnet.CarnetViewModel
import com.example.carnetsanitario.ui.mascotas.MascotasScreen
import com.example.carnetsanitario.ui.mascotas.MascotasViewModel

sealed class Destino(val ruta: String) {
    data object Auth : Destino("auth")
    data object Mascotas : Destino("mascotas/{tutorId}") {
        fun crearRuta(tutorId: String) = "mascotas/$tutorId"
    }
    data object Carnet : Destino("carnet/{tutorId}/{mascotaId}") {
        fun crearRuta(tutorId: String, mascotaId: String) = "carnet/$tutorId/$mascotaId"
    }
}

@Composable
fun AppNavigation(
    repository: CarnetRepository
) {
    val navController = rememberNavController()
    val tutorIdInicial = repository.authDataSource.tutorIdActual
    val puntoDePartida = if (tutorIdInicial != null) Destino.Mascotas.crearRuta(tutorIdInicial) else Destino.Auth.ruta

    NavHost(
        navController = navController,
        startDestination = puntoDePartida
    ) {
        // Pantalla 1: Autenticación
        composable(Destino.Auth.ruta) {
            val authViewModel: AuthViewModel = viewModel { AuthViewModel(repository) }
            AuthScreen(
                viewModel = authViewModel,
                onAuthSuccess = { tutorId ->
                    navController.navigate(Destino.Mascotas.crearRuta(tutorId)) {
                        popUpTo(Destino.Auth.ruta) { inclusive = true }
                    }
                }
            )
        }

        // Pantalla 2: Listado de Mascotas
        composable(
            route = Destino.Mascotas.ruta,
            arguments = listOf(navArgument("tutorId") { type = NavType.StringType })
        ) { backStackEntry ->
            val tutorId = backStackEntry.arguments?.getString("tutorId") ?: ""
            val mascotasViewModel: MascotasViewModel = viewModel { MascotasViewModel(repository, tutorId) }

            MascotasScreen(
                viewModel = mascotasViewModel,
                onMascotaClick = { mascotaId ->
                    navController.navigate(Destino.Carnet.crearRuta(tutorId, mascotaId))
                },
                onCerrarSesion = {
                    navController.navigate(Destino.Auth.ruta) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        // Pantalla 3: Carnet Sanitario (Vacunas y Desparasitaciones)
        composable(
            route = Destino.Carnet.ruta,
            arguments = listOf(
                navArgument("tutorId") { type = NavType.StringType },
                navArgument("mascotaId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val tutorId = backStackEntry.arguments?.getString("tutorId") ?: ""
            val mascotaId = backStackEntry.arguments?.getString("mascotaId") ?: ""
            val carnetViewModel: CarnetViewModel = viewModel {
                CarnetViewModel(repository, mascotaId, tutorId)
            }

            CarnetScreen(
                viewModel = carnetViewModel,
                onBack = { navController.popBackStack() }
            )
        }
    }
}