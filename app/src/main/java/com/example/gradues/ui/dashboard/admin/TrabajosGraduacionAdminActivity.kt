package com.example.gradues.ui.dashboard.admin

import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.gradues.data.dao.TrabajoGraduacionAdminDao
import com.example.gradues.data.db.DatabaseHelper
import com.example.gradues.data.model.TrabajoGraduacionAdminModel
import com.example.gradues.databinding.ActivityTrabajosGraduacionAdminBinding

class TrabajosGraduacionAdminActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTrabajosGraduacionAdminBinding
    private lateinit var trabajoDao: TrabajoGraduacionAdminDao

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityTrabajosGraduacionAdminBinding.inflate(layoutInflater)
        setContentView(binding.root)

        trabajoDao = TrabajoGraduacionAdminDao(DatabaseHelper(this))

        configurarEventos()
        cargarTrabajos()
    }

    override fun onResume() {
        super.onResume()

        if (::trabajoDao.isInitialized) {
            cargarTrabajos()
        }
    }

    private fun configurarEventos() {
        binding.btnActualizar.setOnClickListener {
            cargarTrabajos()
        }

        binding.btnVolver.setOnClickListener {
            finish()
        }
    }

    private fun cargarTrabajos() {
        val trabajos = trabajoDao.listarTrabajosGraduacion()

        binding.contenedorTrabajos.removeAllViews()

        if (trabajos.isEmpty()) {
            binding.tvSinTrabajos.visibility = View.VISIBLE
            return
        }

        binding.tvSinTrabajos.visibility = View.GONE

        trabajos.forEach { trabajo ->
            binding.contenedorTrabajos.addView(crearTarjetaTrabajo(trabajo))
        }
    }

    private fun crearTarjetaTrabajo(trabajo: TrabajoGraduacionAdminModel): View {
        val card = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(0xFFFFFFFF.toInt())
            elevation = 4f
            setPadding(20, 18, 20, 18)

            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            params.setMargins(0, 0, 0, 18)
            layoutParams = params
        }

        card.addView(crearTextoTitulo(trabajo.nombreTrabajo))
        card.addView(crearTextoNormal("Modalidad: ${trabajo.modalidad}"))
        card.addView(crearTextoNormal("Estado: ${trabajo.estadoTrabajo}"))
        card.addView(crearTextoNormal("Ciclo académico: ${trabajo.cicloAcademico}"))
        card.addView(crearTextoNormal("Docente responsable: ${trabajo.docenteResponsable}"))
        card.addView(crearTextoNormal("Fecha inicio: ${trabajo.fechaInicioTrabajo}"))

        if (!trabajo.fechaFinalTrabajo.isNullOrBlank()) {
            card.addView(crearTextoNormal("Fecha final: ${trabajo.fechaFinalTrabajo}"))
        }

        card.addView(crearTextoNormal("Detalle:\n${trabajo.detalleModalidad}"))
        card.addView(crearTextoNormal("Total integrantes: ${trabajo.totalIntegrantes}"))
        card.addView(crearTextoNormal("Integrantes:\n${trabajo.integrantes}"))

        return card
    }

    private fun crearTextoTitulo(texto: String): TextView {
        return TextView(this).apply {
            text = texto
            textSize = 18f
            setTextColor(0xFF212121.toInt())
            setTypeface(null, android.graphics.Typeface.BOLD)
            setPadding(0, 0, 0, 8)
        }
    }

    private fun crearTextoNormal(texto: String): TextView {
        return TextView(this).apply {
            text = texto
            textSize = 14f
            setTextColor(0xFF333333.toInt())
            setPadding(0, 4, 0, 4)
        }
    }
}