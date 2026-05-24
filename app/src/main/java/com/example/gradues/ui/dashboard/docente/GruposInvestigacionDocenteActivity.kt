// GruposInvestigacionDocenteActivity.kt
package com.example.gradues.ui.dashboard.docente

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.gradues.R
import com.example.gradues.data.dao.GrupoInvestigacionDocenteDao
import com.example.gradues.data.db.DatabaseHelper
import com.example.gradues.data.model.GrupoInvestigacionDocenteModel
import com.example.gradues.databinding.ActivityGruposInvestigacionDocenteBinding
import com.example.gradues.utils.SessionManager
import com.google.android.material.card.MaterialCardView

class GruposInvestigacionDocenteActivity : AppCompatActivity() {

    private lateinit var binding: ActivityGruposInvestigacionDocenteBinding
    private lateinit var sessionManager: SessionManager
    private lateinit var grupoInvestigacionDocenteDao: GrupoInvestigacionDocenteDao

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGruposInvestigacionDocenteBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)
        grupoInvestigacionDocenteDao = GrupoInvestigacionDocenteDao(DatabaseHelper(this))

        configurarEventos()
        cargarGruposInvestigacion()
    }

    private fun configurarEventos() {
        binding.btnBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun cargarGruposInvestigacion() {
        val idDocente = sessionManager.getIdUsuario()
        if (idDocente.isBlank()) {
            Toast.makeText(this, "Sesión de docente no válida.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        val grupos = grupoInvestigacionDocenteDao.obtenerGruposInvestigacionAsignados(idDocente)
        displayGruposInvestigacion(grupos)
    }

    private fun displayGruposInvestigacion(grupos: List<GrupoInvestigacionDocenteModel>) {
        binding.llGruposInvestigacionContainer.removeAllViews() // Clear previous views

        if (grupos.isEmpty()) {
            val tvNoGrupos = TextView(this).apply {
                text = "No hay grupos de investigación asignados."
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(0, 16.dpToPx(), 0, 0)
                }
                setTextAppearance(androidx.appcompat.R.style.TextAppearance_AppCompat_Medium)
            }
            binding.llGruposInvestigacionContainer.addView(tvNoGrupos)
            return
        }

        grupos.forEach { grupo ->
            val cardView = LayoutInflater.from(this).inflate(R.layout.item_grupo_investigacion_docente, binding.llGruposInvestigacionContainer, false) as MaterialCardView

            cardView.findViewById<TextView>(R.id.tvCodigoGrupo).text = grupo.codigoGrupoTGI
            cardView.findViewById<TextView>(R.id.tvNombreTrabajoInvestigacion).text = grupo.nombreTrabajo
            cardView.findViewById<TextView>(R.id.tvEstadoGrupoInvestigacion).text = "Estado: ${grupo.estadoGrupo}"
            cardView.findViewById<TextView>(R.id.tvTotalEstudiantesInvestigacion).text = "Estudiantes: ${grupo.totalEstudiantes}"
            cardView.findViewById<TextView>(R.id.tvTotalPropuestasInvestigacion).text = "Propuestas: ${grupo.totalPropuestas}"
            cardView.findViewById<TextView>(R.id.tvTotalDocumentosInvestigacion).text = "Documentos: ${grupo.totalDocumentos}"

            cardView.setOnClickListener {
                val intent = Intent(this, DetalleGrupoInvestigacionDocenteActivity::class.java).apply {
                    putExtra("ID_GRUPO_TGI", grupo.idGrupoTGI)
                    putExtra("ID_TRABAJO_GRADUACION", grupo.idTrabajoGraduacion)
                }
                startActivity(intent)
            }
            binding.llGruposInvestigacionContainer.addView(cardView)
        }
    }

    // Extension function to convert dp to pixels
    private fun Int.dpToPx(): Int = (this * resources.displayMetrics.density).toInt()
}