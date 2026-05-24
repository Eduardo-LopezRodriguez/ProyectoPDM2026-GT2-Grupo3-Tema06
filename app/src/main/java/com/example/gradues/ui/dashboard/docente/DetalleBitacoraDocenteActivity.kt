package com.example.gradues.ui.dashboard.docente

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.gradues.data.dao.PasantiaDocenteDao
import com.example.gradues.data.db.DatabaseHelper
import com.example.gradues.data.model.BitacoraPendienteDocenteModel
import com.example.gradues.databinding.ActivityDetalleBitacoraDocenteBinding
import com.example.gradues.utils.SessionManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class DetalleBitacoraDocenteActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetalleBitacoraDocenteBinding
    private lateinit var pasantiaDocenteDao: PasantiaDocenteDao
    private lateinit var sessionManager: SessionManager

    private var idBitacora: String? = null
    private var currentBitacora: BitacoraPendienteDocenteModel? = null

    private val bitacoraStatuses = listOf("Pendiente", "Revisada", "Con observación")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetalleBitacoraDocenteBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)
        pasantiaDocenteDao = PasantiaDocenteDao(DatabaseHelper(this))

        idBitacora = intent.getStringExtra("ID_BITACORA")

        if (idBitacora.isNullOrBlank()) {
            Toast.makeText(this, "Error: ID de bitácora no encontrado.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        configurarEventos()
        cargarDetalleBitacora(idBitacora!!)
    }

    private fun configurarEventos() {
        binding.toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        binding.btnActualizarEstadoBitacora.setOnClickListener {
            mostrarDialogoConfirmarActualizacion()
        }

        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, bitacoraStatuses)
        binding.spinnerEstadoBitacora.adapter = adapter
    }

    private fun cargarDetalleBitacora(idBit: String) {
        val bitacora = pasantiaDocenteDao.obtenerDetalleBitacora(idBit)
        if (bitacora != null) {
            currentBitacora = bitacora
            pintarDetalleBitacora(bitacora)
        } else {
            Toast.makeText(this, "No se encontró el detalle de la bitácora.", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun pintarDetalleBitacora(model: BitacoraPendienteDocenteModel) {
        binding.tvDetalleTituloActividad.text = model.tituloActividad
        binding.tvDetalleFechaActividad.text = "Fecha: ${model.fechaActividad}"
        binding.tvDetalleTotalHoras.text = "Horas: ${model.totalHorasTrabajadas}"
        binding.tvDetalleEstudianteBitacora.text = "Estudiante: ${model.nombreEstudiante} (${model.carnetEstudiante})"
        binding.tvDetalleTrabajoEmpresa.text = "Trabajo: ${model.nombreTrabajo} (${model.nombreEmpresa})"
        binding.tvDetalleDescripcionActividad.text = model.descripcionActividad
        binding.etObservacionBitacora.setText(model.observacionBitacora)

        val currentStatusIndex = bitacoraStatuses.indexOf(model.estadoBitacora)
        if (currentStatusIndex != -1) {
            binding.spinnerEstadoBitacora.setSelection(currentStatusIndex)
        }
    }

    private fun mostrarDialogoConfirmarActualizacion() {
        MaterialAlertDialogBuilder(this)
            .setTitle("Actualizar Bitácora")
            .setMessage("¿Deseas actualizar el estado y la observación de esta bitácora?")
            .setPositiveButton("Sí, actualizar") { _, _ ->
                actualizarBitacora()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun actualizarBitacora() {
        val bitId = idBitacora
        val nuevoEstado = binding.spinnerEstadoBitacora.selectedItem.toString()
        val nuevaObservacion = binding.etObservacionBitacora.text.toString().ifBlank { null }

        if (bitId.isNullOrBlank()) {
            Toast.makeText(this, "Error: ID de bitácora no válido para actualizar.", Toast.LENGTH_SHORT).show()
            return
        }

        val success = pasantiaDocenteDao.actualizarEstadoYObservacionBitacora(
            idBitacora = bitId,
            nuevoEstado = nuevoEstado,
            nuevaObservacion = nuevaObservacion
        )

        if (success) {
            Toast.makeText(this, "Bitácora actualizada exitosamente.", Toast.LENGTH_SHORT).show()
            cargarDetalleBitacora(bitId) // Reload to update UI
        } else {
            Toast.makeText(this, "Error al actualizar la bitácora.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun Int.dpToPx(): Int = (this * resources.displayMetrics.density).toInt()
}