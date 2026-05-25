package com.example.gradues.ui.dashboard.admin

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.gradues.data.dao.GestionCursosAdminDao
import com.example.gradues.data.db.DatabaseHelper
import com.example.gradues.databinding.ActivityCrearSubgrupoAdminBinding
import com.example.gradues.utils.SessionManager
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CrearSubgrupoAdminActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_ID_GRUPO     = "extra_id_grupo"
        const val EXTRA_NOMBRE_CURSO = "extra_nombre_curso"
        const val EXTRA_CICLO        = "extra_ciclo"
    }

    private lateinit var binding: ActivityCrearSubgrupoAdminBinding
    private lateinit var dao: GestionCursosAdminDao
    private lateinit var sessionManager: SessionManager

    private var idGrupoTGE  = 0
    private var ciclo       = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityCrearSubgrupoAdminBinding.inflate(layoutInflater)
        setContentView(binding.root)

        idGrupoTGE = intent.getIntExtra(EXTRA_ID_GRUPO, 0)
        ciclo      = intent.getStringExtra(EXTRA_CICLO) ?: ""
        val nombreCurso = intent.getStringExtra(EXTRA_NOMBRE_CURSO) ?: ""

        dao = GestionCursosAdminDao(DatabaseHelper(this))
        sessionManager = SessionManager(this)

        binding.tvCursoSubgrupo.text = "Curso: $nombreCurso"

        configurarEventos()
    }

    private fun configurarEventos() {
        binding.btnGuardarSubgrupo.setOnClickListener {
            guardarSubgrupo()
        }

        binding.btnCancelarSubgrupo.setOnClickListener {
            finish()
        }
    }

    private fun guardarSubgrupo() {
        val nombre = binding.etNombreSubgrupo.text.toString().trim()
        val tema   = binding.etTemaSubgrupo.text.toString().trim()

        if (nombre.isBlank()) {
            binding.etNombreSubgrupo.error = "El nombre del subgrupo es obligatorio"
            binding.etNombreSubgrupo.requestFocus()
            return
        }

        val fechaHoy = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

        val exitoso = dao.crearSubgrupo(
            idGrupoTGE     = idGrupoTGE,
            nombreSubgrupo = nombre,
            temaAsignado   = tema.ifBlank { null },
            cicloAcademico = ciclo,
            fechaCreacion  = fechaHoy
        )

        if (exitoso) {
            Toast.makeText(this, "Subgrupo creado exitosamente", Toast.LENGTH_SHORT).show()
            finish()
        } else {
            Toast.makeText(this, "Error al crear el subgrupo. Intente de nuevo.", Toast.LENGTH_LONG).show()
        }
    }
}