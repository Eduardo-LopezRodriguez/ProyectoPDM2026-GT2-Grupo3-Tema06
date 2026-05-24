package com.example.gradues.ui.dashboard.alumno

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.gradues.R
import com.example.gradues.databinding.ActivityDetalleSubgrupoEspecializacionBinding

class DetalleSubgrupoEspecializacionActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetalleSubgrupoEspecializacionBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetalleSubgrupoEspecializacionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        configurarEventos()
    }

    private fun configurarEventos() {
        binding.btnBackSubgrupoEspecializacion.setOnClickListener { finish() }

        findViewById<View>(R.id.btnInfoIntegrante1Especializacion).setOnClickListener {
            mostrarMensaje("Información pendiente de implementar")
        }
        findViewById<View>(R.id.btnInfoIntegrante2Especializacion).setOnClickListener {
            mostrarMensaje("Información pendiente de implementar")
        }
        findViewById<View>(R.id.btnInfoIntegrante3Especializacion).setOnClickListener {
            mostrarMensaje("Información pendiente de implementar")
        }

        binding.btnVerPropuestasEspecializacion.setOnClickListener {
            startActivity(Intent(this, RegistrarPropuestasEspecializacionActivity::class.java))
        }

        binding.btnSubirTesinaEspecializacion.setOnClickListener {
            startActivity(Intent(this, TesinaGrupoEspecializacionActivity::class.java))
        }

        binding.cardTesinaGrupoEspecializacion.setOnClickListener {
            startActivity(Intent(this, TesinaGrupoEspecializacionActivity::class.java))
        }

        binding.btnHistorialTesinaEspecializacion.setOnClickListener { mostrarMensaje("Historial pendiente de implementar") }
        binding.itemInicioSubgrupoEspecializacion.setOnClickListener { finish() }
        binding.itemGrupoSubgrupoEspecializacion.setOnClickListener { mostrarMensaje("Ya estás en Grupo") }
        binding.itemPerfilSubgrupoEspecializacion.setOnClickListener { mostrarMensaje("Perfil pendiente de implementar") }
        binding.btnNotificacionesSubgrupoEspecializacion.setOnClickListener { mostrarMensaje("Notificaciones pendientes de implementar") }
        binding.btnPerfilSubgrupoEspecializacion.setOnClickListener { mostrarMensaje("Perfil pendiente de implementar") }
    }

    private fun mostrarMensaje(mensaje: String) {
        Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show()
    }
}
