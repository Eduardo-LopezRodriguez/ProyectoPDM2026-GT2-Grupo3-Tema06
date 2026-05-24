package com.example.gradues.ui.dashboard.alumno

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.gradues.databinding.ActivityAlumnoPasantiaDetalleBinding

class AlumnoPasantiaDetalleActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAlumnoPasantiaDetalleBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAlumnoPasantiaDetalleBinding.inflate(layoutInflater)
        setContentView(binding.root)

        cargarDatosTemporales()
        configurarEventos()
    }

    private fun cargarDatosTemporales() {
        binding.tvNombreAlumnoPasantia.text = "Eduardo López"
        binding.tvCarnetAlumnoPasantia.text = "LR21008"
        binding.tvDocentePasantia.text = "Docente a cargo: César Augusto"
        binding.tvModalidadPasantia.text = "Pasantía profesional"

        binding.tvEmpresaPasantia.text = "Crowley Shared Services S.A. de C.V."
        binding.tvDireccionPasantia.text =
            "Avenida Olímpica, entre 65 y 67 Avenida Sur, Colonia Escalón, Edificio Corporativo San Salvador"
        binding.tvSupervisorPasantia.text = "Reporta a: Einer Villafuerte"
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
            mostrarMensaje("Pantalla de bitácoras pendiente")
        }

        binding.btnSubirMemoriaPasantia.setOnClickListener {
            mostrarMensaje("Pantalla de memoria pendiente")
        }

        binding.itemInicioPasantiaAlumno.setOnClickListener {
            val intent = Intent(this, DashboardAlumnoActivity::class.java)
            startActivity(intent)
            finish()
        }

        binding.itemGrupoPasantiaAlumno.setOnClickListener {
            mostrarMensaje("Ya estás en Mi pasantía")
        }

        binding.itemPerfilPasantiaAlumno.setOnClickListener {
            mostrarMensaje("Perfil pendiente")
        }
    }

    private fun mostrarMensaje(mensaje: String) {
        Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show()
    }
}