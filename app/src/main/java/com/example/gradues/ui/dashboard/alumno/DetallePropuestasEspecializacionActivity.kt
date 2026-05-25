package com.example.gradues.ui.dashboard.alumno

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.gradues.R
import com.example.gradues.data.dao.PropuestaEspecializacionAlumnoDao
import com.example.gradues.data.db.DatabaseHelper
import com.example.gradues.data.model.DetallePropuestaEspecializacionAlumnoModel
import com.example.gradues.databinding.ActivityDetallePropuestasEspecializacionBinding
import com.example.gradues.utils.SessionManager

class DetallePropuestasEspecializacionActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetallePropuestasEspecializacionBinding
    private lateinit var propuestaDao: PropuestaEspecializacionAlumnoDao
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetallePropuestasEspecializacionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        propuestaDao = PropuestaEspecializacionAlumnoDao(DatabaseHelper(this))
        sessionManager = SessionManager(this)

        configurarEventos()
    }

    override fun onResume() {
        super.onResume()
        cargarPropuestas()
    }

    private fun configurarEventos() {
        binding.btnBackDetallePropuestasEspecializacion.setOnClickListener { finish() }

        binding.btnRegistrarNuevaPropuestaEspecializacion.setOnClickListener {
            startActivity(Intent(this, RegistrarPropuestasEspecializacionActivity::class.java))
        }

        binding.itemInicioDetallePropuestasEspecializacion.setOnClickListener { finish() }
        binding.itemGrupoDetallePropuestasEspecializacion.setOnClickListener {
            startActivity(Intent(this, DetalleSubgrupoEspecializacionActivity::class.java))
        }
        binding.itemPerfilDetallePropuestasEspecializacion.setOnClickListener {
            mostrarMensaje("Perfil pendiente de implementar")
        }
        binding.btnNotificacionesDetallePropuestasEspecializacion.setOnClickListener {
            mostrarMensaje("Notificaciones pendientes de implementar")
        }
        binding.btnPerfilDetallePropuestasEspecializacion.setOnClickListener {
            mostrarMensaje("Perfil pendiente de implementar")
        }
    }

    private fun cargarPropuestas() {
        val idSesion = sessionManager.getIdUsuario().trim()
        if (idSesion.isBlank()) {
            mostrarMensaje("No se encontró la sesión del alumno.")
            mostrarPropuestas(emptyList())
            return
        }

        mostrarPropuestas(propuestaDao.obtenerPropuestasEspecializacionActiva(idSesion))
    }

    private fun mostrarPropuestas(propuestas: List<DetallePropuestaEspecializacionAlumnoModel>) {
        binding.llPropuestasEspecializacionContainer.removeAllViews()
        binding.tvEmptyPropuestasEspecializacion.visibility =
            if (propuestas.isEmpty()) View.VISIBLE else View.GONE

        propuestas.forEach { propuesta ->
            val row = layoutInflater.inflate(
                R.layout.row_propuesta_especializacion_detalle,
                binding.llPropuestasEspecializacionContainer,
                false
            )

            row.findViewById<TextView>(R.id.tvTituloPropuestaDetalle).text = propuesta.tituloPropuesta
            row.findViewById<TextView>(R.id.tvEstadoPropuestaDetalle).text = "Estado: ${propuesta.estadoPropuesta}"
            row.findViewById<TextView>(R.id.tvDescripcionPropuestaDetalle).text = propuesta.descripcionPropuesta
            row.findViewById<TextView>(R.id.tvObservacionPropuestaDetalle).text =
                "Observación: ${propuesta.observacionPropuesta?.ifBlank { null } ?: "Sin observaciones"}"
            row.findViewById<TextView>(R.id.tvArchivoPropuestaDetalle).text =
                propuesta.urlArchivo?.ifBlank { null }?.let {
                    "Archivo adjunto: ${obtenerNombreArchivo(Uri.parse(it)).ifBlank { it }}"
                } ?: "Archivo: Sin archivo adjunto"
            row.findViewById<TextView>(R.id.tvFechaPropuestaDetalle).text =
                "Fecha: ${propuesta.fechaRegistro.ifBlank { "Sin fecha" }}"

            binding.llPropuestasEspecializacionContainer.addView(row)
        }
    }

    private fun obtenerNombreArchivo(uri: Uri): String {
        if (uri.scheme == "content") {
            contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (index >= 0 && !cursor.isNull(index)) {
                        return cursor.getString(index)
                    }
                }
            }
        }

        return uri.lastPathSegment?.substringAfterLast('/') ?: ""
    }

    private fun mostrarMensaje(mensaje: String) {
        Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show()
    }
}
