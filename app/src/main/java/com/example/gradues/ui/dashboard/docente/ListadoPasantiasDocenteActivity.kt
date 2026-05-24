// ListadoPasantiasDocenteActivity.kt
package com.example.gradues.ui.dashboard.docente

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.gradues.R
import com.example.gradues.data.dao.PasantiaDocenteDao
import com.example.gradues.data.db.DatabaseHelper
import com.example.gradues.data.model.PasantiaDocenteModel
import com.example.gradues.databinding.ActivityListadoPasantiasDocenteBinding
import com.example.gradues.utils.SessionManager
import com.google.android.material.card.MaterialCardView

class ListadoPasantiasDocenteActivity : AppCompatActivity() {

    private lateinit var binding: ActivityListadoPasantiasDocenteBinding
    private lateinit var sessionManager: SessionManager
    private lateinit var pasantiaDocenteDao: PasantiaDocenteDao

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityListadoPasantiasDocenteBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)
        pasantiaDocenteDao = PasantiaDocenteDao(DatabaseHelper(this))

        cargarPasantias()
    }

    private fun cargarPasantias() {
        val idDocente = sessionManager.getIdUsuario()
        if (idDocente.isBlank()) {
            Toast.makeText(this, "Sesión de docente no válida.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        val pasantias = pasantiaDocenteDao.obtenerPasantiasAsignadas(idDocente)
        displayPasantias(pasantias)
    }

    private fun displayPasantias(pasantias: List<PasantiaDocenteModel>) {
        binding.llPasantiasContainer.removeAllViews() // Clear previous views

        if (pasantias.isEmpty()) {
            val tvNoPasantias = TextView(this).apply {
                text = "No hay pasantías asignadas."
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(0, 16.dpToPx(), 0, 0)
                }
                setTextAppearance(androidx.appcompat.R.style.TextAppearance_AppCompat_Medium)
            }
            binding.llPasantiasContainer.addView(tvNoPasantias)
            return
        }

        pasantias.forEach { pasantia ->
            val cardView = LayoutInflater.from(this).inflate(R.layout.item_pasantia_docente, binding.llPasantiasContainer, false) as MaterialCardView

            cardView.findViewById<TextView>(R.id.tvTituloPasantia).text = pasantia.nombreTrabajo
            cardView.findViewById<TextView>(R.id.tvEstudiantePasantia).text = "${pasantia.nombreEstudiante} (${pasantia.carnetEstudiante})"
            cardView.findViewById<TextView>(R.id.tvEmpresaPasantia).text = pasantia.nombreEmpresa
            cardView.findViewById<TextView>(R.id.tvEstadoPasantia).text = "Estado: ${pasantia.estadoPasantia}"
            cardView.findViewById<TextView>(R.id.tvTotalBitacoras).text = "Bitácoras: ${pasantia.totalBitacoras}"
            cardView.findViewById<TextView>(R.id.tvEstadoMemoria).text = "Memoria: ${pasantia.estadoMemoria}"

            cardView.setOnClickListener {
                val intent = Intent(this, DetallePasantiaDocenteActivity::class.java).apply {
                    putExtra("ID_PROYECTO_PASANTIA", pasantia.idProyectoPasantia)
                    putExtra("ID_TRABAJO_GRADUACION", pasantia.idTrabajoGraduacion)
                }
                startActivity(intent)
            }
            binding.llPasantiasContainer.addView(cardView)
        }
    }

    // Extension function to convert dp to pixels
    private fun Int.dpToPx(): Int = (this * resources.displayMetrics.density).toInt()
}