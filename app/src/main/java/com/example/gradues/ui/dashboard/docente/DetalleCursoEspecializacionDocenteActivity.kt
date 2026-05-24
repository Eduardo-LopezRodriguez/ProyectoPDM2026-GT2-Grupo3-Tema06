// DetalleCursoEspecializacionDocenteActivity.kt
package com.example.gradues.ui.dashboard.docente

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.gradues.data.dao.CursoEspecializacionDocenteDao
import com.example.gradues.data.db.DatabaseHelper
import com.example.gradues.data.model.CursoEspecializacionDocenteModel
import com.example.gradues.databinding.ActivityDetalleCursoEspecializacionDocenteBinding
import com.example.gradues.utils.SessionManager

class DetalleCursoEspecializacionDocenteActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetalleCursoEspecializacionDocenteBinding
    private lateinit var cursoEspecializacionDocenteDao: CursoEspecializacionDocenteDao
    private lateinit var sessionManager: SessionManager

    private var idGrupoTGE: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetalleCursoEspecializacionDocenteBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)
        cursoEspecializacionDocenteDao = CursoEspecializacionDocenteDao(DatabaseHelper(this))

        idGrupoTGE = intent.getStringExtra("ID_GRUPO_TGE")

        if (idGrupoTGE.isNullOrBlank()) {
            Toast.makeText(this, "Error: ID de curso no encontrado.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        configurarEventos()
        cargarDetalleCurso(idGrupoTGE!!)
    }

    private fun configurarEventos() {
        binding.btnBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        binding.btnVerSubgrupos.setOnClickListener {
            val intent = Intent(this, DetalleSubgrupoEspecializacionDocenteActivity::class.java).apply {
                putExtra("ID_GRUPO_TGE", idGrupoTGE)
                putExtra("ID_DOCENTE", sessionManager.getIdUsuario()) // Pass teacher ID for filtering
            }
            startActivity(intent)
        }
    }

    private fun cargarDetalleCurso(idGrupo: String) {
        val curso = cursoEspecializacionDocenteDao.obtenerDetalleCursoEspecializacion(idGrupo)
        if (curso != null) {
            pintarDetalleCurso(curso)
        } else {
            Toast.makeText(this, "No se encontró el detalle del curso de especialización.", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun pintarDetalleCurso(model: CursoEspecializacionDocenteModel) {
        binding.tvDetalleNombreCurso.text = model.nombreCurso
        binding.tvDetalleCicloCurso.text = "Ciclo: ${model.ciclo}"
        binding.tvDetalleTotalSubgrupos.text = "Total de Subgrupos: ${model.totalSubgrupos}"
        binding.tvDetalleTotalEstudiantesCurso.text = "Total de Estudiantes: ${model.totalEstudiantes}"
    }
}