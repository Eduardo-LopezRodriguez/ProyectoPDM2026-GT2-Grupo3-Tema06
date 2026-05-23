package com.example.gradues.ui.login

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.gradues.data.dao.UsuarioDao
import com.example.gradues.data.db.DatabaseHelper
import com.example.gradues.databinding.ActivityLoginBinding
import com.example.gradues.utils.SessionManager
import android.content.Intent
import android.widget.Toast
import com.example.gradues.ui.dashboard.docente.DashboardDocenteActivity

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var usuarioDao: UsuarioDao
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        usuarioDao = UsuarioDao(DatabaseHelper(this))
        sessionManager = SessionManager(this)

        configurarEventos()
    }

    private fun configurarEventos() {
        binding.btnLogin.setOnClickListener {
            iniciarSesion()
        }
    }

    private fun iniciarSesion() {
        val idUsuario = binding.etIdUsuario.text?.toString()?.trim().orEmpty()
        val contra = binding.etContra.text?.toString()?.trim().orEmpty()

        limpiarError()

        if (idUsuario.isEmpty()) {
            mostrarError("Ingrese su usuario.")
            return
        }

        if (contra.isEmpty()) {
            mostrarError("Ingrese su contraseña.")
            return
        }

        binding.progressBar.visibility = View.VISIBLE
        binding.btnLogin.isEnabled = false

        val usuario = usuarioDao.login(idUsuario, contra)

        binding.progressBar.visibility = View.GONE
        binding.btnLogin.isEnabled = true

        if (usuario == null) {
            mostrarError("Usuario o contraseña incorrectos.")
            return
        }

        sessionManager.guardarSesion(
            idUsuario = usuario.IdUsuario,
            nombre = usuario.NombreUsuario,
            rol = usuario.NombreRol
        )

        redirigirSegunRol(usuario.NombreRol)

// Pendiente: en el siguiente bloque se redirigirá al dashboard según el rol.
// Por ahora solo dejamos la sesión guardada sin mostrar mensaje de prueba.
    }

    private fun mostrarError(mensaje: String) {
        binding.tvError.text = mensaje
        binding.tvError.visibility = View.VISIBLE
    }

    private fun limpiarError() {
        binding.tvError.text = ""
        binding.tvError.visibility = View.GONE
    }

    private fun redirigirSegunRol(rol: String) {
        when (rol.trim().lowercase()) {
            "docente", "administrador" -> {
                val intent = Intent(this, DashboardDocenteActivity::class.java)
                startActivity(intent)
                finish()
            }

            "alumno" -> {
                Toast.makeText(
                    this,
                    "Dashboard de alumno pendiente de implementar.",
                    Toast.LENGTH_LONG
                ).show()
            }

            else -> {
                mostrarError("Rol no reconocido.")
            }
        }
    }
}