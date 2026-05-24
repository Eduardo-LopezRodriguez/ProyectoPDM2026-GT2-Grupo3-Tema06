package com.example.gradues.ui.dashboard.alumno

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.gradues.databinding.ActivityAlumnoRegistrarPropuestaBinding

class AlumnoRegistrarPropuestaActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAlumnoRegistrarPropuestaBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAlumnoRegistrarPropuestaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        cargarDatosTemporales()
        configurarEventos()
    }

    private fun cargarDatosTemporales() {
        binding.tvNombreGrupoRegistrarPropuestaAlumno.text = "Grupo de Investigación 01"
        binding.tvTemaGrupoRegistrarPropuestaAlumno.text = "Sistema de seguimiento académico"
    }

    private fun configurarEventos() {
        binding.btnBackRegistrarPropuestaAlumno.setOnClickListener {
            finish()
        }

        binding.layoutAdjuntarPropuesta1Alumno.setOnClickListener {
            mostrarMensaje("Adjuntar archivo para propuesta 1 pendiente")
        }

        binding.layoutAdjuntarPropuesta2Alumno.setOnClickListener {
            mostrarMensaje("Adjuntar archivo para propuesta 2 pendiente")
        }

        binding.btnGuardarPropuesta1Alumno.setOnClickListener {
            mostrarMensaje("Borrador de propuesta 1 guardado")
        }

        binding.btnGuardarPropuesta2Alumno.setOnClickListener {
            mostrarMensaje("Borrador de propuesta 2 guardado")
        }

        binding.btnGuardarBorradoresAlumno.setOnClickListener {
            mostrarMensaje("Borradores guardados localmente")
        }

        binding.btnEnviarPropuestasAlumno.setOnClickListener {
            enviarPropuestas()
        }

        binding.itemInicioRegistrarPropuestaAlumno.setOnClickListener {
            finish()
        }

        binding.itemGrupoRegistrarPropuestaAlumno.setOnClickListener {
            val intent = Intent(this, AlumnoGrupoDetalleActivity::class.java)
            startActivity(intent)
        }

        binding.itemPerfilRegistrarPropuestaAlumno.setOnClickListener {
            mostrarMensaje("Perfil pendiente")
        }
    }

    private fun enviarPropuestas() {
        val titulo1 = binding.etTituloPropuesta1Alumno.text.toString().trim()
        val descripcion1 = binding.etDescripcionPropuesta1Alumno.text.toString().trim()
        val titulo2 = binding.etTituloPropuesta2Alumno.text.toString().trim()
        val descripcion2 = binding.etDescripcionPropuesta2Alumno.text.toString().trim()

        val propuesta1Llena = titulo1.isNotEmpty() || descripcion1.isNotEmpty()
        val propuesta2Llena = titulo2.isNotEmpty() || descripcion2.isNotEmpty()

        if (!propuesta1Llena && !propuesta2Llena) {
            mostrarMensaje("Debes completar al menos una propuesta")
            return
        }

        mostrarMensaje("Envío de propuestas pendiente de integración con DAO")
    }

    private fun mostrarMensaje(mensaje: String) {
        Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show()
    }
}