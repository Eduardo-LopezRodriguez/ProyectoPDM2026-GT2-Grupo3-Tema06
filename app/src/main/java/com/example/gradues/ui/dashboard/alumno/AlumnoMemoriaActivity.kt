package com.example.gradues.ui.dashboard.alumno

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.gradues.data.dao.AlumnoPasantiaDao
import com.example.gradues.data.db.DatabaseHelper
import com.example.gradues.databinding.ActivityAlumnoMemoriaBinding
import com.example.gradues.utils.SessionManager

class AlumnoMemoriaActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAlumnoMemoriaBinding
    private lateinit var alumnoPasantiaDao: AlumnoPasantiaDao
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAlumnoMemoriaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        alumnoPasantiaDao = AlumnoPasantiaDao(DatabaseHelper(this))
        sessionManager = SessionManager(this)

        cargarMemoria()
        configurarEventos()
    }

    override fun onResume() {
        super.onResume()
        cargarMemoria()
    }

    private fun cargarMemoria() {
        val idUsuario = sessionManager.getIdUsuario().trim()
        if (idUsuario.isBlank()) {
            Toast.makeText(this, "No se encontro la sesion del alumno.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        val memoria = alumnoPasantiaDao.obtenerMemoriaPorAlumno(idUsuario)
        if (memoria == null) {
            binding.tvNombreDocumentoMemoriaAlumno.text = "Memoria no enviada"
            binding.tvMetaDocumentoMemoriaAlumno.text = "Sin documento registrado"
            binding.tvEstadoMemoriaAlumno.text = "Pendiente de entrega"
            binding.tvCabeceraObservacionMemoriaAlumno.text = "Docente"
            binding.tvDetalleObservacionMemoriaAlumno.text = "Sin observaciones."
            return
        }

        binding.tvNombreDocumentoMemoriaAlumno.text =
            memoria.tituloMemoria.ifBlank { "Memoria de pasantia" }
        binding.tvMetaDocumentoMemoriaAlumno.text =
            memoria.urlDocumento ?: "Documento registrado en base de datos"
        binding.tvEstadoMemoriaAlumno.text = memoria.estadoMemoria
        binding.tvCabeceraObservacionMemoriaAlumno.text = "Observacion del docente"
        binding.tvDetalleObservacionMemoriaAlumno.text =
            memoria.observacionMemoria?.ifBlank { "Sin observaciones." } ?: "Sin observaciones."
    }

    private fun configurarEventos() {
        binding.btnBackMemoriaAlumno.setOnClickListener {
            finish()
        }

        binding.btnNotificacionesMemoriaAlumno.setOnClickListener {
            mostrarMensaje("Notificaciones pendientes")
        }

        binding.btnPerfilMemoriaAlumno.setOnClickListener {
            mostrarMensaje("Perfil pendiente")
        }

        binding.layoutDocumentoMemoriaAlumno.setOnClickListener {
            mostrarMensaje(binding.tvMetaDocumentoMemoriaAlumno.text.toString())
        }

        binding.layoutEstadoMemoriaAlumno.setOnClickListener {
            mostrarMensaje("Estado: ${binding.tvEstadoMemoriaAlumno.text}")
        }

        binding.btnEnviarMensajeMemoriaAlumno.setOnClickListener {
            val mensaje = binding.etMensajeMemoriaAlumno.text.toString().trim()
            if (mensaje.isBlank()) {
                mostrarMensaje("Escribe un mensaje primero.")
            } else {
                binding.etMensajeMemoriaAlumno.text?.clear()
                mostrarMensaje("Mensaje registrado localmente.")
            }
        }

        binding.btnSubirNuevaMemoriaAlumno.setOnClickListener {
            mostrarMensaje("Carga de archivo pendiente.")
        }

        binding.itemInicioMemoriaAlumno.setOnClickListener {
            startActivity(Intent(this, DashboardAlumnoActivity::class.java))
            finish()
        }

        binding.itemGrupoMemoriaAlumno.setOnClickListener {
            startActivity(Intent(this, AlumnoPasantiaDetalleActivity::class.java))
            finish()
        }

        binding.itemPerfilMemoriaAlumno.setOnClickListener {
            mostrarMensaje("Perfil pendiente")
        }
    }

    private fun mostrarMensaje(mensaje: String) {
        Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show()
    }
}
