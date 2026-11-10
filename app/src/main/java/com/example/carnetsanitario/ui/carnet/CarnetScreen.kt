package com.example.carnetsanitario.ui.carnet

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.carnetsanitario.data.local.entity.DesparasitacionEntity
import com.example.carnetsanitario.data.local.entity.VacunaEntity
import com.example.carnetsanitario.data.local.enums.Especie
import com.example.carnetsanitario.data.local.enums.TipoVacuna
import com.example.carnetsanitario.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CarnetScreen(
    viewModel: CarnetViewModel,
    onBack: () -> Unit
) {
    val mascota by viewModel.mascota.collectAsState()
    val vacunas by viewModel.vacunas.collectAsState()
    val desparasitaciones by viewModel.desparasitaciones.collectAsState()
    val mensajeError by viewModel.mensajeError.collectAsState()

    var pestanaSeleccionada by remember { mutableIntStateOf(0) }
    var mostrarDialogoVacuna by remember { mutableStateOf(false) }
    var vacunaAEditar by remember { mutableStateOf<VacunaEntity?>(null) }

    var mostrarDialogoDesparasitacion by remember { mutableStateOf(false) }
    var desparasitacionAEditar by remember { mutableStateOf<DesparasitacionEntity?>(null) }

    Scaffold(
        containerColor = FondoPantalla,
        topBar = {
            TopAppBar(
                title = { Text(mascota?.nombre ?: "Carnet Sanitario", fontWeight = FontWeight.Bold, color = TextoPrimario) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Text("←", fontSize = 24.sp, color = TextoPrimario, fontWeight = FontWeight.Bold)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = FondoPantalla)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    if (pestanaSeleccionada == 0) mostrarDialogoVacuna = true
                    else mostrarDialogoDesparasitacion = true
                },
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
            // Ficha Completa de la Mascota
            mascota?.let { m ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SuperficieTarjeta),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            com.example.carnetsanitario.ui.mascotas.MascotaAvatar(
                                fotoUri = m.fotoUri,
                                especie = m.especie,
                                tamano = 56.dp
                            )
                            Spacer(modifier = Modifier.width(14.dp))
                            Column {
                                Text(m.nombre, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = TextoPrimario)
                                Text("${m.raza} • ${m.sexo.name.lowercase().replaceFirstChar { it.uppercase() }}", fontSize = 13.sp, color = TextoSecundario)
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Divider(color = BordeSuave)
                        Spacer(modifier = Modifier.height(8.dp))

                        // Detalles visibles completos
                        Text("Color: ${m.color}", fontSize = 12.sp, color = TextoPrimario)
                        Text("Nacimiento: ${m.fechaNacimiento} ${if (m.esEdadEstimada) "(estimada)" else ""}", fontSize = 12.sp, color = TextoPrimario)
                        Text("Peso: ${m.peso} kg • Estado: ${m.estadoReproductivo.name.lowercase().replaceFirstChar { it.uppercase() }}", fontSize = 12.sp, color = TextoPrimario)
                        m.numeroMicrochip?.let { chip ->
                            Text("Microchip: $chip", fontSize = 12.sp, color = CelesteOscuro, fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }

            // Pestañas
            TabRow(
                selectedTabIndex = pestanaSeleccionada,
                containerColor = Color.Transparent,
                contentColor = CelesteOscuro,
                divider = {}
            ) {
                Tab(
                    selected = pestanaSeleccionada == 0,
                    onClick = { pestanaSeleccionada = 0 },
                    text = { Text("Vacunas (${vacunas.size})", fontWeight = FontWeight.SemiBold) }
                )
                Tab(
                    selected = pestanaSeleccionada == 1,
                    onClick = { pestanaSeleccionada = 1 },
                    text = { Text("Desparasitaciones (${desparasitaciones.size})", fontWeight = FontWeight.SemiBold) }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (pestanaSeleccionada == 0) {
                if (vacunas.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No hay vacunas registradas.\nToca + para ingresar una dosis.", color = TextoSecundario, fontSize = 14.sp, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                    }
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), contentPadding = PaddingValues(vertical = 8.dp)) {
                        items(vacunas, key = { it.id }) { v ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = SuperficieTarjeta)
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(v.nombreVacuna, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = TextoPrimario)
                                        Text("Aplicada el: ${v.fechaAplicacion}", fontSize = 13.sp, color = TextoSecundario)
                                        v.proximaDosis?.let { prox ->
                                            Text("Próximo refuerzo: $prox", fontSize = 12.sp, color = CelesteOscuro, fontWeight = FontWeight.Medium)
                                        }
                                    }
                                    TextButton(onClick = { vacunaAEditar = v }) {
                                        Text("Editar", fontSize = 12.sp, color = CelesteOscuro)
                                    }
                                    IconButton(onClick = { viewModel.eliminarVacuna(v) }) {
                                        Text("✕", color = RosaOscuro, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                if (desparasitaciones.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No hay desparasitaciones registradas.\nToca + para agregar una.", color = TextoSecundario, fontSize = 14.sp, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                    }
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), contentPadding = PaddingValues(vertical = 8.dp)) {
                        items(desparasitaciones, key = { it.id }) { d ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = SuperficieTarjeta)
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(d.nombreAntiparasitario, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = TextoPrimario)
                                        Text("Fecha: ${d.fechaAplicacion}", fontSize = 13.sp, color = TextoSecundario)
                                        d.proximaDosis?.let { prox ->
                                            Text("Próxima dosis: $prox", fontSize = 12.sp, color = CelesteOscuro, fontWeight = FontWeight.Medium)
                                        }
                                    }
                                    TextButton(onClick = { desparasitacionAEditar = d }) {
                                        Text("Editar", fontSize = 12.sp, color = CelesteOscuro)
                                    }
                                    IconButton(onClick = { viewModel.eliminarDesparasitacion(d) }) {
                                        Text("✕", color = RosaOscuro, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Modal Crear Vacuna
    if (mostrarDialogoVacuna && mascota != null) {
        DialogoVacuna(
            titulo = "Registrar Vacuna",
            especie = mascota!!.especie,
            vacunaExistente = null,
            error = mensajeError,
            onDismiss = { mostrarDialogoVacuna = false; viewModel.limpiarError() },
            onGuardar = { nom, fec, prox ->
                viewModel.registrarVacuna(nom, fec, prox) { mostrarDialogoVacuna = false }
            }
        )
    }

    // Modal Editar Vacuna
    vacunaAEditar?.let { v ->
        DialogoVacuna(
            titulo = "Editar Vacuna",
            especie = mascota?.especie ?: Especie.CANINO,
            vacunaExistente = v,
            error = mensajeError,
            onDismiss = { vacunaAEditar = null; viewModel.limpiarError() },
            onGuardar = { nom, fec, prox ->
                viewModel.editarVacuna(v, nom, fec, prox) { vacunaAEditar = null }
            }
        )
    }

    // Modal Crear Desparasitación
    if (mostrarDialogoDesparasitacion) {
        DialogoDesparasitacion(
            titulo = "Registrar Desparasitación",
            desparasitacionExistente = null,
            error = mensajeError,
            onDismiss = { mostrarDialogoDesparasitacion = false; viewModel.limpiarError() },
            onGuardar = { prod, fec, prox ->
                viewModel.registrarDesparasitacion(prod, fec, prox) { mostrarDialogoDesparasitacion = false }
            }
        )
    }

    // Modal Editar Desparasitación
    desparasitacionAEditar?.let { d ->
        DialogoDesparasitacion(
            titulo = "Editar Desparasitación",
            desparasitacionExistente = d,
            error = mensajeError,
            onDismiss = { desparasitacionAEditar = null; viewModel.limpiarError() },
            onGuardar = { prod, fec, prox ->
                viewModel.editarDesparasitacion(d, prod, fec, prox) { desparasitacionAEditar = null }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DialogoVacuna(
    titulo: String,
    especie: Especie,
    vacunaExistente: VacunaEntity?,
    error: String? = null,
    onDismiss: () -> Unit,
    onGuardar: (nombre: String, fecha: String, proxima: String?) -> Unit
) {
    val vacunasSugeridas = remember(especie) { TipoVacuna.obtenerPorEspecie(especie) }
    var vacunaSeleccionada by remember {
        mutableStateOf(vacunasSugeridas.firstOrNull { it.nombreLegible == vacunaExistente?.nombreVacuna } ?: TipoVacuna.OTRA)
    }
    var nombrePersonalizado by remember { mutableStateOf(if (vacunaSeleccionada == TipoVacuna.OTRA) vacunaExistente?.nombreVacuna ?: "" else "") }
    var fechaAplicacion by remember { mutableStateOf(vacunaExistente?.fechaAplicacion ?: "") }
    var proximaDosis by remember { mutableStateOf(vacunaExistente?.proximaDosis ?: "") }
    var desplegadoMenu by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = RoundedCornerShape(16.dp), color = SuperficieTarjeta) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(titulo, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = TextoPrimario)
                Spacer(modifier = Modifier.height(14.dp))

                ExposedDropdownMenuBox(
                    expanded = desplegadoMenu,
                    onExpandedChange = { desplegadoMenu = !desplegadoMenu }
                ) {
                    OutlinedTextField(
                        value = vacunaSeleccionada.nombreLegible,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Vacuna") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = desplegadoMenu) },
                        modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = desplegadoMenu,
                        onDismissRequest = { desplegadoMenu = false }
                    ) {
                        vacunasSugeridas.forEach { v ->
                            DropdownMenuItem(
                                text = { Text(v.nombreLegible) },
                                onClick = {
                                    vacunaSeleccionada = v
                                    desplegadoMenu = false
                                }
                            )
                        }
                    }
                }

                if (vacunaSeleccionada == TipoVacuna.OTRA) {
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = nombrePersonalizado,
                        onValueChange = { nombrePersonalizado = it },
                        label = { Text("Nombre de la vacuna *") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = fechaAplicacion,
                    onValueChange = { fechaAplicacion = it },
                    label = { Text("Fecha de aplicación * (dd/mm/yyyy)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = proximaDosis,
                    onValueChange = { proximaDosis = it },
                    label = { Text("Próxima dosis (dd/mm/yyyy) (Opcional)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                if (error != null) {
                    Text(
                        text = error,
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(top = 10.dp)
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("Cancelar") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val nombreFinal = if (vacunaSeleccionada == TipoVacuna.OTRA) nombrePersonalizado else vacunaSeleccionada.nombreLegible
                            onGuardar(nombreFinal, fechaAplicacion, proximaDosis.takeIf { it.isNotBlank() })
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CelesteOscuro)
                    ) {
                        Text("Guardar")
                    }
                }
            }
        }
    }
}

@Composable
fun DialogoDesparasitacion(
    titulo: String,
    desparasitacionExistente: DesparasitacionEntity?,
    error: String? = null,
    onDismiss: () -> Unit,
    onGuardar: (producto: String, fecha: String, proxima: String?) -> Unit
) {
    var producto by remember { mutableStateOf(desparasitacionExistente?.nombreAntiparasitario ?: "") }
    var fechaAplicacion by remember { mutableStateOf(desparasitacionExistente?.fechaAplicacion ?: "") }
    var proximaDosis by remember { mutableStateOf(desparasitacionExistente?.proximaDosis ?: "") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = RoundedCornerShape(16.dp), color = SuperficieTarjeta) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(titulo, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = TextoPrimario)
                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = producto,
                    onValueChange = { producto = it },
                    label = { Text("Producto / Antiparasitario *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = fechaAplicacion,
                    onValueChange = { fechaAplicacion = it },
                    label = { Text("Fecha de aplicación * (dd/mm/yyyy)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = proximaDosis,
                    onValueChange = { proximaDosis = it },
                    label = { Text("Próxima dosis (dd/mm/yyyy) (Opcional)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                if (error != null) {
                    Text(
                        text = error,
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(top = 10.dp)
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("Cancelar") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            onGuardar(producto, fechaAplicacion, proximaDosis.takeIf { it.isNotBlank() })
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CelesteOscuro)
                    ) {
                        Text("Guardar")
                    }
                }
            }
        }
    }
}