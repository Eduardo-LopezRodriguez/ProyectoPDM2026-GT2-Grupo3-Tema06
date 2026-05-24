package com.example.gradues.ui.dashboard.docente

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.setMargins
import com.example.gradues.R
import com.example.gradues.data.dao.PasantiaDocenteDao
import com.example.gradues.data.db.DatabaseHelper
import com.example.gradues.data.model.BitacoraPendienteDocenteModel
import com.example.gradues.databinding.ActivityBitacorasPendientesDocenteBinding
import com.example.gradues.utils.SessionManager
import com.google.android.material.card.MaterialCardView

class BitacorasPendientesDocenteActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBitacorasPendientesDocenteBinding
    private lateinit var sessionManager: SessionManager
    private lateinit var pasantiaDocenteDao: PasantiaDocenteDao

    companion object {
        private const val TAG = "BitacorasPendientesDocenteActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBitacorasPendientesDocenteBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)
        pasantiaDocenteDao = PasantiaDocenteDao(DatabaseHelper(this))

        configurarEventos()
    }

    override fun onResume() {
        super.onResume()
        cargarBitacorasPendientes()
    }

    private fun configurarEventos() {
        binding.toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun cargarBitacorasPendientes() {
        val idDocente = sessionManager.getIdUsuario()
        if (idDocente.isBlank()) {
            Toast.makeText(this, "Sesión de docente no válida.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        Log.d(TAG, "Cargando bitácoras pendientes para idDocente: $idDocente")
        val bitacoras = pasantiaDocenteDao.obtenerBitacorasPendientes(idDocente)
        Log.d(TAG, "Bitácoras pendientes encontradas: ${bitacoras.size}")
        displayBitacorasPendientes(bitacoras)
    }

    private fun displayBitacorasPendientes(bitacoras: List<BitacoraPendienteDocenteModel>) {
        binding.llBitacorasPendientesContainer.removeAllViews() // Clear previous views

        if (bitacoras.isEmpty()) {
            val tvNoBitacoras = TextView(this).apply {
                text = "No hay bitácoras pendientes de revisión."
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(0, 16.dpToPx(), 0, 0)
                }
                setTextAppearance(androidx.appcompat.R.style.TextAppearance_AppCompat_Medium)
            }
            binding.llBitacorasPendientesContainer.addView(tvNoBitacoras)
            return
        }

        bitacoras.forEach { bitacora ->
            val cardView = LayoutInflater.from(this).inflate(R.layout.item_bitacora_pendiente_docente, binding.llBitacorasPendientesContainer, false) as MaterialCardView

            cardView.findViewById<TextView>(R.id.tvTituloActividad).text = bitacora.tituloActividad
            cardView.findViewById<TextView>(R.id.tvEstudianteBitacora).text = "Estudiante: ${bitacora.nombreEstudiante} (${bitacora.carnetEstudiante})"
            cardView.findViewById<TextView>(R.id.tvTrabajoEmpresa).text = "Trabajo: ${bitacora.nombreTrabajo} (${bitacora.nombreEmpresa})"
            cardView.findViewById<TextView>(R.id.tvEstadoFechaBitacora).text = "Estado: ${bitacora.estadoBitacora} | Fecha: ${bitacora.fechaActividad}"

            cardView.setOnClickListener {
                val intent = Intent(this, DetalleBitacoraDocenteActivity::class.java).apply {
                    putExtra("ID_BITACORA", bitacora.idBitacora)
                }
                startActivity(intent)
            }
            binding.llBitacorasPendientesContainer.addView(cardView)
        }
    }

    // Extension function to convert dp to pixels
    private fun Int.dpToPx(): Int = (this * resources.displayMetrics.density).toInt()
}