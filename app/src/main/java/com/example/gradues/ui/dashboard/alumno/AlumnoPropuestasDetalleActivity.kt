package com.example.gradues.ui.dashboard.alumno

import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.example.gradues.data.dao.AlumnoGrupoDetalleDao
import com.example.gradues.data.db.DatabaseHelper
import com.example.gradues.data.model.AlumnoPropuestaModel
import com.example.gradues.databinding.ActivityAlumnoPropuestasDetalleBinding
import com.example.gradues.utils.SessionManager

class AlumnoPropuestasDetalleActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAlumnoPropuestasDetalleBinding
    private lateinit var alumnoGrupoDetalleDao: AlumnoGrupoDetalleDao
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAlumnoPropuestasDetalleBinding.inflate(layoutInflater)
        setContentView(binding.root)

        alumnoGrupoDetalleDao = AlumnoGrupoDetalleDao(DatabaseHelper(this))
        sessionManager = SessionManager(this)

        configurarEventos()
    }

    override fun onResume() {
        super.onResume()
        cargarPropuestas()
    }

    private fun cargarPropuestas() {
        val idSesion = sessionManager.getIdUsuario().trim()
        if (idSesion.isBlank()) {
            mostrarMensaje("No se encontro la sesion del alumno.")
            finish()
            return
        }

        val grupo = alumnoGrupoDetalleDao.obtenerGrupoInvestigacionAlumno(idSesion)
        if (grupo == null) {
            mostrarMensaje("No se encontro informacion del grupo.")
            finish()
            return
        }

        val propuestas = alumnoGrupoDetalleDao.obtenerPropuestasAlumno(idSesion)
        binding.tvNombreGrupoPropuestasDetalleAlumno.text = "${grupo.nombreGrupo} ${grupo.codigoGrupo}"
        binding.tvTemaPropuestasDetalleAlumno.text = grupo.temaTrabajo
        binding.tvResumenPropuestasDetalleAlumno.text = "${propuestas.size} propuesta(s) registradas"

        val container = binding.llPropuestasDetalleAlumnoContainer
        val resumen = binding.cardResumenPropuestasDetalleAlumno
        val botonNueva = binding.btnNuevaPropuestaDetalleAlumno
        container.removeAllViews()
        container.addView(resumen)

        if (propuestas.isEmpty()) {
            container.addView(crearTextoVacio())
        } else {
            propuestas.forEachIndexed { index, propuesta ->
                container.addView(crearCardPropuesta(index + 1, propuesta))
            }
        }

        container.addView(botonNueva)
    }

    private fun crearCardPropuesta(numero: Int, propuesta: AlumnoPropuestaModel): CardView {
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
            setPadding(0, 0, 0, dp(12))
        }

        contenido.addView(TextView(this).apply {
            text = "Propuesta $numero"
            setTextColor(Color.WHITE)
            textSize = 18f
            setTypeface(typeface, Typeface.BOLD)
            gravity = Gravity.CENTER
            setBackgroundColor(Color.parseColor("#B30000"))
            setPadding(dp(12), dp(10), dp(12), dp(10))
        })

        val cuerpo = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(14), dp(14), dp(14), 0)
        }

        cuerpo.addView(crearEtiqueta("Titulo"))
        cuerpo.addView(crearValor(propuesta.tituloPropuesta.ifBlank { "Sin titulo" }))
        cuerpo.addView(crearEtiqueta("Descripcion", margenTop = 12))
        cuerpo.addView(crearValor(propuesta.descripcionPropuesta.ifBlank { "Sin descripcion" }))
        cuerpo.addView(crearEtiqueta("Estado", margenTop = 12))
        cuerpo.addView(crearValor(propuesta.estadoPropuesta.ifBlank { "En revision" }))
        cuerpo.addView(crearEtiqueta("Observacion", margenTop = 12))
        cuerpo.addView(crearValor(propuesta.observacionPropuesta.ifBlank { "Sin observaciones." }))
        cuerpo.addView(crearEtiqueta("Fecha de registro", margenTop = 12))
        cuerpo.addView(crearValor(propuesta.fechaRegistro.ifBlank { "Sin fecha" }))
        cuerpo.addView(crearEtiqueta("Archivo", margenTop = 12))
        cuerpo.addView(crearValor(propuesta.urlArchivo ?: "Sin archivo adjunto"))

        contenido.addView(cuerpo)
        card.addView(contenido)
        return card
    }

    private fun crearEtiqueta(texto: String, margenTop: Int = 0): TextView {
        return TextView(this).apply {
            text = texto
            setTextColor(Color.parseColor("#222222"))
            textSize = 15f
            setTypeface(typeface, Typeface.BOLD)
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = dp(margenTop)
            }
        }
    }

    private fun crearValor(texto: String): TextView {
        return TextView(this).apply {
            text = texto
            setTextColor(Color.parseColor("#555555"))
            textSize = 15f
            setBackgroundColor(Color.parseColor("#F5F5F5"))
            setPadding(dp(12), dp(10), dp(12), dp(10))
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = dp(6)
            }
        }
    }

    private fun crearTextoVacio(): TextView {
        return TextView(this).apply {
            text = "Todavia no hay propuestas registradas para este grupo."
            setTextColor(Color.parseColor("#444444"))
            textSize = 16f
            setPadding(dp(12), dp(24), dp(12), dp(12))
        }
    }

    private fun configurarEventos() {
        binding.btnBackPropuestasDetalleAlumno.setOnClickListener { finish() }

        binding.btnPerfilPropuestasDetalleAlumno.setOnClickListener {
            mostrarMensaje("Perfil pendiente")
        }

        binding.btnNuevaPropuestaDetalleAlumno.setOnClickListener {
            startActivity(Intent(this, AlumnoRegistrarPropuestaActivity::class.java))
        }

        binding.itemInicioPropuestasDetalleAlumno.setOnClickListener {
            startActivity(Intent(this, DashboardAlumnoActivity::class.java))
            finish()
        }

        binding.itemGrupoPropuestasDetalleAlumno.setOnClickListener {
            startActivity(Intent(this, AlumnoGrupoDetalleActivity::class.java))
            finish()
        }

        binding.itemPerfilPropuestasDetalleAlumno.setOnClickListener {
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
