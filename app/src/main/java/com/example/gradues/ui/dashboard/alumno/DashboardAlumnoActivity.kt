package com.example.gradues.ui.dashboard.alumno

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.gradues.data.dao.DashboardAlumnoDao
import com.example.gradues.data.db.DatabaseHelper
import com.example.gradues.databinding.ActivityDashboardAlumnoBinding
import com.example.gradues.ui.login.LoginActivity
import com.example.gradues.utils.SessionManager
import java.util.Locale

class DashboardAlumnoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDashboardAlumnoBinding
    private lateinit var dashboardAlumnoDao: DashboardAlumnoDao
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardAlumnoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        dashboardAlumnoDao = DashboardAlumnoDao(DatabaseHelper(this))
        sessionManager = SessionManager(this)

        validarSesion()
        configurarEventos()
        cargarResumenAlumno()
    }

    private fun validarSesion() {
        if (!sessionManager.estaLogueado()) {
            irAlLogin()
            return
        }

        val rol = sessionManager.getRol()?.trim()?.lowercase(Locale.getDefault()).orEmpty()
        if (rol != "alumno") {
            Toast.makeText(this, "Acceso no autorizado para esta pantalla.", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun configurarEventos() {
        binding.btnCerrarSesionAlumno.setOnClickListener {
            sessionManager.cerrarSesion()
            irAlLogin()
        }
    }

    private fun cargarResumenAlumno() {
        val idSesion = sessionManager.getIdUsuario()?.trim().orEmpty()

        if (idSesion.isEmpty()) {
            Toast.makeText(this, "No se encontró la sesión del alumno.", Toast.LENGTH_SHORT).show()
            irAlLogin()
            return
        }

        val resumen = dashboardAlumnoDao.obtenerResumenAlumno(idSesion)

        if (resumen == null) {
            Toast.makeText(this, "No se encontró información del alumno.", Toast.LENGTH_SHORT).show()
            return
        }

        binding.tvNombreAlumno.text = resumen.nombreCompleto
        binding.tvCarnetAlumno.text = "Carnet: ${resumen.carnet}"
        binding.tvModalidadAlumno.text = "Modalidad: ${resumen.modalidadActual}"
        binding.tvEstadoSolicitudAlumno.text = "Estado de solicitud: ${resumen.estadoSolicitud}"
        binding.tvTrabajoAlumno.text = "Trabajo: ${resumen.nombreTrabajo}"
        binding.tvCodigoAgrupacionAlumno.text = "Código de agrupación: ${resumen.codigoAgrupacion}"

        binding.tvTotalDocumentosAlumno.text = resumen.totalDocumentos.toString()
        binding.tvTotalPropuestasAlumno.text = resumen.totalPropuestas.toString()
        binding.tvTotalNotasAlumno.text = resumen.totalNotasRegistradas.toString()
        binding.tvPromedioNotasAlumno.text = String.format(
            Locale.getDefault(),
            "%.2f",
            resumen.promedioNotas
        )
    }

    private fun irAlLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }
}