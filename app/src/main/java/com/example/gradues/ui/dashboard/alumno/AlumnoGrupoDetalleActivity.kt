package com.example.gradues.ui.dashboard.alumno

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.gradues.data.dao.AlumnoGrupoDetalleDao
import com.example.gradues.data.db.DatabaseHelper
import com.example.gradues.data.model.AlumnoGrupoDetalleModel
import com.example.gradues.databinding.ActivityAlumnoGrupoDetalleBinding
import com.example.gradues.utils.SessionManager

class AlumnoGrupoDetalleActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAlumnoGrupoDetalleBinding
    private lateinit var alumnoGrupoDetalleDao: AlumnoGrupoDetalleDao
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAlumnoGrupoDetalleBinding.inflate(layoutInflater)
        setContentView(binding.root)

        alumnoGrupoDetalleDao = AlumnoGrupoDetalleDao(DatabaseHelper(this))
        sessionManager = SessionManager(this)

        cargarDatosGrupo()
        configurarEventos()
    }

    override fun onResume() {
        super.onResume()
        cargarDatosGrupo()
    }

    private fun cargarDatosGrupo() {
        val idSesion = sessionManager.getIdUsuario().trim()

        if (idSesion.isEmpty()) {
            mostrarMensaje("No se encontro la sesion del alumno")
            finish()
            return
        }

        val grupo = alumnoGrupoDetalleDao.obtenerGrupoInvestigacionAlumno(idSesion)

        if (grupo == null) {
            mostrarMensaje("No se encontro informacion del grupo")
            finish()
            return
        }

        pintarGrupo(grupo)
    }

    private fun pintarGrupo(grupo: AlumnoGrupoDetalleModel) {
        binding.tvNombreGrupoAlumno.text = "${grupo.nombreGrupo} ${grupo.codigoGrupo}"
        binding.tvTemaGrupoAlumno.text = grupo.temaTrabajo
        binding.tvDirectorGrupoAlumno.text = "Director: ${grupo.nombreDocente}"

        val integrantes = grupo.integrantesTexto
            .split("\n")
            .filter { it.isNotBlank() }

        pintarIntegrante(0, integrantes)
        pintarIntegrante(1, integrantes)
        pintarIntegrante(2, integrantes)

        binding.tvResumenJuradosGrupoAlumno.text = grupo.resumenPropuestas
        binding.tvDescripcionTesisGrupoAlumno.text = grupo.descripcionTesis
        binding.tvEstadoTesisGrupoAlumno.text = "Estado: ${grupo.estadoTesis}"
        binding.tvUltimaVersionTesisGrupoAlumno.text = "Ultima version: ${grupo.ultimaVersionTesis}"

        binding.tvNotaEtapa1GrupoAlumno.text = "Etapa 1: ${grupo.notaEtapa1}"
        binding.tvNotaEtapa2GrupoAlumno.text = "Etapa 2: ${grupo.notaEtapa2}"
        binding.tvNotaEtapa3GrupoAlumno.text = "Etapa 3: ${grupo.notaEtapa3}"
        binding.tvNotaEtapa4GrupoAlumno.text = "Etapa 4: ${grupo.notaEtapa4}"

        val promedio = calcularPromedio(grupo)
        binding.tvPromedioAlumnoGrupo.text = "Mi promedio: $promedio"
        binding.tvPromedioGrupalGrupo.text = "Promedio grupal: $promedio"
    }

    private fun pintarIntegrante(posicion: Int, integrantes: List<String>) {
        val texto = if (posicion < integrantes.size) integrantes[posicion] else "Sin integrante"
        val nombre: String
        val carnet: String

        if (texto.contains("(") && texto.contains(")")) {
            nombre = texto.substringBefore(" (").trim()
            carnet = texto.substringAfter("(").substringBefore(")").trim()
        } else {
            nombre = texto
            carnet = "--"
        }

        when (posicion) {
            0 -> {
                binding.tvIntegrante1Nombre.text = nombre
                binding.tvIntegrante1Carnet.text = carnet
            }
            1 -> {
                binding.tvIntegrante2Nombre.text = nombre
                binding.tvIntegrante2Carnet.text = carnet
            }
            2 -> {
                binding.tvIntegrante3Nombre.text = nombre
                binding.tvIntegrante3Carnet.text = carnet
            }
        }
    }

    private fun calcularPromedio(grupo: AlumnoGrupoDetalleModel): String {
        val notas = listOf(
            grupo.notaEtapa1,
            grupo.notaEtapa2,
            grupo.notaEtapa3,
            grupo.notaEtapa4
        ).mapNotNull { it.toDoubleOrNull() }

        if (notas.isEmpty()) return "--"

        return String.format("%.2f", notas.average())
    }

    private fun configurarEventos() {
        binding.btnBackGrupoAlumno.setOnClickListener {
            finish()
        }

        binding.btnInfoIntegrante1.setOnClickListener {
            mostrarMensaje("Detalle de integrante 1 pendiente")
        }

        binding.btnInfoIntegrante2.setOnClickListener {
            mostrarMensaje("Detalle de integrante 2 pendiente")
        }

        binding.btnInfoIntegrante3.setOnClickListener {
            mostrarMensaje("Detalle de integrante 3 pendiente")
        }

        binding.btnVerPropuestasGrupoAlumno.setOnClickListener {
            startActivity(Intent(this, AlumnoPropuestasDetalleActivity::class.java))
        }

        binding.btnSubirTesisGrupoAlumno.setOnClickListener {
            startActivity(Intent(this, AlumnoTesisActivity::class.java))
        }

        binding.btnHistorialTesisGrupoAlumno.setOnClickListener {
            startActivity(Intent(this, AlumnoTesisActivity::class.java))
        }

        binding.itemInicioGrupoAlumno.setOnClickListener {
            startActivity(Intent(this, DashboardAlumnoActivity::class.java))
            finish()
        }

        binding.itemGrupoDetalleAlumno.setOnClickListener {
            mostrarMensaje("Ya estas en Mi grupo")
        }

        binding.itemPerfilGrupoAlumno.setOnClickListener {
            mostrarMensaje("Perfil pendiente")
        }
    }

    private fun mostrarMensaje(mensaje: String) {
        Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show()
    }
}
