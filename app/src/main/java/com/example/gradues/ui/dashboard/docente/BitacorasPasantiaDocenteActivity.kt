// BitacorasPasantiaDocenteActivity.kt
package com.example.gradues.ui.dashboard.docente

import android.os.Bundle
import android.view.LayoutInflater
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.gradues.R
import com.example.gradues.data.dao.PasantiaDocenteDao
import com.example.gradues.data.db.DatabaseHelper
import com.example.gradues.data.model.BitacoraDocenteModel
import com.example.gradues.databinding.ActivityBitacorasPasantiaDocenteBinding
import com.google.android.material.card.MaterialCardView

class BitacorasPasantiaDocenteActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBitacorasPasantiaDocenteBinding
    private lateinit var pasantiaDocenteDao: PasantiaDocenteDao

    private var idTrabajoGraduacion: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBitacorasPasantiaDocenteBinding.inflate(layoutInflater)
        setContentView(binding.root)

        pasantiaDocenteDao = PasantiaDocenteDao(DatabaseHelper(this))

        idTrabajoGraduacion = intent.getStringExtra("ID_TRABAJO_GRADUACION")

        if (idTrabajoGraduacion.isNullOrBlank()) {
            Toast.makeText(this, "Error: ID de trabajo de graduación no encontrado.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        configurarEventos()
        cargarBitacoras(idTrabajoGraduacion!!)
    }

    private fun configurarEventos() {
        binding.btnBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun cargarBitacoras(idTrabajo: String) {
        val bitacoras = pasantiaDocenteDao.obtenerBitacorasPasantia(idTrabajo)
        displayBitacoras(bitacoras)
    }

    private fun displayBitacoras(bitacoras: List<BitacoraDocenteModel>) {
        binding.llBitacorasContainer.removeAllViews()

        if (bitacoras.isEmpty()) {
            val tvNoBitacoras = TextView(this).apply {
                text = "No hay bitácoras registradas para esta pasantía."
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(0, 16.dpToPx(), 0, 0)
                }
                setTextAppearance(androidx.appcompat.R.style.TextAppearance_AppCompat_Medium)
            }
            binding.llBitacorasContainer.addView(tvNoBitacoras)
            return
        }

        bitacoras.forEach { bitacora ->
            val cardView = LayoutInflater.from(this).inflate(R.layout.item_bitacora_docente, binding.llBitacorasContainer, false) as MaterialCardView

            cardView.findViewById<TextView>(R.id.tvBitacoraFecha).text = "Fecha: ${bitacora.fechaBitacora}"
            cardView.findViewById<TextView>(R.id.tvBitacoraDescripcion).text = bitacora.descripcionBitacora
            cardView.findViewById<TextView>(R.id.tvBitacoraEstado).text = "Estado: ${bitacora.estadoBitacora}"

            binding.llBitacorasContainer.addView(cardView)
        }
    }

    // Extension function to convert dp to pixels
    private fun Int.dpToPx(): Int = (this * resources.displayMetrics.density).toInt()
}