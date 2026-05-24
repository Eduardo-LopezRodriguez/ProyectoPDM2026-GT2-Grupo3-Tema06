// CursosEspecializacionDocenteActivity.kt
package com.example.gradues.ui.dashboard.docente

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.gradues.R
import com.example.gradues.data.dao.CursoEspecializacionDocenteDao
import com.example.gradues.data.db.DatabaseHelper
import com.example.gradues.data.model.CursoEspecializacionDocenteModel
import com.example.gradues.databinding.ActivityCursosEspecializacionDocenteBinding
import com.example.gradues.utils.SessionManager
import com.google.android.material.card.MaterialCardView

class CursosEspecializacionDocenteActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCursosEspecializacionDocenteBinding
    private lateinit var sessionManager: SessionManager
    private lateinit var cursoEspecializacionDocenteDao: CursoEspecializacionDocenteDao

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCursosEspecializacionDocenteBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)
        cursoEspecializacionDocenteDao = CursoEspecializacionDocenteDao(DatabaseHelper(this))

        cargarCursosEspecializacion()
    }

    private fun cargarCursosEspecializacion() {
        val idDocente = sessionManager.getIdUsuario()
        if (idDocente.isBlank()) {
            Toast.makeText(this, "Sesión de docente no válida.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        val cursos = cursoEspecializacionDocenteDao.obtenerCursosEspecializacionAsignados(idDocente)
        displayCursosEspecializacion(cursos)
    }

    private fun displayCursosEspecializacion(cursos: List<CursoEspecializacionDocenteModel>) {
        binding.llCursosEspecializacionContainer.removeAllViews() // Clear previous views

        if (cursos.isEmpty()) {
            val tvNoCursos = TextView(this).apply {
                text = "No hay cursos de especialización asignados."
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(0, 16.dpToPx(), 0, 0)
                }
                setTextAppearance(androidx.appcompat.R.style.TextAppearance_AppCompat_Medium)
            }
            binding.llCursosEspecializacionContainer.addView(tvNoCursos)
            return
        }

        cursos.forEach { curso ->
            val cardView = LayoutInflater.from(this).inflate(R.layout.item_curso_especializacion_docente, binding.llCursosEspecializacionContainer, false) as MaterialCardView

            cardView.findViewById<TextView>(R.id.tvNombreCurso).text = curso.nombreCurso
            cardView.findViewById<TextView>(R.id.tvCicloCurso).text = "Ciclo: ${curso.ciclo}"
            cardView.findViewById<TextView>(R.id.tvTotalSubgrupos).text = "Subgrupos: ${curso.totalSubgrupos}"
            cardView.findViewById<TextView>(R.id.tvTotalEstudiantesCurso).text = "Estudiantes: ${curso.totalEstudiantes}"

            cardView.setOnClickListener {
                val intent = Intent(this, DetalleCursoEspecializacionDocenteActivity::class.java).apply {
                    putExtra("ID_GRUPO_TGE", curso.idGrupoTGE)
                }
                startActivity(intent)
            }
            binding.llCursosEspecializacionContainer.addView(cardView)
        }
    }

    // Extension function to convert dp to pixels
    private fun Int.dpToPx(): Int = (this * resources.displayMetrics.density).toInt()
}