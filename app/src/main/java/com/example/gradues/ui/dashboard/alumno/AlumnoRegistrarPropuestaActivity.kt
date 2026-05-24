package com.example.gradues.ui.dashboard.alumno

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.gradues.data.dao.AlumnoGrupoDetalleDao
import com.example.gradues.data.db.DatabaseHelper
import com.example.gradues.databinding.ActivityAlumnoRegistrarPropuestaBinding
import com.example.gradues.utils.SessionManager

class AlumnoRegistrarPropuestaActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAlumnoRegistrarPropuestaBinding
    private lateinit var alumnoGrupoDetalleDao: AlumnoGrupoDetalleDao
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAlumnoRegistrarPropuestaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        alumnoGrupoDetalleDao = AlumnoGrupoDetalleDao(DatabaseHelper(this))
        sessionManager = SessionManager(this)

        cargarDatosGrupo()
        configurarEventos()
    }

    private fun cargarDatosGrupo() {
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

        binding.tvNombreGrupoRegistrarPropuestaAlumno.text =
            "${grupo.nombreGrupo} ${grupo.codigoGrupo}"
        binding.tvTemaGrupoRegistrarPropuestaAlumno.text = grupo.temaTrabajo
    }

    private fun configurarEventos() {
        binding.btnBackRegistrarPropuestaAlumno.setOnClickListener { finish() }

        binding.btnNotificacionesRegistrarPropuestaAlumno.setOnClickListener {
            mostrarMensaje("Notificaciones pendientes")
        }

        binding.btnPerfilRegistrarPropuestaAlumno.setOnClickListener {
            mostrarMensaje("Perfil pendiente")
        }

        binding.layoutAdjuntarPropuesta1Alumno.setOnClickListener {
            mostrarMensaje("Adjuntar archivo para propuesta 1 pendiente")
        }

        binding.layoutAdjuntarPropuesta2Alumno.setOnClickListener {
            mostrarMensaje("Adjuntar archivo para propuesta 2 pendiente")
        }

        binding.btnGuardarPropuesta1Alumno.setOnClickListener {
            guardarPropuesta(
                binding.etTituloPropuesta1Alumno.text.toString().trim(),
                binding.etDescripcionPropuesta1Alumno.text.toString().trim()
            )
        }

        binding.btnGuardarPropuesta2Alumno.setOnClickListener {
            guardarPropuesta(
                binding.etTituloPropuesta2Alumno.text.toString().trim(),
                binding.etDescripcionPropuesta2Alumno.text.toString().trim()
            )
        }

        binding.btnGuardarBorradoresAlumno.setOnClickListener {
            mostrarMensaje("Los borradores quedan en pantalla hasta enviarlos.")
        }

        binding.btnEnviarPropuestasAlumno.setOnClickListener {
            enviarPropuestas()
        }

        binding.itemInicioRegistrarPropuestaAlumno.setOnClickListener {
            startActivity(Intent(this, DashboardAlumnoActivity::class.java))
            finish()
        }

        binding.itemGrupoRegistrarPropuestaAlumno.setOnClickListener {
            startActivity(Intent(this, AlumnoGrupoDetalleActivity::class.java))
            finish()
        }

        binding.itemPerfilRegistrarPropuestaAlumno.setOnClickListener {
            mostrarMensaje("Perfil pendiente")
        }
    }

    private fun enviarPropuestas() {
        val propuestas = listOf(
            binding.etTituloPropuesta1Alumno.text.toString().trim() to
                binding.etDescripcionPropuesta1Alumno.text.toString().trim(),
            binding.etTituloPropuesta2Alumno.text.toString().trim() to
                binding.etDescripcionPropuesta2Alumno.text.toString().trim()
        ).filter { (titulo, descripcion) ->
            titulo.isNotBlank() || descripcion.isNotBlank()
        }

        if (propuestas.isEmpty()) {
            mostrarMensaje("Debes completar al menos una propuesta.")
            return
        }

        if (propuestas.any { (titulo, descripcion) -> titulo.isBlank() || descripcion.isBlank() }) {
            mostrarMensaje("Cada propuesta enviada necesita titulo y descripcion.")
            return
        }

        val guardadas = propuestas.count { (titulo, descripcion) ->
            registrarPropuesta(titulo, descripcion, mostrarResultado = false)
        }

        if (guardadas == propuestas.size) {
            mostrarMensaje("Propuestas enviadas.")
            finish()
        } else {
            mostrarMensaje("Algunas propuestas no se pudieron guardar.")
        }
    }

    private fun guardarPropuesta(titulo: String, descripcion: String) {
        registrarPropuesta(titulo, descripcion, mostrarResultado = true)
    }

    private fun registrarPropuesta(
        titulo: String,
        descripcion: String,
        mostrarResultado: Boolean
    ): Boolean {
        if (titulo.isBlank() || descripcion.isBlank()) {
            if (mostrarResultado) mostrarMensaje("Completa titulo y descripcion.")
            return false
        }

        val guardada = alumnoGrupoDetalleDao.registrarPropuesta(
            sessionManager.getIdUsuario().trim(),
            titulo,
            descripcion
        )

        if (mostrarResultado) {
            mostrarMensaje(if (guardada) "Propuesta guardada." else "No se pudo guardar la propuesta.")
        }

        return guardada
    }

    private fun mostrarMensaje(mensaje: String) {
        Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show()
    }
}
