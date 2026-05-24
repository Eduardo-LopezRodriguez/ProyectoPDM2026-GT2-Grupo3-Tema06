// DetallePasantiaDocenteActivity.kt
package com.example.gradues.ui.dashboard.docente

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.gradues.data.dao.PasantiaDocenteDao
import com.example.gradues.data.db.DatabaseHelper
import com.example.gradues.data.model.PasantiaDocenteModel
import com.example.gradues.databinding.ActivityDetallePasantiaDocenteBinding
import com.example.gradues.utils.SessionManager

class DetallePasantiaDocenteActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetallePasantiaDocenteBinding
    private lateinit var pasantiaDocenteDao: PasantiaDocenteDao
    private lateinit var sessionManager: SessionManager

    private var idProyectoPasantia: String? = null
    private var idTrabajoGraduacion: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetallePasantiaDocenteBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)
        pasantiaDocenteDao = PasantiaDocenteDao(DatabaseHelper(this))

        idProyectoPasantia = intent.getStringExtra("ID_PROYECTO_PASANTIA")
        idTrabajoGraduacion = intent.getStringExtra("ID_TRABAJO_GRADUACION")

        if (idProyectoPasantia.isNullOrBlank() || idTrabajoGraduacion.isNullOrBlank()) {
            Toast.makeText(this, "Error: ID de pasantía no encontrado.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        configurarEventos()
        cargarDetallePasantia(idProyectoPasantia!!)
    }

    private fun configurarEventos() {
        binding.btnBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        binding.btnVerBitacoras.setOnClickListener {
            val intent = Intent(this, BitacorasPendientesDocenteActivity::class.java).apply { // Corrected Activity Name
                putExtra("ID_TRABAJO_GRADUACION", idTrabajoGraduacion)
            }
            startActivity(intent)
        }

        binding.btnVerMemoria.setOnClickListener {
            val intent = Intent(this, MemoriaPasantiaDocenteActivity::class.java).apply {
                putExtra("ID_TRABAJO_GRADUACION", idTrabajoGraduacion)
            }
            startActivity(intent)
        }
    }

    private fun cargarDetallePasantia(idProyecto: String) {
        val pasantia = pasantiaDocenteDao.obtenerDetallePasantia(idProyecto)
        if (pasantia != null) {
            pintarDetallePasantia(pasantia)
        } else {
            Toast.makeText(this, "No se encontró el detalle de la pasantía.", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun pintarDetallePasantia(model: PasantiaDocenteModel) {
        binding.tvDetalleTituloPasantia.text = model.nombreTrabajo
        binding.tvDetalleEstudiante.text = "Estudiante: ${model.nombreEstudiante} (${model.carnetEstudiante})"
        binding.tvDetalleEmpresa.text = "Empresa: ${model.nombreEmpresa}"
        binding.tvDetallePersonero.text = "Contacto Empresa: ${model.nombrePersonero}"
        binding.tvDetalleEstadoPasantia.text = "Estado: ${model.estadoPasantia}"
        binding.tvDetalleTotalBitacoras.text = "Bitácoras registradas: ${model.totalBitacoras}"
        binding.tvDetalleEstadoMemoria.text = "Estado de la memoria: ${model.estadoMemoria}"
    }
}