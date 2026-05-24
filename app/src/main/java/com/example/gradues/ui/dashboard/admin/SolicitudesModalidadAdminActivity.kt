package com.example.gradues.ui.dashboard.admin

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.gradues.data.dao.SolicitudModalidadAdminDao
import com.example.gradues.data.db.DatabaseHelper
import com.example.gradues.data.model.SolicitudModalidadAdminModel
import com.example.gradues.databinding.ActivitySolicitudesModalidadAdminBinding
import android.content.Intent

class SolicitudesModalidadAdminActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySolicitudesModalidadAdminBinding
    private lateinit var solicitudDao: SolicitudModalidadAdminDao

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySolicitudesModalidadAdminBinding.inflate(layoutInflater)
        setContentView(binding.root)

        solicitudDao = SolicitudModalidadAdminDao(DatabaseHelper(this))

        configurarEventos()
        cargarSolicitudes()
    }

    override fun onResume() {
        super.onResume()
        cargarSolicitudes()
    }

    private fun configurarEventos() {
        binding.btnActualizar.setOnClickListener {
            cargarSolicitudes()
        }

        binding.btnVolver.setOnClickListener {
            finish()
        }
    }

    private fun cargarSolicitudes() {
        val solicitudes = solicitudDao.listarSolicitudesAgrupadas()

        binding.contenedorSolicitudes.removeAllViews()

        if (solicitudes.isEmpty()) {
            binding.tvSinSolicitudes.visibility = View.VISIBLE
            return
        }

        binding.tvSinSolicitudes.visibility = View.GONE

        solicitudes.forEach { solicitud ->
            binding.contenedorSolicitudes.addView(crearTarjetaSolicitud(solicitud))
        }
    }

    private fun crearTarjetaSolicitud(solicitud: SolicitudModalidadAdminModel): View {
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

        card.addView(crearTextoTitulo("${solicitud.codigoAgrupacionSolicitud} - ${solicitud.modalidad}"))
        card.addView(crearTextoNormal("Estado: ${solicitud.estadoSolicitud}"))
        card.addView(crearTextoNormal("Fecha: ${solicitud.fechaSolicitud}"))
        card.addView(crearTextoNormal("Trabajo propuesto: ${solicitud.nombreTrabajoPropuesto}"))

        if (!solicitud.cursoSolicitado.isNullOrBlank()) {
            card.addView(crearTextoNormal("Curso solicitado: ${solicitud.cursoSolicitado}"))
        }

        if (!solicitud.empresaSolicitada.isNullOrBlank()) {
            card.addView(crearTextoNormal("Empresa solicitada: ${solicitud.empresaSolicitada}"))
        }

        card.addView(crearTextoNormal("Total solicitantes: ${solicitud.totalSolicitantes}"))
        card.addView(crearTextoNormal("Solicitantes:\n${solicitud.solicitantes}"))

        if (!solicitud.observacionSolicitud.isNullOrBlank()) {
            card.addView(crearTextoNormal("Observación: ${solicitud.observacionSolicitud}"))
        }

        val btnDetalle = Button(this).apply {
            text = "Ver detalle"
            isAllCaps = false
            setTextColor(0xFFFFFFFF.toInt())
            setBackgroundColor(0xFFB71C1C.toInt())

            setOnClickListener {
                val intent = Intent(
                    this@SolicitudesModalidadAdminActivity,
                    DetalleSolicitudAdminActivity::class.java
                )

                intent.putExtra("idReferenciaSolicitud", solicitud.idReferenciaSolicitud)

                startActivity(intent)
            }
        }

        card.addView(btnDetalle)

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