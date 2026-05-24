package com.example.gradues.ui.dashboard.alumno

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.gradues.data.dao.TrabajoDisponibleAlumnoDao
import com.example.gradues.data.db.DatabaseHelper
import com.example.gradues.data.model.TrabajoDisponibleAlumnoModel
import com.example.gradues.databinding.ActivityAplicarTrabajoAlumnoBinding
import com.example.gradues.utils.SessionManager

class AplicarTrabajoAlumnoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAplicarTrabajoAlumnoBinding
    private lateinit var trabajoDisponibleAlumnoDao: TrabajoDisponibleAlumnoDao
    private lateinit var trabajoDisponibleAlumnoAdapter: TrabajoDisponibleAlumnoAdapter

    private lateinit var sessionManager: SessionManager

    private var trabajoSeleccionado: TrabajoDisponibleAlumnoModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        sessionManager = SessionManager(this)
        super.onCreate(savedInstanceState)
        binding = ActivityAplicarTrabajoAlumnoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        trabajoDisponibleAlumnoDao = TrabajoDisponibleAlumnoDao(DatabaseHelper(this))

        configurarRecyclerView()
        configurarEventos()
        cargarTrabajosDisponibles()
    }

    private fun configurarRecyclerView() {
        trabajoDisponibleAlumnoAdapter = TrabajoDisponibleAlumnoAdapter(emptyList()) { trabajo ->
            seleccionarTrabajo(trabajo)
        }

        binding.rvTrabajosDisponiblesAlumno.layoutManager = LinearLayoutManager(this)
        binding.rvTrabajosDisponiblesAlumno.adapter = trabajoDisponibleAlumnoAdapter
    }

    private fun configurarEventos() {
        binding.btnVolverAplicarTrabajo.setOnClickListener {
            finish()
        }

        binding.btnAplicarTrabajoSeleccionado.setOnClickListener {
            val trabajo = trabajoSeleccionado

            if (trabajo == null) {
                Toast.makeText(this, "Selecciona un trabajo primero.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val idSesion = sessionManager.getIdUsuario()?.trim().orEmpty()
            if (idSesion.isEmpty()) {
                Toast.makeText(this, "No se encontró la sesión del alumno.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val aplicado = trabajoDisponibleAlumnoDao.aplicarATrabajo(idSesion, trabajo)

            if (aplicado) {
                Toast.makeText(
                    this,
                    "Solicitud enviada correctamente.",
                    Toast.LENGTH_LONG
                ).show()

                setResult(RESULT_OK)
                finish()
            } else {
                Toast.makeText(
                    this,
                    "No fue posible enviar la solicitud. Verifica si ya tienes trabajo o una solicitud pendiente.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun cargarTrabajosDisponibles() {
        val lista = trabajoDisponibleAlumnoDao.obtenerTrabajosDisponibles()
        trabajoDisponibleAlumnoAdapter.actualizarLista(lista)

        if (lista.isEmpty()) {
            binding.tvTrabajoSeleccionadoAplicar.text = "Trabajo seleccionado: no hay trabajos disponibles"
            binding.btnAplicarTrabajoSeleccionado.isEnabled = false
            Toast.makeText(this, "No hay trabajos disponibles en este momento.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun seleccionarTrabajo(trabajo: TrabajoDisponibleAlumnoModel) {
        trabajoSeleccionado = trabajo
        binding.tvTrabajoSeleccionadoAplicar.text = "Trabajo seleccionado: ${trabajo.nombreTrabajo}"
        binding.btnAplicarTrabajoSeleccionado.isEnabled = true
    }
}