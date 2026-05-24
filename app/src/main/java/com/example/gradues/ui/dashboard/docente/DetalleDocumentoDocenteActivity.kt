// DetalleDocumentoDocenteActivity.kt
package com.example.gradues.ui.dashboard.docente

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.gradues.R
import com.example.gradues.data.dao.DocumentoRevisionDocenteDao
import com.example.gradues.data.db.DatabaseHelper
import com.example.gradues.data.model.DocumentoRevisionDocenteModel
import com.example.gradues.databinding.ActivityDetalleDocumentoDocenteBinding
import com.example.gradues.utils.SessionManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class DetalleDocumentoDocenteActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetalleDocumentoDocenteBinding
    private lateinit var documentoRevisionDocenteDao: DocumentoRevisionDocenteDao
    private lateinit var sessionManager: SessionManager

    private var idDocumento: String? = null
    private var currentDocumento: DocumentoRevisionDocenteModel? = null

    // Define possible document statuses
    private val documentStatuses = listOf("Pendiente", "Aprobado", "Rechazado", "Observado")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetalleDocumentoDocenteBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)
        documentoRevisionDocenteDao = DocumentoRevisionDocenteDao(DatabaseHelper(this))

        idDocumento = intent.getStringExtra("ID_DOCUMENTO")

        if (idDocumento.isNullOrBlank()) {
            Toast.makeText(this, "Error: ID de documento no encontrado.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        configurarEventos()
        cargarDetalleDocumento(idDocumento!!)
    }

    private fun configurarEventos() {
        binding.btnBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        binding.tvDetalleUrlDocumento.setOnClickListener { 
            currentDocumento?.urlDocumento?.let { url ->
                if (url.isNotBlank()) {
                    try {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                        startActivity(intent)
                    } catch (e: Exception) {
                        Toast.makeText(this, "No se pudo abrir el documento. Verifique la URL.", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this, "No hay archivo subido para este documento.", Toast.LENGTH_SHORT).show()
                }
            }
        }

        binding.btnActualizarEstadoDocumento.setOnClickListener {
            mostrarDialogoConfirmarActualizacion()
        }

        // Populate spinner with statuses
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, documentStatuses)
        binding.spinnerEstadoDocumento.adapter = adapter
    }

    private fun cargarDetalleDocumento(idDoc: String) {
        val documento = documentoRevisionDocenteDao.obtenerDetalleDocumento(idDoc)
        if (documento != null) {
            currentDocumento = documento
            pintarDetalleDocumento(documento)
        } else {
            Toast.makeText(this, "No se encontró el detalle del documento.", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun pintarDetalleDocumento(model: DocumentoRevisionDocenteModel) {
        binding.tvDetalleNombreDocumento.text = model.nombreDocumento
        binding.tvDetalleTrabajoModalidad.text = "Trabajo: ${model.nombreTrabajo} (${model.nombreModalidad})"
        binding.tvDetalleEstudiante.text = "Estudiante: ${model.nombreEstudiante} (${model.carnetEstudiante})"
        binding.tvDetalleTipoVersionDocumento.text = "Tipo: ${model.tipoDocumento} | Versión: ${model.versionDocumento}"
        binding.tvDetalleFechaCarga.text = "Fecha de Carga: ${model.fechaCarga}"
        binding.tvDetalleUrlDocumento.text = "Documento: ${model.urlDocumento ?: "No file uploaded."}"

        // Set spinner selection
        val currentStatusIndex = documentStatuses.indexOf(model.estadoDocumento)
        if (currentStatusIndex != -1) {
            binding.spinnerEstadoDocumento.setSelection(currentStatusIndex)
        }

        binding.etObservacionDocumento.setText(model.observacionDocumento)
    }

    private fun mostrarDialogoConfirmarActualizacion() {
        MaterialAlertDialogBuilder(this)
            .setTitle("Actualizar Documento")
            .setMessage("¿Deseas actualizar el estado y la observación de este documento?")
            .setPositiveButton("Sí, actualizar") { _, _ ->
                actualizarDocumento()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun actualizarDocumento() {
        val docId = idDocumento
        val nuevoEstado = binding.spinnerEstadoDocumento.selectedItem.toString()
        val nuevaObservacion = binding.etObservacionDocumento.text.toString().ifBlank { null }

        if (docId.isNullOrBlank()) {
            Toast.makeText(this, "Error: ID de documento no válido para actualizar.", Toast.LENGTH_SHORT).show()
            return
        }

        val success = documentoRevisionDocenteDao.actualizarEstadoYObservacionDocumento(
            idDocumento = docId,
            nuevoEstado = nuevoEstado,
            nuevaObservacion = nuevaObservacion
        )

        if (success) {
            Toast.makeText(this, "Documento actualizado exitosamente.", Toast.LENGTH_SHORT).show()
            cargarDetalleDocumento(docId) // Reload to update UI
        } else {
            Toast.makeText(this, "Error al actualizar el documento.", Toast.LENGTH_SHORT).show()
        }
    }

    // Extension function to convert dp to pixels (already in other activities, but for completeness)
    private fun Int.dpToPx(): Int = (this * resources.displayMetrics.density).toInt()
}