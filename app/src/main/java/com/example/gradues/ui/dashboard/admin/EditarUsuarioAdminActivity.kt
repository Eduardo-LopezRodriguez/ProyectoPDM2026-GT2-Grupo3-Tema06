package com.example.gradues.ui.dashboard.admin

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.gradues.data.dao.UsuarioAdminDao
import com.example.gradues.data.db.DatabaseHelper
import com.example.gradues.data.model.RolAdminModel
import com.example.gradues.data.model.UsuarioAdminDetalleModel
import com.example.gradues.databinding.ActivityEditarUsuarioAdminBinding
import com.example.gradues.utils.SessionManager

class EditarUsuarioAdminActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditarUsuarioAdminBinding
    private lateinit var usuarioDao: UsuarioAdminDao
    private lateinit var sessionManager: SessionManager

    private val roles = mutableListOf<RolAdminModel>()
    private var idUsuarioEditar: Int = -1
    private var usuarioActual: UsuarioAdminDetalleModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityEditarUsuarioAdminBinding.inflate(layoutInflater)
        setContentView(binding.root)

        usuarioDao = UsuarioAdminDao(DatabaseHelper(this))
        sessionManager = SessionManager(this)

        idUsuarioEditar = intent.getIntExtra("idUsuario", -1)

        if (idUsuarioEditar <= 0) {
            Toast.makeText(this, "No se recibió el usuario a editar.", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        cargarRoles()
        cargarUsuario()
        configurarEventos()
    }

    private fun cargarRoles() {
        roles.clear()
        roles.addAll(usuarioDao.listarRoles())

        if (roles.isEmpty()) {
            Toast.makeText(this, "No hay roles registrados.", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            roles.map { it.nombreRol }
        )

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spRol.adapter = adapter
    }

    private fun cargarUsuario() {
        val usuario = usuarioDao.obtenerUsuarioPorId(idUsuarioEditar)

        if (usuario == null) {
            Toast.makeText(this, "No se encontró el usuario.", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        usuarioActual = usuario

        val posicionRol = roles.indexOfFirst { it.idRol == usuario.idRol }
        if (posicionRol >= 0) {
            binding.spRol.setSelection(posicionRol)
        }

        binding.etPrimerNombre.setText(usuario.primerNombreUsuario)
        binding.etSegundoNombre.setText(usuario.segundoNombreUsuario.orEmpty())
        binding.etPrimerApellido.setText(usuario.primerApellidoUsuario)
        binding.etSegundoApellido.setText(usuario.segundoApellidoUsuario.orEmpty())
        binding.etCorreo.setText(usuario.correoUsuario)
        binding.etCarnet.setText(usuario.carnetUsuario.orEmpty())
        binding.etDui.setText(usuario.duiUsuario.orEmpty())
        binding.etCarrera.setText(usuario.carreraUsuario.orEmpty())
    }

    private fun configurarEventos() {
        binding.btnGuardar.setOnClickListener {
            guardarCambios()
        }

        binding.btnCancelar.setOnClickListener {
            finish()
        }
    }

    private fun guardarCambios() {
        val usuario = usuarioActual

        if (usuario == null) {
            mostrarMensaje("No hay usuario cargado.")
            return
        }

        val rolSeleccionado = roles.getOrNull(binding.spRol.selectedItemPosition)

        if (rolSeleccionado == null) {
            mostrarMensaje("Seleccione un rol válido.")
            return
        }

        val primerNombre = binding.etPrimerNombre.text.toString().trim()
        val segundoNombre = binding.etSegundoNombre.text.toString().trim().ifBlank { null }
        val primerApellido = binding.etPrimerApellido.text.toString().trim()
        val segundoApellido = binding.etSegundoApellido.text.toString().trim().ifBlank { null }
        val correo = binding.etCorreo.text.toString().trim()
        val carnet = binding.etCarnet.text.toString().trim().ifBlank { null }
        val dui = binding.etDui.text.toString().trim().ifBlank { null }
        val carrera = binding.etCarrera.text.toString().trim().ifBlank { null }
        val nuevaContrasenia = binding.etNuevaContrasenia.text.toString().trim().ifBlank { null }

        if (primerNombre.isBlank()) {
            binding.etPrimerNombre.error = "Ingrese el primer nombre"
            return
        }

        if (primerApellido.isBlank()) {
            binding.etPrimerApellido.error = "Ingrese el primer apellido"
            return
        }

        if (correo.isBlank()) {
            binding.etCorreo.error = "Ingrese el correo"
            return
        }

        if (!correo.contains("@")) {
            binding.etCorreo.error = "Ingrese un correo válido"
            return
        }

        if (!nuevaContrasenia.isNullOrBlank() && nuevaContrasenia.length < 4) {
            binding.etNuevaContrasenia.error = "La contraseña debe tener al menos 4 caracteres"
            return
        }

        if (rolSeleccionado.nombreRol.equals("Alumno", ignoreCase = true)) {
            if (carnet.isNullOrBlank()) {
                binding.etCarnet.error = "El carnet es obligatorio para alumnos"
                return
            }

            if (carrera.isNullOrBlank()) {
                binding.etCarrera.error = "La carrera es obligatoria para alumnos"
                return
            }
        }

        val idUsuarioSesion = sessionManager.getIdUsuario()

        val esUsuarioEnSesion =
            idUsuarioSesion == usuario.idUsuario.toString() ||
                    idUsuarioSesion.equals(usuario.carnetUsuario.orEmpty(), ignoreCase = true) ||
                    idUsuarioSesion.equals(usuario.correoUsuario, ignoreCase = true)

        if (esUsuarioEnSesion && !rolSeleccionado.nombreRol.equals("Administrador", ignoreCase = true)) {
            mostrarMensaje("No puedes quitarte el rol de administrador a ti mismo.")
            return
        }

        if (usuarioDao.existeCorreoEnOtroUsuario(correo, usuario.idUsuario)) {
            binding.etCorreo.error = "Ya existe otro usuario con este correo"
            return
        }

        if (!carnet.isNullOrBlank() && usuarioDao.existeCarnetEnOtroUsuario(carnet, usuario.idUsuario)) {
            binding.etCarnet.error = "Ya existe otro usuario con este carnet/código"
            return
        }

        val exito = usuarioDao.actualizarUsuario(
            idUsuario = usuario.idUsuario,
            idRol = rolSeleccionado.idRol,
            primerNombre = primerNombre,
            segundoNombre = segundoNombre,
            primerApellido = primerApellido,
            segundoApellido = segundoApellido,
            correo = correo,
            carnet = carnet,
            dui = dui,
            carrera = carrera,
            nuevaContrasenia = nuevaContrasenia
        )

        if (exito) {
            Toast.makeText(this, "Usuario actualizado correctamente.", Toast.LENGTH_LONG).show()
            setResult(RESULT_OK)
            finish()
        } else {
            mostrarMensaje("No se pudo actualizar el usuario.")
        }
    }

    private fun mostrarMensaje(mensaje: String) {
        Toast.makeText(this, mensaje, Toast.LENGTH_LONG).show()
    }
}