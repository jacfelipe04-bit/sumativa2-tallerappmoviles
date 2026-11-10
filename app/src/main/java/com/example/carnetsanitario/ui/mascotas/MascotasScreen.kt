package com.example.carnetsanitario.ui.mascotas

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.carnetsanitario.data.local.entity.MascotaEntity
import com.example.carnetsanitario.data.local.entity.TutorEntity
import com.example.carnetsanitario.data.local.enums.Especie
import com.example.carnetsanitario.data.local.enums.EstadoReproductivo
import com.example.carnetsanitario.data.local.enums.Sexo
import com.example.carnetsanitario.ui.theme.*
import com.example.carnetsanitario.ui.util.verticalScrollbar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MascotasScreen(
    viewModel: MascotasViewModel,
    onMascotaClick: (String) -> Unit,
    onCerrarSesion: () -> Unit
) {
    val context = LocalContext.current
    val mascotas by viewModel.listaMascotas.collectAsState()
    val soloActivas by viewModel.mostrarSoloActivas.collectAsState()
    val mensajeError by viewModel.mensajeError.collectAsState()
    val mensajeExito by viewModel.mensajeExito.collectAsState()
    val tutor by viewModel.tutorActual.collectAsState()

    var mostrarDialogoAgregar by remember { mutableStateOf(false) }
    var mascotaAEditar by remember { mutableStateOf<MascotaEntity?>(null) }
    var mostrarDialogoTutor by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(mensajeExito) {
        mensajeExito?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.limpiarMensajes()
        }
    }

    Scaffold(
        containerColor = FondoPantalla,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Mis Mascotas", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = TextoPrimario)
                        tutor?.let {
                            Text("Tutor: ${it.nombre} ${it.apellido}", fontSize = 12.sp, color = TextoSecundario)
                        }
                    }
                },
                actions = {
                    TextButton(onClick = { mostrarDialogoTutor = true }) {
                        Text("Perfil", color = CelesteOscuro, fontWeight = FontWeight.SemiBold)
                    }
                    TextButton(onClick = { viewModel.cerrarSesion(onCerrarSesion) }) {
                        Text("Salir", color = RosaOscuro, fontWeight = FontWeight.SemiBold)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = FondoPantalla)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { mostrarDialogoAgregar = true },
                containerColor = CelesteOscuro,
                contentColor = Color.White
            ) {
                Text("+", fontSize = 28.sp, fontWeight = FontWeight.Bold)
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = soloActivas,
                    onClick = { viewModel.alternarFiltroActivas(true) },
                    label = { Text("Activas") }
                )
                FilterChip(
                    selected = !soloActivas,
                    onClick = { viewModel.alternarFiltroActivas(false) },
                    label = { Text("Archivadas") }
                )
            }

            if (mascotas.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (soloActivas) "No tienes mascotas activas.\nToca + para registrar una." else "No hay mascotas archivadas.",
                        color = TextoSecundario,
                        fontSize = 15.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    items(mascotas, key = { it.id }) { mascota ->
                        TarjetaMascota(
                            mascota = mascota,
                            onClick = { onMascotaClick(mascota.id) },
                            onEditar = { mascotaAEditar = mascota },
                            onCambiarEstado = { viewModel.cambiarEstadoActivo(mascota) },
                            onEliminar = { viewModel.eliminarMascotaDefinitiva(mascota) }
                        )
                    }
                }
            }
        }
    }

    // Modal Registrar
    if (mostrarDialogoAgregar) {
        DialogoFormularioMascota(
            titulo = "Registrar Mascota",
            mascotaExistente = null,
            error = mensajeError,
            onDismiss = {
                mostrarDialogoAgregar = false
                viewModel.limpiarMensajes()
            },
            onGuardar = { nombre, especie, sexo, raza, color, fechaNac, esEstimada, peso, repro, chip, fotoUri ->
                viewModel.registrarMascota(
                    context = context,
                    nombre = nombre,
                    especie = especie,
                    sexo = sexo,
                    raza = raza,
                    color = color,
                    fechaNacimiento = fechaNac,
                    esEdadEstimada = esEstimada,
                    pesoTexto = peso,
                    estadoReproductivo = repro,
                    numeroMicrochip = chip,
                    fotoUri = fotoUri
                ) {
                    mostrarDialogoAgregar = false
                }
            }
        )
    }

    // Modal Editar
    mascotaAEditar?.let { mascota ->
        DialogoFormularioMascota(
            titulo = "Editar Mascota",
            mascotaExistente = mascota,
            error = mensajeError,
            onDismiss = {
                mascotaAEditar = null
                viewModel.limpiarMensajes()
            },
            onGuardar = { nombre, _, sexo, raza, color, fechaNac, esEstimada, peso, repro, chip, fotoUri ->
                viewModel.actualizarMascota(
                    context = context,
                    mascotaOriginal = mascota,
                    nombre = nombre,
                    sexo = sexo,
                    raza = raza,
                    color = color,
                    fechaNacimiento = fechaNac,
                    esEdadEstimada = esEstimada,
                    pesoTexto = peso,
                    estadoReproductivo = repro,
                    numeroMicrochip = chip,
                    fotoUri = fotoUri
                ) {
                    mascotaAEditar = null
                }
            }
        )
    }

    // Modal Tutor
    if (mostrarDialogoTutor && tutor != null) {
        DialogoEditarTutor(
            tutor = tutor!!,
            error = mensajeError,
            onDismiss = {
                mostrarDialogoTutor = false
                viewModel.limpiarMensajes()
            },
            onGuardar = { nombre, apellido, rut, tel, dir, reg, com ->
                viewModel.actualizarDatosTutor(nombre, apellido, rut, tel, dir, reg, com) {
                    mostrarDialogoTutor = false
                }
            }
        )
    }
}

