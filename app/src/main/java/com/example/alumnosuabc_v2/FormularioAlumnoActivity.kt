package com.example.alumnosuabc_v2

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.alumnosuabc_v2.ui.theme.AlumnosUABC_V2Theme
import com.example.alumnosuabc_v2.viewmodel.FormularioAlumnoUiState
import com.example.alumnosuabc_v2.viewmodel.FormularioAlumnoViewModel

/**
 * Pantalla de formulario para registrar un alumno nuevo o editar uno existente.
 */
class FormularioAlumnoActivity : ComponentActivity() {

    private val viewModel: FormularioAlumnoViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val alumnoId = intent.getLongExtra(EXTRA_ALUMNO_ID, -1L)
        viewModel.inicializar(alumnoId)

        setContent {
            AlumnosUABC_V2Theme {
                val uiState by viewModel.uiState.collectAsStateWithLifecycle()

                LaunchedEffect(uiState.guardadoExitoso) {
                    if (uiState.guardadoExitoso) {
                        finish()
                    }
                }

                PantallaFormularioAlumno(
                    uiState = uiState,
                    onNombreChange = viewModel::onNombreChange,
                    onMatriculaChange = viewModel::onMatriculaChange,
                    onCorreoChange = viewModel::onCorreoChange,
                    onSemestreChange = viewModel::onSemestreChange,
                    onCarreraChange = viewModel::onCarreraChange,
                    onGuardar = viewModel::guardar,
                    onFinalizar = { finish() }
                )
            }
        }
    }

    companion object {
        const val EXTRA_ALUMNO_ID = "alumnoId"
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PantallaFormularioAlumno(
    uiState: FormularioAlumnoUiState,
    onNombreChange: (String) -> Unit,
    onMatriculaChange: (String) -> Unit,
    onCorreoChange: (String) -> Unit,
    onSemestreChange: (String) -> Unit,
    onCarreraChange: (Long) -> Unit,
    onGuardar: () -> Unit,
    onFinalizar: () -> Unit
) {
    var menuExpandido by remember { mutableStateOf(false) }
    val nombreCarreraSeleccionada = uiState.carrerasDisponibles
        .firstOrNull { it.id == uiState.carreraIdSeleccionada }
        ?.nombre
        .orEmpty()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(if (uiState.esEdicion) "Editar Alumno" else "Registrar Alumno")
                },
                navigationIcon = {
                    IconButton(onClick = onFinalizar) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Regresar"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = uiState.nombre,
                onValueChange = onNombreChange,
                label = { Text("Nombre completo") },
                singleLine = true,
                enabled = !uiState.guardando,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = uiState.matricula,
                onValueChange = onMatriculaChange,
                label = { Text("Matrícula") },
                singleLine = true,
                enabled = !uiState.guardando,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = uiState.correo,
                onValueChange = onCorreoChange,
                label = { Text("Correo electrónico") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                enabled = !uiState.guardando,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = uiState.semestre,
                onValueChange = onSemestreChange,
                label = { Text("Semestre") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                enabled = !uiState.guardando,
                modifier = Modifier.fillMaxWidth()
            )

            ExposedDropdownMenuBox(
                expanded = menuExpandido,
                onExpandedChange = {
                    if (uiState.puedeCambiarCarrera && !uiState.guardando) {
                        menuExpandido = it
                    }
                }
            ) {
                OutlinedTextField(
                    value = nombreCarreraSeleccionada,
                    onValueChange = {},
                    readOnly = true,
                    enabled = uiState.puedeCambiarCarrera && !uiState.guardando,
                    label = { Text("Carrera") },
                    placeholder = { Text("Selecciona una carrera") },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = menuExpandido)
                    },
                    colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(
                            type = ExposedDropdownMenuAnchorType.PrimaryNotEditable,
                            enabled = uiState.puedeCambiarCarrera && !uiState.guardando
                        )
                )

                ExposedDropdownMenu(
                    expanded = menuExpandido,
                    onDismissRequest = { menuExpandido = false }
                ) {
                    uiState.carrerasDisponibles.forEach { carrera ->
                        DropdownMenuItem(
                            text = { Text(carrera.nombre) },
                            onClick = {
                                onCarreraChange(carrera.id)
                                menuExpandido = false
                            }
                        )
                    }
                }
            }

            if (!uiState.puedeCambiarCarrera && uiState.avisoCambioCarrera != null) {
                Text(
                    text = uiState.avisoCambioCarrera,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (uiState.mensajeError != null) {
                Text(
                    text = uiState.mensajeError,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onFinalizar,
                    enabled = !uiState.guardando,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Cancelar")
                }

                Button(
                    onClick = onGuardar,
                    enabled = !uiState.guardando,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Guardar")
                }
            }
        }
    }
}
