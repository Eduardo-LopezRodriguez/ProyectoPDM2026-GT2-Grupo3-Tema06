package com.example.gradues.ui.dashboard.admin

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.gradues.data.dao.UsuarioAdminDao
import com.example.gradues.data.db.DatabaseHelper
import com.example.gradues.data.model.UsuarioAdminModel
import com.example.gradues.databinding.ActivityGestionUsuariosAdminBinding
import com.example.gradues.utils.SessionManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import android.content.Intent

class GestionUsuariosAdminActivity : AppCompatActivity() {

    private lateinit var binding: ActivityGestionUsuariosAdminBinding
    private lateinit var usuarioDao: UsuarioAdminDao
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityGestionUsuariosAdminBinding.inflate(layoutInflater)
        setContentView(binding.root)

        usuarioDao = UsuarioAdminDao(DatabaseHelper(this))
        sessionManager = SessionManager(this)

        configurarEventos()
        cargarUsuarios()
    }

    override fun onResume() {
        super.onResume()

        if (::usuarioDao.isInitialized) {
            cargarUsuarios()
        }
    }

    private fun configurarEventos() {
        binding.btnCrearUsuario.setOnClickListener {
            val intent = Intent(this, RegistroUsuarioAdminActivity::class.java)
            startActivity(intent)
        }

        binding.btnActualizar.setOnClickListener {
            cargarUsuarios()
        }

        binding.btnVolver.setOnClickListener {
            finish()
        }
    }

    private fun cargarUsuarios() {
        val usuarios = usuarioDao.listarUsuarios()

        binding.contenedorUsuarios.removeAllViews()

        if (usuarios.isEmpty()) {
            binding.tvSinUsuarios.visibility = View.VISIBLE
            return
        }

        binding.tvSinUsuarios.visibility = View.GONE

        usuarios.forEach { usuario ->
            binding.contenedorUsuarios.addView(crearTarjetaUsuario(usuario))
        }
    }

    private fun crearTarjetaUsuario(usuario: UsuarioAdminModel): View {
        val card = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(0xFFFFFFFF.toInt())
            elevation = 4f
            setPadding(20, 18, 20, 18)

            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            params.setMargins(0, 0, 0, 18)
            layoutParams = params
        }

        card.addView(crearTextoTitulo(usuario.nombreUsuario))
        card.addView(crearTextoNormal("Rol: ${usuario.nombreRol}"))
        card.addView(crearTextoNormal("Estado: ${usuario.estadoUsuario}"))

        if (!usuario.carnetUsuario.isNullOrBlank()) {
            card.addView(crearTextoNormal("Carnet/Código: ${usuario.carnetUsuario}"))
        }

        if (!usuario.correoUsuario.isNullOrBlank()) {
            card.addView(crearTextoNormal("Correo: ${usuario.correoUsuario}"))
        }

        if (!usuario.carreraUsuario.isNullOrBlank()) {
            card.addView(crearTextoNormal("Carrera: ${usuario.carreraUsuario}"))
        }

        val idUsuarioSesion = sessionManager.getIdUsuario()

        val esUsuarioActual =
            idUsuarioSesion == usuario.idUsuario.toString() ||
                    idUsuarioSesion.equals(usuario.carnetUsuario.orEmpty(), ignoreCase = true) ||
                    idUsuarioSesion.equals(usuario.correoUsuario.orEmpty(), ignoreCase = true)

        val btnEditar = Button(this).apply {
            isAllCaps = false
            text = "Editar usuario"
            setTextColor(0xFFFFFFFF.toInt())
            setBackgroundColor(0xFFB71C1C.toInt())

            setOnClickListener {
                val intent = Intent(
                    this@GestionUsuariosAdminActivity,
                    EditarUsuarioAdminActivity::class.java
                )

                intent.putExtra("idUsuario", usuario.idUsuario)
                startActivity(intent)
            }
        }

        card.addView(btnEditar)

        val btnCambiarEstado = Button(this).apply {
            isAllCaps = false

            val estaActivo = usuario.estadoUsuario.equals("Activo", ignoreCase = true)

            if (esUsuarioActual) {
                text = "Usuario en sesión"
                isEnabled = false
                alpha = 0.6f
                setTextColor(0xFF666666.toInt())
                setBackgroundColor(0xFFE0E0E0.toInt())
            } else {
                text = if (estaActivo) {
                    "Desactivar usuario"
                } else {
                    "Activar usuario"
                }

                if (estaActivo) {
                    setTextColor(0xFFB71C1C.toInt())
                    setBackgroundColor(0xFFFFFFFF.toInt())
                } else {
                    setTextColor(0xFFFFFFFF.toInt())
                    setBackgroundColor(0xFFB71C1C.toInt())
                }

                setOnClickListener {
                    confirmarCambioEstado(usuario)
                }
            }
        }

        card.addView(btnCambiarEstado)

        return card
    }

    private fun confirmarCambioEstado(usuario: UsuarioAdminModel) {
        val idUsuarioSesion = sessionManager.getIdUsuario()

        val esUsuarioActual =
            idUsuarioSesion == usuario.idUsuario.toString() ||
                    idUsuarioSesion.equals(usuario.carnetUsuario.orEmpty(), ignoreCase = true) ||
                    idUsuarioSesion.equals(usuario.correoUsuario.orEmpty(), ignoreCase = true)

        if (esUsuarioActual) {
            Toast.makeText(
                this,
                "No puedes cambiar el estado del usuario que tiene la sesión activa.",
                Toast.LENGTH_LONG
            ).show()
            return
        }

        val estaActivo = usuario.estadoUsuario.equals("Activo", ignoreCase = true)
        val nuevoEstado = if (estaActivo) "Inactivo" else "Activo"

        MaterialAlertDialogBuilder(this)
            .setTitle("Cambiar estado")
            .setMessage(
                """
                Usuario:
                ${usuario.nombreUsuario}
                
                Nuevo estado:
                $nuevoEstado
                """.trimIndent()
            )
            .setPositiveButton("Confirmar") { _, _ ->
                cambiarEstado(usuario.idUsuario, nuevoEstado)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun cambiarEstado(idUsuario: Int, nuevoEstado: String) {
        val idUsuarioSesion = sessionManager.getIdUsuario()

        if (idUsuarioSesion == idUsuario.toString()) {
            Toast.makeText(
                this,
                "No puedes cambiar el estado del usuario que tiene la sesión activa.",
                Toast.LENGTH_LONG
            ).show()
            return
        }

        val exito = usuarioDao.cambiarEstadoUsuario(idUsuario, nuevoEstado)

        if (exito) {
            Toast.makeText(
                this,
                "Estado actualizado correctamente.",
                Toast.LENGTH_LONG
            ).show()

            cargarUsuarios()
        } else {
            Toast.makeText(
                this,
                "No se pudo actualizar el estado del usuario.",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun crearTextoTitulo(texto: String): TextView {
        return TextView(this).apply {
            text = texto
            textSize = 18f
            setTextColor(0xFF212121.toInt())
            setTypeface(null, android.graphics.Typeface.BOLD)
            setPadding(0, 0, 0, 8)
        }
    }

    private fun crearTextoNormal(texto: String): TextView {
        return TextView(this).apply {
            text = texto
            textSize = 14f
            setTextColor(0xFF333333.toInt())
            setPadding(0, 4, 0, 4)
        }
    }
}