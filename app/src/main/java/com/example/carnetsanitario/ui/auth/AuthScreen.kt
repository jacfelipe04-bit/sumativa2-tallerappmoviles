package com.example.carnetsanitario.ui.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.carnetsanitario.R
import com.example.carnetsanitario.ui.theme.*

@Composable
fun AuthScreen(
    viewModel: AuthViewModel,
    onAuthSuccess: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var esModoRegistro by remember { mutableStateOf(false) }

    // Campos formulario
    var correo by remember { mutableStateOf("") }
    var contrasena by remember { mutableStateOf("") }
    var nombre by remember { mutableStateOf("") }
    var apellido by remember { mutableStateOf("") }
    var rut by remember { mutableStateOf("") }
    var telefono by remember { mutableStateOf("") }

    LaunchedEffect(uiState) {
        if (uiState is AuthUiState.Autenticado) {
            onAuthSuccess((uiState as AuthUiState.Autenticado).tutorId)
        }
    }

    Scaffold(
        containerColor = FondoPantalla
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Imagen/logo centrado arriba del título
            Image(
                painter = painterResource(id = R.drawable.ic_logo_app), // Reemplaza ic_logo_app por el nombre exacto de tu archivo en res/drawable
                contentDescription = "Logo Carnet Sanitario",
                modifier = Modifier
                    .size(100.dp)
                    .padding(bottom = 12.dp)
            )
            Text(
                text = "Carnet Sanitario",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = TextoPrimario
            )
            Text(
                text = "Cuidado y registro médico de tus mascotas",
                fontSize = 14.sp,
                color = TextoSecundario,
                modifier = Modifier.padding(top = 4.dp, bottom = 24.dp)
            )

            // Selector de pestaña (Login / Registro)
            TabRow(
                selectedTabIndex = if (esModoRegistro) 1 else 0,
                containerColor = Color.Transparent,
                contentColor = CelesteOscuro,
                divider = {}
            ) {
                Tab(
                    selected = !esModoRegistro,
                    onClick = { esModoRegistro = false; viewModel.reiniciarEstado() },
                    text = { Text("Iniciar Sesión", fontWeight = FontWeight.SemiBold) }
                )
                Tab(
                    selected = esModoRegistro,
                    onClick = { esModoRegistro = true; viewModel.reiniciarEstado() },
                    text = { Text("Registrarse", fontWeight = FontWeight.SemiBold) }
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Campos del formulario
            if (esModoRegistro) {
                OutlinedTextField(
                    value = nombre,
                    onValueChange = { nombre = it },
                    label = { Text("Nombre *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = apellido,
                    onValueChange = { apellido = it },
                    label = { Text("Apellido *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = rut,
                    onValueChange = { rut = it },
                    label = { Text("RUT * (ej: 12345678-9)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = telefono,
                    onValueChange = { telefono = it },
                    label = { Text("Teléfono * (+569...)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(10.dp))
            }

            OutlinedTextField(
                value = correo,
                onValueChange = { correo = it },
                label = { Text("Correo electrónico *") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = contrasena,
                onValueChange = { contrasena = it },
                label = { Text("Contraseña *") },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            if (uiState is AuthUiState.Error) {
                Text(
                    text = (uiState as AuthUiState.Error).mensaje,
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(top = 10.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    if (esModoRegistro) {
                        viewModel.registrarTutor(
                            nombre, apellido, rut, correo, contrasena,
                            telefono
                        )
                    } else {
                        viewModel.iniciarSesion(correo, contrasena)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = CelesteOscuro),
                enabled = uiState !is AuthUiState.Cargando
            ) {
                if (uiState is AuthUiState.Cargando) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text(
                        text = if (esModoRegistro) "Crear Cuenta de Tutor" else "Iniciar Sesión",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}