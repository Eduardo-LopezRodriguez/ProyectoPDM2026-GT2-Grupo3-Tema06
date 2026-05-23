package com.example.gradues.ui.dashboard.docente

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.gradues.data.dao.DashboardDocenteDao
import com.example.gradues.data.db.DatabaseHelper
import com.example.gradues.data.model.DashboardDocenteModel
import com.example.gradues.databinding.ActivityDashboardDocenteBinding
import com.example.gradues.ui.login.LoginActivity
import com.example.gradues.utils.SessionManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class DashboardDocenteActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDashboardDocenteBinding
    private lateinit var sessionManager: SessionManager
    private lateinit var dashboardDocenteDao: DashboardDocenteDao

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityDashboardDocenteBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)
        dashboardDocenteDao = DashboardDocenteDao(DatabaseHelper(this))

        configurarEventos()
        cargarDashboard()
    }

    private fun configurarEventos() {
        binding.btnActualizar.setOnClickListener {
            cargarDashboard()
        }

        binding.btnCerrarSesion.setOnClickListener {
            mostrarDialogoCerrarSesion()
        }
    }

    private fun cargarDashboard() {
        val idUsuario = sessionManager.getIdUsuario()

        if (idUsuario.isBlank()) {
            mostrarSesionInvalida()
            return
        }

        val dashboard = dashboardDocenteDao.obtenerDashboardDocente(idUsuario)

        if (dashboard == null) {
            Toast.makeText(
                this,
                "No se encontró información del docente.",
                Toast.LENGTH_LONG
            ).show()
            return
        }

        pintarDashboard(dashboard)
    }

    private fun pintarDashboard(model: DashboardDocenteModel) {
        binding.tvNombreDocente.text = model.nombreCompleto
        binding.tvCorreoDocente.text = model.correo.ifBlank { "Correo no registrado" }

        binding.tvTotalInvestigacion.text = model.totalInvestigacion.toString()
        binding.tvTotalEspecializacion.text = model.totalEspecializacion.toString()
        binding.tvTotalPasantia.text = model.totalPasantia.toString()

        binding.tvSolicitudesPendientes.text =
            "Solicitudes pendientes: ${model.solicitudesPendientes}"

        binding.tvDocumentosRevision.text =
            "Documentos en revisión: ${model.documentosRevision}"

        binding.tvBitacorasPendientes.text =
            "Bitácoras pendientes: ${model.bitacorasPendientes}"
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