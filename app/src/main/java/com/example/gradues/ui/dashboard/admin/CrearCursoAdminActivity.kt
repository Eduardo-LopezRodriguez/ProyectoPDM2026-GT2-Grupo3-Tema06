package com.example.gradues.ui.dashboard.admin

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.gradues.data.dao.GestionCursosAdminDao
import com.example.gradues.data.db.DatabaseHelper
import com.example.gradues.databinding.ActivityCrearCursoAdminBinding
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class CrearCursoAdminActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCrearCursoAdminBinding
    private lateinit var dao: GestionCursosAdminDao

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityCrearCursoAdminBinding.inflate(layoutInflater)
        setContentView(binding.root)

        dao = GestionCursosAdminDao(DatabaseHelper(this))

        configurarEventos()
    }

    private fun configurarEventos() {
        // Selector de fecha
        binding.etFechaCreacion.setOnClickListener {
            mostrarDatePicker()
        }

        binding.btnGuardarCurso.setOnClickListener {
            guardarCurso()
        }

        binding.btnCancelarCurso.setOnClickListener {
            finish()
        }
    }

    private fun mostrarDatePicker() {
        val calendario = Calendar.getInstance()

        DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                val fecha = "%04d-%02d-%02d".format(year, month + 1, dayOfMonth)
                binding.etFechaCreacion.setText(fecha)
            },
            calendario.get(Calendar.YEAR),
            calendario.get(Calendar.MONTH),
            calendario.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun guardarCurso() {
        val nombre = binding.etNombreCurso.text.toString().trim()
        val ciclo  = binding.etCicloAcademico.text.toString().trim()
        val cupoTxt = binding.etCupoMaximo.text.toString().trim()
        val fecha  = binding.etFechaCreacion.text.toString().trim()

        // Validaciones
        if (nombre.isBlank()) {
            binding.etNombreCurso.error = "El nombre del curso es obligatorio"
            binding.etNombreCurso.requestFocus()
            return
        }
        if (ciclo.isBlank()) {
            binding.etCicloAcademico.error = "El ciclo académico es obligatorio"
            binding.etCicloAcademico.requestFocus()
            return
        }
        if (cupoTxt.isBlank()) {
            binding.etCupoMaximo.error = "El cupo máximo es obligatorio"
            binding.etCupoMaximo.requestFocus()
            return
        }
        val cupo = cupoTxt.toIntOrNull()
        if (cupo == null || cupo <= 0) {
            binding.etCupoMaximo.error = "Ingrese un cupo válido mayor a 0"
            binding.etCupoMaximo.requestFocus()
            return
        }
        if (fecha.isBlank()) {
            Toast.makeText(this, "Seleccione una fecha de inicio", Toast.LENGTH_SHORT).show()
            return
        }

        val exitoso = dao.crearCurso(nombre, ciclo, cupo, fecha)

        if (exitoso) {
            Toast.makeText(this, "Curso creado exitosamente", Toast.LENGTH_SHORT).show()
            finish()
        } else {
            Toast.makeText(this, "Error al crear el curso. Intente de nuevo.", Toast.LENGTH_LONG).show()
        }
    }
}
