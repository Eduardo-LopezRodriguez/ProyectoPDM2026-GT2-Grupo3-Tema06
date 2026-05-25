package com.example.gradues.ui.dashboard.alumno

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.gradues.databinding.ActivityTesinaGrupoEspecializacionBinding

class TesinaGrupoEspecializacionActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTesinaGrupoEspecializacionBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTesinaGrupoEspecializacionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        configurarEventos()
    }

    private fun configurarEventos() {
        binding.btnBackTesinaEspecializacion.setOnClickListener { finish() }
        binding.cardObservacionesTesinaEspecializacion.setOnClickListener {
            mostrarMensaje("Observaciones pendientes de implementar")
        }
        binding.layoutAdjuntarTesinaEspecializacion.setOnClickListener {
            mostrarMensaje("Archivo adjunto pendiente de implementar")
        }
        binding.btnSubirNuevaVersionTesinaEspecializacion.setOnClickListener {
            mostrarMensaje("Subida pendiente de implementar")
        }
        binding.itemInicioTesinaEspecializacion.setOnClickListener { finish() }
        binding.itemGrupoTesinaEspecializacion.setOnClickListener {
            startActivity(Intent(this, DetalleSubgrupoEspecializacionActivity::class.java))
        }
        binding.itemPerfilTesinaEspecializacion.setOnClickListener { mostrarMensaje("Perfil pendiente de implementar") }
        binding.btnNotificacionesTesinaEspecializacion.setOnClickListener { mostrarMensaje("Notificaciones pendientes de implementar") }
        binding.btnPerfilTesinaEspecializacion.setOnClickListener { mostrarMensaje("Perfil pendiente de implementar") }
    }

    private fun mostrarMensaje(mensaje: String) {
        Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show()
    }
}
