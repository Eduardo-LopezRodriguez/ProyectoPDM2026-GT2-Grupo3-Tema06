package com.example.gradues.ui.dashboard.alumno

import android.graphics.Color
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.gradues.data.dao.SolicitudAlumnoDetalleDao
import com.example.gradues.data.db.DatabaseHelper
import com.example.gradues.databinding.ActivityDetalleSolicitudAlumnoBinding
import com.example.gradues.utils.SessionManager

class DetalleSolicitudAlumnoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetalleSolicitudAlumnoBinding
    private lateinit var sessionManager: SessionManager
    private lateinit var detalleDao: SolicitudAlumnoDetalleDao

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetalleSolicitudAlumnoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)
        detalleDao = SolicitudAlumnoDetalleDao(DatabaseHelper(this))

        configurarEventos()
        cargarDetalleSolicitud()
    }

    private fun configurarEventos() {
        binding.btnVolverDetalleSolicitud.setOnClickListener {
            finish()
        }
    }

    private fun cargarDetalleSolicitud() {
        val idSesion = sessionManager.getIdUsuario()?.trim().orEmpty()

        if (idSesion.isBlank()) {
            Toast.makeText(this, "No se encontró la sesión del alumno.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        val detalle = detalleDao.obtenerUltimaSolicitudAlumno(idSesion)

        if (detalle == null) {
            Toast.makeText(this, "No se encontró una solicitud registrada.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        binding.tvEstadoPrincipalDetalle.text = "Estado: ${detalle.estadoSolicitud}"
        binding.tvTrabajoPrincipalDetalle.text = detalle.nombreTrabajoPropuesto.ifBlank {
            "Sin trabajo propuesto"
        }
        binding.tvModalidadPrincipalDetalle.text = "Modalidad: ${detalle.modalidad.ifBlank { "No definida" }}"

        binding.tvFechaDetalle.text = detalle.fechaSolicitud.ifBlank { "No registrada" }
        binding.tvCodigoAgrupacionDetalle.text = detalle.codigoAgrupacionSolicitud.ifBlank { "No aplica" }
        binding.tvObservacionDetalle.text = detalle.observacionSolicitud.ifBlank { "Sin observaciones registradas." }

        binding.tvNombreTrabajoAsociadoDetalle.text = detalle.nombreTrabajoAsociado.ifBlank { "No asignado" }
        binding.tvCursoDetalle.text = detalle.nombreCurso.ifBlank { "No aplica" }
        binding.tvEmpresaDetalle.text = detalle.nombreEmpresa.ifBlank { "No aplica" }
        binding.tvCodigoGrupoTgiDetalle.text = detalle.codigoGrupoTGI.ifBlank { "No aplica" }

        aplicarColorEstado(detalle.estadoSolicitud)
    }

    private fun aplicarColorEstado(estado: String) {
        when {
            estado.equals("Pendiente", ignoreCase = true) -> {
                binding.tvEstadoPrincipalDetalle.setTextColor(Color.parseColor("#FFF3CD"))
                binding.tvEstadoPrincipalDetalle.setBackgroundColor(Color.parseColor("#C40000"))
            }

            estado.equals("Aprobada", ignoreCase = true) -> {
                binding.tvEstadoPrincipalDetalle.setTextColor(Color.parseColor("#D4EDDA"))
                binding.tvEstadoPrincipalDetalle.setBackgroundColor(Color.parseColor("#C40000"))
            }

            estado.equals("Rechazada", ignoreCase = true) -> {
                binding.tvEstadoPrincipalDetalle.setTextColor(Color.parseColor("#F8D7DA"))
                binding.tvEstadoPrincipalDetalle.setBackgroundColor(Color.parseColor("#C40000"))
            }

            else -> {
                binding.tvEstadoPrincipalDetalle.setTextColor(Color.WHITE)
                binding.tvEstadoPrincipalDetalle.setBackgroundColor(Color.parseColor("#C40000"))
            }
        }
    }
}