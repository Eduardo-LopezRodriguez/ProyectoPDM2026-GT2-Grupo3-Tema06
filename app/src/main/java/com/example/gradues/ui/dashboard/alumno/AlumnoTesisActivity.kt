package com.example.gradues.ui.dashboard.alumno

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.gradues.databinding.ActivityAlumnoTesisBinding

class AlumnoTesisActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAlumnoTesisBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAlumnoTesisBinding.inflate(layoutInflater)
        setContentView(binding.root)

        cargarDatosTemporales()
        configurarEventos()
    }

    private fun cargarDatosTemporales() {
        binding.tvNombreGrupoTesisAlumno.text = "Grupo de Investigación 01"
        binding.tvTemaGrupoTesisAlumno.text = "Sistema de seguimiento académico"

        binding.tvDescripcionTesisAlumno.text =
            "Implementación del sistema de seguimiento académico para la UES"
        binding.tvEstadoTesisAlumno.text = "Estado: No subida"
        binding.tvUltimaVersionTesisAlumno.text = "Última versión: No enviada"

        binding.tvDetalleObservacionesTesisAlumno.text =
            "Todavía no has subido ninguna versión de la tesis."

        binding.tvDetalleRecordatorioTesisAlumno.text =
            "Debes subir la versión finalizada de tu tesis antes del 25 de mayo"
    }

    private fun configurarEventos() {
        binding.btnBackTesisAlumno.setOnClickListener {
            finish()
        }

        binding.layoutAdjuntarTesisAlumno.setOnClickListener {
            mostrarMensaje("Adjuntar archivo de tesis pendiente")
        }

        binding.btnSubirNuevaVersionTesisAlumno.setOnClickListener {
            mostrarMensaje("Subida de nueva versión pendiente de integración")
        }

        binding.cardObservacionesTesisAlumno.setOnClickListener {
            mostrarMensaje("Detalle de observaciones pendiente")
        }

        binding.itemInicioTesisAlumno.setOnClickListener {
            finish()
        }

        binding.itemGrupoTesisAlumno.setOnClickListener {
            val intent = Intent(this, AlumnoGrupoDetalleActivity::class.java)
            startActivity(intent)
        }

        binding.itemPerfilTesisAlumno.setOnClickListener {
            mostrarMensaje("Perfil pendiente")
        }
    }

    private fun mostrarMensaje(mensaje: String) {
        Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show()
    }
}