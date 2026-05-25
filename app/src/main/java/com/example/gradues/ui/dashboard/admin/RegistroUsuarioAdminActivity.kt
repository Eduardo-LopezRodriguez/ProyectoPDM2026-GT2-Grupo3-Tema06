package com.example.gradues.ui.dashboard.admin

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.gradues.data.dao.UsuarioAdminDao
import com.example.gradues.data.db.DatabaseHelper
import com.example.gradues.data.model.RolAdminModel
import com.example.gradues.databinding.ActivityRegistroUsuarioAdminBinding

class RegistroUsuarioAdminActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegistroUsuarioAdminBinding
    private lateinit var usuarioDao: UsuarioAdminDao

    private val roles = mutableListOf<RolAdminModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityRegistroUsuarioAdminBinding.inflate(layoutInflater)
        setContentView(binding.root)

        usuarioDao = UsuarioAdminDao(DatabaseHelper(this))

        cargarRoles()
        configurarEventos()
    }

    private fun cargarRoles() {
        roles.clear()
        roles.addAll(usuarioDao.listarRoles())

        if (roles.isEmpty()) {
            Toast.makeText(
                this,
                "No hay roles registrados en la base.",
                Toast.LENGTH_LONG
            ).show()
            finish()
            return
        }

        val nombresRoles = roles.map { it.nombreRol }

        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            nombresRoles
        )

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        binding.spRol.adapter = adapter
    }

    private fun configurarEventos() {
        binding.btnGuardar.setOnClickListener {
            guardarUsuario()
        }

        binding.btnCancelar.setOnClickListener {
            finish()
        }
    }

    private fun guardarUsuario() {
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
        val contrasenia = binding.etContrasenia.text.toString().trim()

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

        if (contrasenia.isBlank()) {
            binding.etContrasenia.error = "Ingrese la contraseña"
            return
        }

        if (contrasenia.length < 4) {
            binding.etContrasenia.error = "La contraseña debe tener al menos 4 caracteres"
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

        if (usuarioDao.existeCorreo(correo)) {
            binding.etCorreo.error = "Ya existe un usuario con este correo"
            return
        }

        if (!carnet.isNullOrBlank() && usuarioDao.existeCarnet(carnet)) {
            binding.etCarnet.error = "Ya existe un usuario con este carnet/código"
            return
        }

        val exito = usuarioDao.crearUsuario(
            idRol = rolSeleccionado.idRol,
            primerNombre = primerNombre,
            segundoNombre = segundoNombre,
            primerApellido = primerApellido,
            segundoApellido = segundoApellido,
            correo = correo,
            carnet = carnet,
            dui = dui,
            carrera = carrera,
            contrasenia = contrasenia
        )

        if (exito) {
            Toast.makeText(
                this,
                "Usuario creado correctamente.",
                Toast.LENGTH_LONG
            ).show()

            setResult(RESULT_OK)
            finish()
        } else {
            mostrarMensaje("No se pudo crear el usuario.")
        }
    }

    private fun mostrarMensaje(mensaje: String) {
        Toast.makeText(this, mensaje, Toast.LENGTH_LONG).show()
    }
}