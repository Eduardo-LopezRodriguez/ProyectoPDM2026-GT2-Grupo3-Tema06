// MemoriaPasantiaDocenteActivity.kt
package com.example.gradues.ui.dashboard.docente

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.gradues.data.dao.PasantiaDocenteDao
import com.example.gradues.data.db.DatabaseHelper
import com.example.gradues.data.model.MemoriaDocenteModel
import com.example.gradues.databinding.ActivityMemoriaPasantiaDocenteBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class MemoriaPasantiaDocenteActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMemoriaPasantiaDocenteBinding
    private lateinit var pasantiaDocenteDao: PasantiaDocenteDao

    private var idTrabajoGraduacion: String? = null
    private var idMemoriaResumen: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMemoriaPasantiaDocenteBinding.inflate(layoutInflater)
        setContentView(binding.root)

        pasantiaDocenteDao = PasantiaDocenteDao(DatabaseHelper(this))

        idTrabajoGraduacion = intent.getStringExtra("ID_TRABAJO_GRADUACION")

        if (idTrabajoGraduacion.isNullOrBlank()) {
            Toast.makeText(this, "Error: ID de trabajo de graduación no encontrado.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        configurarEventos()
        cargarMemoria(idTrabajoGraduacion!!)
    }

    private fun configurarEventos() {
        binding.btnBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        binding.btnAprobarMemoria.setOnClickListener {
            mostrarDialogoAprobarMemoria()
        }

        // TODO: Implement logic for Rechazar Memoria if needed in a future phase
        binding.btnRechazarMemoria.setOnClickListener {
            Toast.makeText(this, "Rechazar memoria no implementado en esta fase.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun cargarMemoria(idTrabajo: String) {
        val memoria = pasantiaDocenteDao.obtenerMemoriaPasantia(idTrabajo)
        if (memoria != null) {
            idMemoriaResumen = memoria.idMemoriaResumen
            pintarMemoria(memoria)
        } else {
            Toast.makeText(this, "No se encontró memoria final para esta pasantía.", Toast.LENGTH_SHORT).show()
            binding.tvMemoriaTitulo.text = "No se ha subido memoria final."
            binding.tvMemoriaEstado.text = "Estado: No Enviada"
            binding.tvMemoriaUrl.text = "Documento: No file uploaded."
            binding.tvMemoriaObservacion.text = "Observación: N/A"
            binding.btnAprobarMemoria.isEnabled = false
            binding.btnRechazarMemoria.isEnabled = false
        }
    }

    private fun pintarMemoria(model: MemoriaDocenteModel) {
        binding.tvMemoriaTitulo.text = model.tituloMemoria
        binding.tvMemoriaEstado.text = "Estado: ${model.estadoMemoria}"
        binding.tvMemoriaUrl.text = "Documento: ${model.urlDocumento ?: "No file uploaded."}"
        binding.tvMemoriaObservacion.text = "Observación: ${model.observacionMemoria ?: "Sin observaciones."}"

        binding.btnAprobarMemoria.isEnabled = model.estadoMemoria == "Pendiente" // Only enable if pending
        binding.btnRechazarMemoria.isEnabled = model.estadoMemoria == "Pendiente" // Only enable if pending
    }

    private fun mostrarDialogoAprobarMemoria() {
        MaterialAlertDialogBuilder(this)
            .setTitle("Aprobar Memoria")
            .setMessage("¿Estás seguro de que deseas aprobar esta memoria final?")
            .setPositiveButton("Sí, aprobar") { _, _ ->
                aprobarMemoria()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun aprobarMemoria() {
        val memoriaId = idMemoriaResumen
        if (memoriaId.isNullOrBlank()) {
            Toast.makeText(this, "Error: ID de memoria no encontrado para aprobar.", Toast.LENGTH_SHORT).show()
            return
        }

        val success = pasantiaDocenteDao.aprobarMemoria(memoriaId)
        if (success) {
            Toast.makeText(this, "Memoria aprobada exitosamente.", Toast.LENGTH_SHORT).show()
            cargarMemoria(idTrabajoGraduacion!!) // Reload to update UI
        } else {
            Toast.makeText(this, "Error al aprobar la memoria.", Toast.LENGTH_SHORT).show()
        }
    }
}