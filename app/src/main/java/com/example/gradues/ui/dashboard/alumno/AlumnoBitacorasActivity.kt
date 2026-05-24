package com.example.gradues.ui.dashboard.alumno

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.example.gradues.data.dao.AlumnoPasantiaDao
import com.example.gradues.data.db.DatabaseHelper
import com.example.gradues.data.model.AlumnoBitacoraModel
import com.example.gradues.databinding.ActivityAlumnoBitacorasBinding
import com.example.gradues.utils.SessionManager

class AlumnoBitacorasActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAlumnoBitacorasBinding
    private lateinit var alumnoPasantiaDao: AlumnoPasantiaDao
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAlumnoBitacorasBinding.inflate(layoutInflater)
        setContentView(binding.root)

        alumnoPasantiaDao = AlumnoPasantiaDao(DatabaseHelper(this))
        sessionManager = SessionManager(this)

        configurarEventos()
    }

    override fun onResume() {
        super.onResume()
        cargarBitacoras()
    }

    private fun cargarBitacoras() {
        val idUsuario = sessionManager.getIdUsuario().trim()
        if (idUsuario.isBlank()) {
            Toast.makeText(this, "No se encontro la sesion del alumno.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        val bitacoras = alumnoPasantiaDao.obtenerBitacorasPorAlumno(idUsuario)
        binding.tvResumenBitacorasAlumno.text = "Bitacoras registradas: ${bitacoras.size}/10"

        val container = binding.llBitacorasAlumnoContainer
        val resumen = binding.cardResumenBitacorasAlumno
        val botonAgregar = binding.btnAgregarBitacoraAlumno

        container.removeAllViews()
        container.addView(resumen)

        if (bitacoras.isEmpty()) {
            container.addView(crearTextoVacio())
        } else {
            bitacoras.forEachIndexed { index, bitacora ->
                container.addView(crearCardBitacora(index + 1, bitacora))
            }
        }

        container.addView(botonAgregar)
    }

    private fun crearCardBitacora(numero: Int, bitacora: AlumnoBitacoraModel): CardView {
        val card = CardView(this).apply {
            radius = dp(10).toFloat()
            cardElevation = dp(2).toFloat()
            useCompatPadding = true
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = dp(12)
            }
        }

        val contenido = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(0, 0, 0, dp(10))
        }

        contenido.addView(TextView(this).apply {
            text = "Bitacora $numero"
            setTextColor(Color.WHITE)
            textSize = 20f
            setTypeface(typeface, android.graphics.Typeface.BOLD)
            gravity = android.view.Gravity.CENTER
            setBackgroundColor(Color.parseColor("#B30000"))
            setPadding(0, dp(10), 0, dp(10))
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        })

        val cuerpo = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(12), dp(12), dp(12), 0)
        }

        cuerpo.addView(crearCampo("#${bitacora.idBitacora} ${bitacora.tituloActividad.ifBlank { "Sin titulo" }}"))
        cuerpo.addView(crearCampo(bitacora.fechaActividad.ifBlank { "Sin fecha" }, margenTop = 6))
        cuerpo.addView(crearCampo("${bitacora.totalHorasTrabajadas} horas - ${bitacora.descripcionActividad}", margenTop = 6))
        cuerpo.addView(View(this).apply {
            setBackgroundColor(Color.parseColor("#DADADA"))
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                dp(1)
            ).apply {
                topMargin = dp(12)
            }
        })

        val acciones = LinearLayout(this).apply {
            gravity = android.view.Gravity.CENTER
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = dp(12)
            }
        }

        acciones.addView(crearBotonEstado(bitacora.estadoBitacora))
        acciones.addView(crearBotonAccion("Ver") {
            val observacion = bitacora.observacionBitacora.ifBlank { "Sin observaciones." }
            mostrarMensaje("${bitacora.descripcionActividad}\n$observacion")
        })
        acciones.addView(crearBotonAccion("Editar") {
            mostrarMensaje("Solo se pueden editar bitacoras pendientes desde esta pantalla.")
        })

        cuerpo.addView(acciones)
        contenido.addView(cuerpo)
        card.addView(contenido)
        return card
    }

    private fun crearCampo(textoCampo: String, margenTop: Int = 0): TextView {
        return TextView(this).apply {
            text = textoCampo
            setTextColor(Color.parseColor("#444444"))
            textSize = 16f
            setBackgroundColor(Color.parseColor("#F5F5F5"))
            setPadding(dp(12), dp(12), dp(12), dp(12))
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = dp(margenTop)
            }
        }
    }

    private fun crearBotonEstado(estado: String): Button {
        val color = if (estado.equals("Revisada", ignoreCase = true)) "#6AA86E" else "#D98A2B"
        return Button(this).apply {
            text = estado
            isAllCaps = false
            setTextColor(Color.WHITE)
            setBackgroundColor(Color.parseColor(color))
            layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f).apply {
                marginEnd = dp(6)
            }
        }
    }

    private fun crearBotonAccion(texto: String, accion: () -> Unit): Button {
        return Button(this).apply {
            text = texto
            isAllCaps = false
            setTextColor(Color.parseColor("#222222"))
            setBackgroundColor(Color.parseColor("#D9D9D9"))
            setOnClickListener { accion() }
            layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f).apply {
                marginStart = dp(6)
            }
        }
    }

    private fun crearTextoVacio(): TextView {
        return TextView(this).apply {
            text = "No hay bitacoras registradas para esta pasantia."
            setTextColor(Color.parseColor("#444444"))
            textSize = 16f
            setPadding(dp(12), dp(24), dp(12), dp(12))
        }
    }

    private fun configurarEventos() {
        binding.btnBackBitacorasAlumno.setOnClickListener {
            finish()
        }

        binding.btnNotificacionesBitacorasAlumno.setOnClickListener {
            mostrarMensaje("Notificaciones pendientes")
        }

        binding.btnPerfilBitacorasAlumno.setOnClickListener {
            mostrarMensaje("Perfil pendiente")
        }

        binding.btnAgregarBitacoraAlumno.setOnClickListener {
            startActivity(Intent(this, AlumnoRegistrarBitacoraActivity::class.java))
        }

        binding.itemInicioBitacorasAlumno.setOnClickListener {
            startActivity(Intent(this, DashboardAlumnoActivity::class.java))
            finish()
        }

        binding.itemGrupoBitacorasAlumno.setOnClickListener {
            startActivity(Intent(this, AlumnoPasantiaDetalleActivity::class.java))
            finish()
        }

        binding.itemPerfilBitacorasAlumno.setOnClickListener {
            mostrarMensaje("Perfil pendiente")
        }
    }

    private fun dp(value: Int): Int {
        return (value * resources.displayMetrics.density).toInt()
    }

    private fun mostrarMensaje(mensaje: String) {
        Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show()
    }
}