@Composable
fun TarjetaMascota(
    mascota: MascotaEntity,
    onClick: () -> Unit,
    onEditar: () -> Unit,
    onCambiarEstado: () -> Unit,
    onEliminar: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SuperficieTarjeta),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                MascotaAvatar(
                    fotoUri = mascota.fotoUri,
                    especie = mascota.especie,
                    tamano = 56.dp
                )

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(mascota.nombre, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextoPrimario)
                    Text("${mascota.raza} • ${mascota.peso} kg", fontSize = 13.sp, color = TextoSecundario)
                    Text("Color: ${mascota.color}", fontSize = 12.sp, color = TextoSecundario)
                    Text(
                        text = "Nacimiento: ${mascota.fechaNacimiento} ${if (mascota.esEdadEstimada) "(estimada)" else ""}",
                        fontSize = 12.sp,
                        color = TextoSecundario
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = BordeSuave)
            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = onEditar) {
                    Text("Editar", fontSize = 12.sp, color = CelesteOscuro)
                }
                TextButton(onClick = onCambiarEstado) {
                    Text(if (mascota.activa) "Archivar" else "Reactivar", fontSize = 12.sp, color = TextoSecundario)
                }
                if (!mascota.activa) {
                    TextButton(onClick = onEliminar) {
                        Text("Eliminar", fontSize = 12.sp, color = RosaOscuro, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DialogoFormularioMascota(
    titulo: String,
    mascotaExistente: MascotaEntity?,
    error: String?,
    onDismiss: () -> Unit,
    onGuardar: (
        nombre: String,
        especie: Especie,
        sexo: Sexo,
        raza: String,
        color: String,
        fechaNac: String,
        esEstimada: Boolean,
        peso: String,
        reproductivo: EstadoReproductivo,
        microchip: String?,
        fotoUri: String?
    ) -> Unit
) {
    var nombre by remember { mutableStateOf(mascotaExistente?.nombre ?: "") }
    var especie by remember { mutableStateOf(mascotaExistente?.especie ?: Especie.CANINO) }
    var sexo by remember { mutableStateOf(mascotaExistente?.sexo ?: Sexo.MACHO) }
    var raza by remember { mutableStateOf(mascotaExistente?.raza ?: "") }
    var colorPelaje by remember { mutableStateOf(mascotaExistente?.color ?: "") }
    var fechaNac by remember { mutableStateOf(mascotaExistente?.fechaNacimiento ?: "") }
    var esEdadEstimada by remember { mutableStateOf(mascotaExistente?.esEdadEstimada ?: false) }
    var peso by remember { mutableStateOf(mascotaExistente?.peso?.toString() ?: "") }
    var estadoReproductivo by remember { mutableStateOf(mascotaExistente?.estadoReproductivo ?: EstadoReproductivo.NO_SABE) }
    var microchip by remember { mutableStateOf(mascotaExistente?.numeroMicrochip ?: "") }
    var fotoUriLocal by remember { mutableStateOf(mascotaExistente?.fotoUri) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            fotoUriLocal = uri.toString()
        }
    }

    val scrollState = rememberScrollState()

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = SuperficieTarjeta,
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.88f)
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // 1. Cabecera fija
                Text(
                    text = titulo,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextoPrimario,
                    modifier = Modifier.padding(top = 20.dp, start = 20.dp, end = 20.dp, bottom = 8.dp)
                )

                // 2. Formulario con barra lateral visible y scroll independiente
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScrollbar(state = scrollState, color = CelesteOscuro.copy(alpha = 0.55f))
                        .verticalScroll(scrollState)
                        .padding(horizontal = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Selector de foto interactivo
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        MascotaAvatar(
                            fotoUri = fotoUriLocal,
                            especie = especie,
                            tamano = 80.dp,
                            onClick = {
                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            }
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (fotoUriLocal != null) "Toca la imagen para cambiarla" else "Toca para agregar foto",
                            fontSize = 12.sp,
                            color = CelesteOscuro,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.clickable {
                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedTextField(
                        value = nombre,
                        onValueChange = { nombre = it },
                        label = { Text("Nombre de la mascota *") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (mascotaExistente == null) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Especie *",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = TextoSecundario,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilterChip(
                                selected = especie == Especie.CANINO,
                                onClick = { especie = Especie.CANINO },
                                label = { Text("Canino 🐶") }
                            )
                            FilterChip(
                                selected = especie == Especie.FELINO,
                                onClick = { especie = Especie.FELINO },
                                label = { Text("Felino 🐱") }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Sexo *",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextoSecundario,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = sexo == Sexo.MACHO,
                            onClick = { sexo = Sexo.MACHO },
                            label = { Text("Macho") }
                        )
                        FilterChip(
                            selected = sexo == Sexo.HEMBRA,
                            onClick = { sexo = Sexo.HEMBRA },
                            label = { Text("Hembra") }
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = raza,
                        onValueChange = { raza = it },
                        label = { Text("Raza *") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = colorPelaje,
                        onValueChange = { colorPelaje = it },
                        label = { Text("Color *") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = fechaNac,
                        onValueChange = { fechaNac = it },
                        label = { Text("Fecha de nacimiento * (dd/mm/yyyy)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { esEdadEstimada = !esEdadEstimada }
                    ) {
                        Checkbox(checked = esEdadEstimada, onCheckedChange = { esEdadEstimada = it })
                        Text("Es fecha/edad estimada", fontSize = 13.sp, color = TextoPrimario)
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = peso,
                        onValueChange = { peso = it },
                        label = { Text("Peso en kg * (ej: 4.5)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Estado Reproductivo *",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextoSecundario,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        FilterChip(
                            selected = estadoReproductivo == EstadoReproductivo.CASTRADO,
                            onClick = { estadoReproductivo = EstadoReproductivo.CASTRADO },
                            label = { Text("Castrado") }
                        )
                        FilterChip(
                            selected = estadoReproductivo == EstadoReproductivo.ENTERO,
                            onClick = { estadoReproductivo = EstadoReproductivo.ENTERO },
                            label = { Text("Entero") }
                        )
                        FilterChip(
                            selected = estadoReproductivo == EstadoReproductivo.NO_SABE,
                            onClick = { estadoReproductivo = EstadoReproductivo.NO_SABE },
                            label = { Text("No sabe") }
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = microchip,
                        onValueChange = { microchip = it },
                        label = { Text("N° Microchip (Opcional)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (error != null) {
                        Text(
                            text = error,
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 12.sp,
                            modifier = Modifier
                                .padding(top = 10.dp)
                                .fillMaxWidth()
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }

                // 3. Barra inferior de botones fija
                HorizontalDivider(color = BordeSuave)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancelar", color = TextoSecundario)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Button(
                        onClick = {
                            onGuardar(
                                nombre,
                                especie,
                                sexo,
                                raza,
                                colorPelaje,
                                fechaNac,
                                esEdadEstimada,
                                peso,
                                estadoReproductivo,
                                microchip,
                                fotoUriLocal
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CelesteOscuro),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(if (mascotaExistente == null) "Registrar" else "Guardar Cambios")
                    }
                }
            }
        }
    }
}

@Composable
fun DialogoEditarTutor(
    tutor: TutorEntity,
    error: String?,
    onDismiss: () -> Unit,
    onGuardar: (
        nombre: String,
        apellido: String,
        rut: String,
        telefono: String,
        direccion: String?,
        region: String?,
        comuna: String?
    ) -> Unit
) {
    var nombre by remember { mutableStateOf(tutor.nombre) }
    var apellido by remember { mutableStateOf(tutor.apellido) }
    var rut by remember { mutableStateOf(tutor.rut) }
    var telefono by remember { mutableStateOf(tutor.telefono) }
    var direccion by remember { mutableStateOf(tutor.direccion ?: "") }
    var region by remember { mutableStateOf(tutor.region ?: "") }
    var comuna by remember { mutableStateOf(tutor.comuna ?: "") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = SuperficieTarjeta,
            modifier = Modifier.fillMaxWidth().fillMaxHeight(0.85f)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text("Perfil del Tutor", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = TextoPrimario)
                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(value = nombre, onValueChange = { nombre = it }, label = { Text("Nombre *") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = apellido, onValueChange = { apellido = it }, label = { Text("Apellido *") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = rut, onValueChange = { rut = it }, label = { Text("RUT *") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = telefono, onValueChange = { telefono = it }, label = { Text("Teléfono *") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = direccion, onValueChange = { direccion = it }, label = { Text("Dirección") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = region, onValueChange = { region = it }, label = { Text("Región") }, modifier = Modifier.weight(1f))
                    OutlinedTextField(value = comuna, onValueChange = { comuna = it }, label = { Text("Comuna") }, modifier = Modifier.weight(1f))
                }

                if (error != null) {
                    Text(text = error, color = MaterialTheme.colorScheme.error, fontSize = 12.sp, modifier = Modifier.padding(top = 8.dp))
                }

                Spacer(modifier = Modifier.height(20.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("Cancelar") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = { onGuardar(nombre, apellido, rut, telefono, direccion, region, comuna) },
                        colors = ButtonDefaults.buttonColors(containerColor = CelesteOscuro)
                    ) {
                        Text("Guardar Cambios")
                    }
                }
            }
        }
    }
}