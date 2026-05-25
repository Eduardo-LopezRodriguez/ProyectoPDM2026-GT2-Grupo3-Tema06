package com.example.gradues.ui.dashboard.admin

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.gradues.data.dao.DashboardAdminDao
import com.example.gradues.data.db.DatabaseHelper
import com.example.gradues.data.model.DashboardAdminModel
import com.example.gradues.databinding.ActivityDashboardAdminBinding
import com.example.gradues.ui.login.LoginActivity
import com.example.gradues.utils.SessionManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class DashboardAdminActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDashboardAdminBinding
    private lateinit var sessionManager: SessionManager
    private lateinit var dashboardAdminDao: DashboardAdminDao

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityDashboardAdminBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)
        dashboardAdminDao = DashboardAdminDao(DatabaseHelper(this))

        configurarEventos()
        cargarDashboard()
    }

    private fun configurarEventos() {
        binding.btnGestionUsuarios.setOnClickListener {
            val intent = Intent(this, GestionUsuariosAdminActivity::class.java)
            startActivity(intent)
        }

        binding.btnSolicitudesModalidad.setOnClickListener {
            val intent = Intent(this, SolicitudesModalidadAdminActivity::class.java)
            startActivity(intent)
        }

        binding.btnVerTrabajos.setOnClickListener {
            val intent = Intent(this, TrabajosGraduacionAdminActivity::class.java)
            startActivity(intent)
        }

        binding.btnGestionarCursos.setOnClickListener {
            val intent = Intent(this, GestionCursosAdminActivity::class.java)
            startActivity(intent)
        }

        binding.btnActualizar.setOnClickListener {
            cargarDashboard()
        }

        binding.btnCerrarSesion.setOnClickListener {
            mostrarDialogoCerrarSesion()
        }
    }

    override fun onResume() {
        super.onResume()

        if (::dashboardAdminDao.isInitialized) {
            cargarDashboard()
        }
    }

    private fun cargarDashboard() {
        val idUsuario = sessionManager.getIdUsuario()

        if (idUsuario.isBlank()) {
            mostrarSesionInvalida()
            return
        }

        val dashboard = dashboardAdminDao.obtenerDashboardAdmin(idUsuario)

        if (dashboard == null) {
            Toast.makeText(
                this,
                "No se encontró información del administrador.",
                Toast.LENGTH_LONG
            ).show()
            return
        }

        pintarDashboard(dashboard)
    }

    private fun pintarDashboard(model: DashboardAdminModel) {
        binding.tvNombreAdmin.text = model.nombreCompleto
        binding.tvCorreoAdmin.text = model.correo.ifBlank { "Correo no registrado" }

        binding.tvTotalUsuarios.text = model.totalUsuarios.toString()
        binding.tvTotalAlumnos.text = model.totalAlumnos.toString()
        binding.tvTotalDocentes.text = model.totalDocentes.toString()

        binding.tvTrabajosActivos.text =
            "Trabajos activos: ${model.totalTrabajosActivos}"

        binding.tvSolicitudesPendientes.text =
            "Solicitudes pendientes: ${model.solicitudesPendientes}"

        binding.tvSolicitudesAprobadas.text =
            "Solicitudes aprobadas: ${model.solicitudesAprobadas}"
    }

    private fun mostrarDialogoCerrarSesion() {
        MaterialAlertDialogBuilder(this)
            .setTitle("Cerrar sesión")
            .setMessage("¿Deseas cerrar la sesión actual?")
            .setPositiveButton("Sí, salir") { _, _ ->
                cerrarSesion()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun mostrarSesionInvalida() {
        MaterialAlertDialogBuilder(this)
            .setTitle("Sesión no válida")
            .setMessage("No se encontró una sesión activa. Inicia sesión nuevamente.")
            .setPositiveButton("Ir al login") { _, _ ->
                cerrarSesion()
            }
            .setCancelable(false)
            .show()
    }

    private fun cerrarSesion() {
        sessionManager.cerrarSesion()

        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK

        startActivity(intent)
        finish()
    }
}