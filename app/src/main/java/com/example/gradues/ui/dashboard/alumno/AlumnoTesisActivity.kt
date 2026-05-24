package com.example.gradues.ui.dashboard.alumno

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.gradues.data.dao.AlumnoGrupoDetalleDao
import com.example.gradues.data.db.DatabaseHelper
import com.example.gradues.databinding.ActivityAlumnoTesisBinding
import com.example.gradues.utils.SessionManager

class AlumnoTesisActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAlumnoTesisBinding
    private lateinit var alumnoGrupoDetalleDao: AlumnoGrupoDetalleDao
    private lateinit var sessionManager: SessionManager
    private var tituloTrabajoActual: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAlumnoTesisBinding.inflate(layoutInflater)
        setContentView(binding.root)

        alumnoGrupoDetalleDao = AlumnoGrupoDetalleDao(DatabaseHelper(this))
        sessionManager = SessionManager(this)

        cargarDatosTesis()
        configurarEventos()
    }

    override fun onResume() {
        super.onResume()
        cargarDatosTesis()
    }

    private fun cargarDatosTesis() {
        val idSesion = sessionManager.getIdUsuario().trim()
        if (idSesion.isBlank()) {
            mostrarMensaje("No se encontro la sesion del alumno.")
            finish()
            return
        }

        val grupo = alumnoGrupoDetalleDao.obtenerGrupoInvestigacionAlumno(idSesion)
        if (grupo == null) {
            mostrarMensaje("No se encontro informacion del grupo.")
            finish()
            return
        }

        tituloTrabajoActual = grupo.temaTrabajo
        binding.tvNombreGrupoTesisAlumno.text = "${grupo.nombreGrupo} ${grupo.codigoGrupo}"
        binding.tvTemaGrupoTesisAlumno.text = grupo.temaTrabajo
        binding.tvDescripcionTesisAlumno.text = grupo.descripcionTesis
        binding.tvEstadoTesisAlumno.text = "Estado: ${grupo.estadoTesis}"
        binding.tvUltimaVersionTesisAlumno.text = "Ultima version: ${grupo.ultimaVersionTesis}"

        val documento = alumnoGrupoDetalleDao.obtenerUltimoDocumentoTesis(idSesion)
        binding.tvDetalleObservacionesTesisAlumno.text =
            documento?.observacionDocumento?.ifBlank { "Sin observaciones del docente." }
                ?: "Todavia no has subido ninguna version de la tesis."
        binding.tvDetalleRecordatorioTesisAlumno.text =
            if (documento == null) {
                "Sube la primera version de tu documento de investigacion."
            } else {
                "Ultimo envio: version ${documento.versionDocumento} - ${documento.fechaSubida.ifBlank { "sin fecha" }}"
            }
    }

    private fun configurarEventos() {
        binding.btnBackTesisAlumno.setOnClickListener { finish() }

        binding.btnNotificacionesTesisAlumno.setOnClickListener {
            mostrarMensaje("Notificaciones pendientes")
        }

        binding.btnPerfilTesisAlumno.setOnClickListener {
            mostrarMensaje("Perfil pendiente")
        }

        binding.layoutAdjuntarTesisAlumno.setOnClickListener {
            mostrarMensaje("Se registrara una version sin archivo fisico.")
        }

        binding.btnSubirNuevaVersionTesisAlumno.setOnClickListener {
            subirNuevaVersion()
        }

        binding.cardObservacionesTesisAlumno.setOnClickListener {
            mostrarMensaje(binding.tvDetalleObservacionesTesisAlumno.text.toString())
        }

        binding.itemInicioTesisAlumno.setOnClickListener {
            startActivity(Intent(this, DashboardAlumnoActivity::class.java))
            finish()
        }

        binding.itemGrupoTesisAlumno.setOnClickListener {
            startActivity(Intent(this, AlumnoGrupoDetalleActivity::class.java))
            finish()
        }

        binding.itemPerfilTesisAlumno.setOnClickListener {
            mostrarMensaje("Perfil pendiente")
        }
    }

    private fun subirNuevaVersion() {
        val guardado = alumnoGrupoDetalleDao.registrarNuevaVersionTesis(
            sessionManager.getIdUsuario().trim(),
            tituloTrabajoActual.ifBlank { "Documento de investigacion" }
        )

        if (guardado) {
            mostrarMensaje("Nueva version registrada.")
            cargarDatosTesis()
        } else {
            mostrarMensaje("No se pudo registrar la version.")
        }
    }

    private fun mostrarMensaje(mensaje: String) {
        Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show()
    }
}
