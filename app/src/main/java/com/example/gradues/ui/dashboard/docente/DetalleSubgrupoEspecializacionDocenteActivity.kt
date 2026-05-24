// DetalleSubgrupoEspecializacionDocenteActivity.kt
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
import com.example.gradues.data.model.SubgrupoEspecializacionDocenteModel
import com.example.gradues.databinding.ActivityDetalleSubgrupoEspecializacionDocenteBinding
import com.example.gradues.utils.SessionManager
import com.google.android.material.card.MaterialCardView

class DetalleSubgrupoEspecializacionDocenteActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetalleSubgrupoEspecializacionDocenteBinding
    private lateinit var cursoEspecializacionDocenteDao: CursoEspecializacionDocenteDao
    private lateinit var sessionManager: SessionManager

    private var idGrupoTGE: String? = null
    private var idDocente: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetalleSubgrupoEspecializacionDocenteBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)
        cursoEspecializacionDocenteDao = CursoEspecializacionDocenteDao(DatabaseHelper(this))

        idGrupoTGE = intent.getStringExtra("ID_GRUPO_TGE")
        idDocente = intent.getStringExtra("ID_DOCENTE")

        if (idGrupoTGE.isNullOrBlank() || idDocente.isNullOrBlank()) {
            Toast.makeText(this, "Error: ID de curso o docente no encontrado.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        configurarEventos()
        cargarSubgrupos(idGrupoTGE!!, idDocente!!)
    }

    private fun configurarEventos() {
        binding.btnBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun cargarSubgrupos(idGrupo: String, idDocente: String) {
        val subgrupos = cursoEspecializacionDocenteDao.obtenerSubgruposPorCurso(idGrupo, idDocente)
        displaySubgrupos(subgrupos)
    }

    private fun displaySubgrupos(subgrupos: List<SubgrupoEspecializacionDocenteModel>) {
        binding.llSubgruposContainer.removeAllViews() // Clear previous views

        if (subgrupos.isEmpty()) {
            val tvNoSubgrupos = TextView(this).apply {
                text = "No hay subgrupos asignados a este curso."
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(0, 16.dpToPx(), 0, 0)
                }
                setTextAppearance(androidx.appcompat.R.style.TextAppearance_AppCompat_Medium)
            }
            binding.llSubgruposContainer.addView(tvNoSubgrupos)
            return
        }

        subgrupos.forEach { subgrupo ->
            val cardView = LayoutInflater.from(this).inflate(R.layout.item_subgrupo_especializacion_docente, binding.llSubgruposContainer, false) as MaterialCardView

            cardView.findViewById<TextView>(R.id.tvNombreSubgrupo).text = subgrupo.nombreSubgrupo
            cardView.findViewById<TextView>(R.id.tvTemaAsignado).text = "Tema Asignado: ${subgrupo.temaAsignado}"
            cardView.findViewById<TextView>(R.id.tvEstadoSubgrupo).text = "Estado: ${subgrupo.estadoSubgrupo}"
            cardView.findViewById<TextView>(R.id.tvTotalEstudiantesSubgrupo).text = "Estudiantes: ${subgrupo.totalEstudiantes}"
            cardView.findViewById<TextView>(R.id.tvPromedioGeneralSubgrupo).text = "Promedio General: ${subgrupo.promedioGeneralSubgrupo}"

            cardView.setOnClickListener {
                val intent = Intent(this, NotasSubgrupoDocenteActivity::class.java).apply {
                    putExtra("ID_TRABAJO_GRADUACION", subgrupo.idTrabajoGraduacion)
                    putExtra("NOMBRE_SUBGRUPO", subgrupo.nombreSubgrupo)
                }
                startActivity(intent)
            }
            binding.llSubgruposContainer.addView(cardView)
        }
    }

    // Extension function to convert dp to pixels
    private fun Int.dpToPx(): Int = (this * resources.displayMetrics.density).toInt()
}