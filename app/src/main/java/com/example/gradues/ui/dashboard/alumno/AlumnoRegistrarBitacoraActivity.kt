package com.example.gradues.ui.dashboard.alumno

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.gradues.data.dao.AlumnoPasantiaDao
import com.example.gradues.data.db.DatabaseHelper
import com.example.gradues.databinding.ActivityAlumnoRegistrarBitacoraBinding
import com.example.gradues.utils.SessionManager
import java.util.Calendar
import java.util.Locale

class AlumnoRegistrarBitacoraActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAlumnoRegistrarBitacoraBinding
    private lateinit var alumnoPasantiaDao: AlumnoPasantiaDao
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAlumnoRegistrarBitacoraBinding.inflate(layoutInflater)
        setContentView(binding.root)

        alumnoPasantiaDao = AlumnoPasantiaDao(DatabaseHelper(this))
        sessionManager = SessionManager(this)

        configurarEventos()
    }

    private fun configurarEventos() {
        binding.btnBackRegistrarBitacoraAlumno.setOnClickListener {
            finish()
        }

        binding.etFechaBitacoraAlumno.setOnClickListener {
            mostrarSelectorFecha()
        }

        binding.layoutAdjuntoBitacoraAlumno.setOnClickListener {
            mostrarMensaje("Adjuntar archivo pendiente")
        }

        binding.btnGuardarBitacoraAlumno.setOnClickListener {
            guardarBitacora()
        }

        binding.btnCancelarBitacoraAlumno.setOnClickListener {
            finish()
        }

        binding.itemInicioRegistrarBitacoraAlumno.setOnClickListener {
            startActivity(Intent(this, DashboardAlumnoActivity::class.java))
            finish()
        }

        binding.itemGrupoRegistrarBitacoraAlumno.setOnClickListener {
            startActivity(Intent(this, AlumnoBitacorasActivity::class.java))
            finish()
        }

        binding.itemPerfilRegistrarBitacoraAlumno.setOnClickListener {
            mostrarMensaje("Perfil pendiente")
        }
    }

    private fun mostrarSelectorFecha() {
        val calendario = Calendar.getInstance()
        DatePickerDialog(
            this,
            { _, anio, mes, dia ->
                binding.etFechaBitacoraAlumno.setText(
                    String.format(Locale.US, "%04d-%02d-%02d", anio, mes + 1, dia)
                )
            },
            calendario.get(Calendar.YEAR),
            calendario.get(Calendar.MONTH),
            calendario.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun guardarBitacora() {
        val idUsuario = sessionManager.getIdUsuario().trim()
        val titulo = binding.etTituloBitacoraAlumno.text.toString().trim()
        val fecha = binding.etFechaBitacoraAlumno.text.toString().trim()
        val horas = binding.etHorasBitacoraAlumno.text.toString().trim().toIntOrNull()
        val descripcion = binding.etDescripcionBitacoraAlumno.text.toString().trim()

        if (idUsuario.isBlank()) {
            mostrarMensaje("No se encontro la sesion del alumno.")
            return
        }

        if (titulo.isBlank() || fecha.isBlank() || descripcion.isBlank() || horas == null || horas <= 0) {
            mostrarMensaje("Completa titulo, fecha, horas y descripcion.")
            return
        }

        val guardado = alumnoPasantiaDao.registrarBitacoraAlumno(
            idUsuario = idUsuario,
            fechaActividad = fecha,
            tituloActividad = titulo,
            descripcionActividad = descripcion,
            totalHorasTrabajadas = horas
        )

        if (guardado) {
            mostrarMensaje("Bitacora registrada.")
            finish()
        } else {
            mostrarMensaje("No se pudo registrar la bitacora.")
        }
    }

    private fun mostrarMensaje(mensaje: String) {
        Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show()
    }
}
