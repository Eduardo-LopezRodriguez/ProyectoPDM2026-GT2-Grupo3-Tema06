package com.example.gradues.ui.dashboard.alumno

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.gradues.R
import com.example.gradues.databinding.ActivityRegistrarPropuestasEspecializacionBinding

class RegistrarPropuestasEspecializacionActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegistrarPropuestasEspecializacionBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegistrarPropuestasEspecializacionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        configurarEventos()
    }

    private fun configurarEventos() {
        binding.btnBackRegistrarEspecializacion.setOnClickListener { finish() }
        findViewById<View>(R.id.layoutAdjuntarPropuesta1Especializacion).setOnClickListener {
            mostrarMensaje("Archivo adjunto pendiente de implementar")
        }
        findViewById<View>(R.id.layoutAdjuntarPropuesta2Especializacion).setOnClickListener {
            mostrarMensaje("Archivo adjunto pendiente de implementar")
        }
        findViewById<View>(R.id.btnGuardarPropuesta1Especializacion).setOnClickListener {
            mostrarMensaje("Propuesta guardada")
        }
        findViewById<View>(R.id.btnGuardarPropuesta2Especializacion).setOnClickListener {
            mostrarMensaje("Propuesta guardada")
        }
        binding.btnGuardarBorradoresEspecializacion.setOnClickListener { mostrarMensaje("Borradores guardados") }
        binding.btnEnviarPropuestasEspecializacion.setOnClickListener { mostrarMensaje("Propuestas enviadas") }
        binding.itemInicioRegistrarEspecializacion.setOnClickListener { finish() }
        binding.itemGrupoRegistrarEspecializacion.setOnClickListener {
            startActivity(Intent(this, DetalleSubgrupoEspecializacionActivity::class.java))
        }
        binding.itemPerfilRegistrarEspecializacion.setOnClickListener { mostrarMensaje("Perfil pendiente de implementar") }
        binding.btnNotificacionesRegistrarEspecializacion.setOnClickListener { mostrarMensaje("Notificaciones pendientes de implementar") }
        binding.btnPerfilRegistrarEspecializacion.setOnClickListener { mostrarMensaje("Perfil pendiente de implementar") }
    }

    private fun mostrarMensaje(mensaje: String) {
        Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show()
    }
}
