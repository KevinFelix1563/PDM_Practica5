package com.example.alumnosuabc_v2

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.alumnosuabc_v2.data.dao.AlumnoConCarrera
import com.example.alumnosuabc_v2.ui.theme.AlumnosUABC_V2Theme
import com.example.alumnosuabc_v2.viewmodel.AlumnosUiState
import com.example.alumnosuabc_v2.viewmodel.AlumnosViewModel

/**
 * Pantalla principal del sistema de control escolar.
 */
class MainActivity : ComponentActivity() {

    private val viewModel: AlumnosViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AlumnosUABC_V2Theme {
                val uiState by viewModel.uiState.collectAsStateWithLifecycle()

                PantallaAlumnos(
                    uiState = uiState,
                    onSeleccionarCarrera = viewModel::seleccionarCarrera,
                    onAgregarAlumno = {
                        startActivity(Intent(this, FormularioAlumnoActivity::class.java))
                    },
                    onEditarAlumno = { alumno ->
                        val intent = Intent(this, FormularioAlumnoActivity::class.java).apply {
                            putExtra(FormularioAlumnoActivity.EXTRA_ALUMNO_ID, alumno.id)
                        }
                        startActivity(intent)
                    },
                    onConfirmarEliminarAlumno = viewModel::eliminarAlumno,
                    onLimpiarError = viewModel::limpiarError
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PantallaAlumnos(
    uiState: AlumnosUiState,
    onSeleccionarCarrera: (Long?) -> Unit,
    onAgregarAlumno: () -> Unit,
    onEditarAlumno: (AlumnoConCarrera) -> Unit,
    onConfirmarEliminarAlumno: (AlumnoConCarrera) -> Unit,
    onLimpiarError: () -> Unit
) {
    val context = LocalContext.current
    var alumnoAEliminar by remember { mutableStateOf<AlumnoConCarrera?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Control Escolar") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAgregarAlumno,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Registrar alumno"
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (uiState.mensajeError != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = uiState.mensajeError,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.weight(1f)
                    )
                    TextButton(onClick = onLimpiarError) {
                        Text("Cerrar")
                    }
                }
            }

            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item(key = "todas") {
                    FilterChip(
                        selected = uiState.carreraSeleccionadaId == null,
                        onClick = { onSeleccionarCarrera(null) },
                        label = { Text("Todas") }
                    )
                }

                items(
                    items = uiState.carreras,
                    key = { it.id }
                ) { carrera ->
                    FilterChip(
                        selected = uiState.carreraSeleccionadaId == carrera.id,
                        onClick = { onSeleccionarCarrera(carrera.id) },
                        label = { Text(carrera.nombre) }
                    )
                }
            }

            if (uiState.alumnos.isEmpty()) {
                val mensajeVacio = if (uiState.carreraSeleccionadaId == null) {
                    "No hay alumnos registrados todavía.\nPulsa + para agregar el primero."
                } else {
                    "No hay alumnos registrados en esta carrera."
                }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = mensajeVacio,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        start = 16.dp,
                        end = 16.dp,
                        top = 4.dp,
                        bottom = 88.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(
                        items = uiState.alumnos,
                        key = { it.id }
                    ) { alumno ->
                        TarjetaAlumno(
                            alumno = alumno,
                            onAbrirDetalle = {
                                Toast.makeText(
                                    context,
                                    "Detalle del alumno disponible en el siguiente incremento",
                                    Toast.LENGTH_SHORT
                                ).show()
                            },
                            onEditar = { onEditarAlumno(alumno) },
                            onEliminar = { alumnoAEliminar = alumno }
                        )
                    }
                }
            }
        }
    }

    alumnoAEliminar?.let { alumno ->
        AlertDialog(
            onDismissRequest = { alumnoAEliminar = null },
            title = { Text("Eliminar alumno") },
            text = {
                Text(
                    "¿Deseas eliminar a ${alumno.nombre} (Matrícula: ${alumno.matricula})? " +
                        "También se eliminarán todas sus calificaciones registradas."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onConfirmarEliminarAlumno(alumno)
                        alumnoAEliminar = null
                    }
                ) {
                    Text(
                        text = "Eliminar",
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { alumnoAEliminar = null }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
private fun TarjetaAlumno(
    alumno: AlumnoConCarrera,
    onAbrirDetalle: () -> Unit,
    onEditar: () -> Unit,
    onEliminar: () -> Unit
) {
    Card(
        onClick = onAbrirDetalle,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, top = 14.dp, bottom = 14.dp, end = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = alumno.nombre,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Matrícula: ${alumno.matricula}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = alumno.nombreCarrera,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Row {
                IconButton(onClick = onEditar) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Editar alumno"
                    )
                }
                IconButton(onClick = onEliminar) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Eliminar alumno",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}
