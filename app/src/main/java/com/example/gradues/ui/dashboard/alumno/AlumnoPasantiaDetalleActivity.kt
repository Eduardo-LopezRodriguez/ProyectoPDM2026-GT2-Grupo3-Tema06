package com.example.gradues.ui.dashboard.alumno

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.gradues.data.dao.AlumnoPasantiaDao
import com.example.gradues.data.db.DatabaseHelper
import com.example.gradues.databinding.ActivityAlumnoPasantiaDetalleBinding
import com.example.gradues.utils.SessionManager

class AlumnoPasantiaDetalleActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAlumnoPasantiaDetalleBinding
    private lateinit var alumnoPasantiaDao: AlumnoPasantiaDao
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAlumnoPasantiaDetalleBinding.inflate(layoutInflater)
        setContentView(binding.root)

        alumnoPasantiaDao = AlumnoPasantiaDao(DatabaseHelper(this))
        sessionManager = SessionManager(this)

        cargarDetallePasantia()
        configurarEventos()
    }

    private fun cargarDetallePasantia() {
        val idUsuario = sessionManager.getIdUsuario().trim()

        if (idUsuario.isBlank()) {
            Toast.makeText(this, "No se encontro la sesion del alumno.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        val detalle = alumnoPasantiaDao.obtenerDetallePasantiaPorAlumno(idUsuario)
        if (detalle == null) {
            Toast.makeText(this, "No se encontro informacion de pasantia.", Toast.LENGTH_SHORT).show()
            return
        }

        binding.tvNombreAlumnoPasantia.text =
            detalle.nombreAlumno.ifBlank { "Alumno" }

        binding.tvCarnetAlumnoPasantia.text =
            detalle.carnetAlumno.ifBlank { "Sin carnet" }

        binding.tvDocentePasantia.text =
            if (detalle.nombreDocente.isNotBlank()) {
                "Docente a cargo: ${detalle.nombreDocente}"
            } else {
                "Docente a cargo: No asignado"
            }

        binding.tvModalidadPasantia.text = "Pasantia profesional"

        binding.tvEmpresaPasantia.text =
            detalle.nombreEmpresa.ifBlank { "Empresa no disponible" }

        binding.tvDireccionPasantia.text =
            if (detalle.rubroEmpresa.isNotBlank()) {
                "Rubro: ${detalle.rubroEmpresa}"
            } else {
                "Rubro no disponible"
            }

        binding.tvSupervisorPasantia.text =
            if (detalle.nombrePersonero.isNotBlank()) {
                if (detalle.cargoPersonero.isNotBlank()) {
                    "Reporta a: ${detalle.nombrePersonero} (${detalle.cargoPersonero})"
                } else {
                    "Reporta a: ${detalle.nombrePersonero}"
                }
            } else {
                "Reporta a: No asignado"
            }
    }

    private fun configurarEventos() {
        binding.btnBackPasantiaAlumno.setOnClickListener {
            finish()
        }

        binding.btnNotificacionesPasantiaAlumno.setOnClickListener {
            mostrarMensaje("Notificaciones pendientes")
        }

        binding.btnPerfilPasantiaAlumno.setOnClickListener {
            mostrarMensaje("Perfil pendiente")
        }

        binding.btnMisBitacorasPasantia.setOnClickListener {
            startActivity(Intent(this, AlumnoBitacorasActivity::class.java))
        }

        binding.btnSubirMemoriaPasantia.setOnClickListener {
            startActivity(Intent(this, AlumnoMemoriaActivity::class.java))
        }

        binding.itemInicioPasantiaAlumno.setOnClickListener {
            startActivity(Intent(this, DashboardAlumnoActivity::class.java))
            finish()
        }

        binding.itemGrupoPasantiaAlumno.setOnClickListener {
            mostrarMensaje("Ya estas en Mi pasantia")
        }

        binding.itemPerfilPasantiaAlumno.setOnClickListener {
            mostrarMensaje("Perfil pendiente")
        }
    }

    private fun mostrarMensaje(mensaje: String) {
        Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show()
    }
}
