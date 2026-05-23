package com.example.gradues.ui.dashboard.alumno

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.gradues.data.dao.TrabajoDisponibleAlumnoDao
import com.example.gradues.data.db.DatabaseHelper
import com.example.gradues.data.model.TrabajoDisponibleAlumnoModel
import com.example.gradues.databinding.ActivityAplicarTrabajoAlumnoBinding

class AplicarTrabajoAlumnoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAplicarTrabajoAlumnoBinding
    private lateinit var trabajoDisponibleAlumnoDao: TrabajoDisponibleAlumnoDao
    private lateinit var trabajoDisponibleAlumnoAdapter: TrabajoDisponibleAlumnoAdapter

    private var trabajoSeleccionado: TrabajoDisponibleAlumnoModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
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

            Toast.makeText(
                this,
                "Solicitud lista para el trabajo: ${trabajo.nombreTrabajo}",
                Toast.LENGTH_LONG
            ).show()
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