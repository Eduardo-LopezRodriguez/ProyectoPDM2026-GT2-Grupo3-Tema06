package com.example.gradues.ui.dashboard.docente

import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.view.LayoutInflater
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.gradues.R
import com.example.gradues.data.dao.CursoEspecializacionDocenteDao
import com.example.gradues.data.db.DatabaseHelper
import com.example.gradues.data.model.PropuestaEspecializacionDocenteModel
import com.example.gradues.databinding.ActivityRevisionPropuestasEspecializacionDocenteBinding

class RevisionPropuestasEspecializacionDocenteActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRevisionPropuestasEspecializacionDocenteBinding
    private lateinit var cursoDao: CursoEspecializacionDocenteDao
    private var idTrabajoGraduacion: String = ""
    private var nombreSubgrupo: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRevisionPropuestasEspecializacionDocenteBinding.inflate(layoutInflater)
        setContentView(binding.root)

        cursoDao = CursoEspecializacionDocenteDao(DatabaseHelper(this))
        idTrabajoGraduacion = intent.getStringExtra("ID_TRABAJO_GRADUACION").orEmpty()
        nombreSubgrupo = intent.getStringExtra("NOMBRE_SUBGRUPO").orEmpty()

        if (idTrabajoGraduacion.isBlank()) {
            Toast.makeText(this, "Error: ID de trabajo no encontrado.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        binding.btnBackRevisionPropuestas.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
        binding.tvTituloRevisionPropuestas.text = "Revisión de propuestas"
        binding.tvSubtituloRevisionPropuestas.text = nombreSubgrupo.ifBlank { "Curso de especialización" }

        cargarPropuestas()
    }

    private fun cargarPropuestas() {
        val propuestas = cursoDao.obtenerPropuestasEspecializacion(idTrabajoGraduacion)
        mostrarPropuestas(propuestas)
    }

    private fun mostrarPropuestas(propuestas: List<PropuestaEspecializacionDocenteModel>) {
        binding.llPropuestasRevisionContainer.removeAllViews()

        if (propuestas.isEmpty()) {
            binding.llPropuestasRevisionContainer.addView(
                TextView(this).apply {
                    text = "No hay propuestas registradas."
                    setTextColor(0xFF555555.toInt())
                    textSize = 16f
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                    setPadding(0, 18, 0, 0)
                }
            )
            return
        }

        propuestas.forEach { propuesta ->
            val row = LayoutInflater.from(this).inflate(
                R.layout.row_propuesta_especializacion_docente,
                binding.llPropuestasRevisionContainer,
                false
            )

            row.findViewById<TextView>(R.id.tvTituloPropuestaDocente).text = propuesta.tituloPropuesta
            row.findViewById<TextView>(R.id.tvEstadoPropuestaDocente).text = "Estado: ${propuesta.estadoPropuesta}"
            row.findViewById<TextView>(R.id.tvDescripcionPropuestaDocente).text = propuesta.descripcionPropuesta
            row.findViewById<TextView>(R.id.tvArchivoPropuestaDocente).text =
                propuesta.urlArchivo?.ifBlank { null }?.let {
                    "Archivo: ${obtenerNombreArchivo(Uri.parse(it)).ifBlank { it }}"
                } ?: "Archivo: Sin archivo adjunto"
            row.findViewById<TextView>(R.id.tvFechaPropuestaDocente).text =
                "Fecha: ${propuesta.fechaRegistro.ifBlank { "Sin fecha" }}"

            val etObservacion = row.findViewById<EditText>(R.id.etObservacionPropuestaDocente)
            etObservacion.setText(propuesta.observacionPropuesta.orEmpty())

            row.findViewById<Button>(R.id.btnAprobarPropuestaDocente).setOnClickListener {
                actualizarRevision(propuesta.idPropuesta, "Aprobada", etObservacion.text.toString())
            }
            row.findViewById<Button>(R.id.btnObservacionPropuestaDocente).setOnClickListener {
                actualizarRevision(propuesta.idPropuesta, "Con observación", etObservacion.text.toString())
            }
            row.findViewById<Button>(R.id.btnDenegarPropuestaDocente).setOnClickListener {
                actualizarRevision(propuesta.idPropuesta, "Denegada", etObservacion.text.toString())
            }

            binding.llPropuestasRevisionContainer.addView(row)
        }
    }

    private fun actualizarRevision(idPropuesta: Int, estado: String, observacion: String) {
        val actualizada = cursoDao.actualizarRevisionPropuestaEspecializacion(
            idPropuesta = idPropuesta,
            estadoPropuesta = estado,
            observacionPropuesta = observacion.trim().ifBlank { null }
        )

        if (actualizada) {
            Toast.makeText(this, "Propuesta actualizada.", Toast.LENGTH_SHORT).show()
            cargarPropuestas()
        } else {
            Toast.makeText(this, "No se pudo actualizar la propuesta.", Toast.LENGTH_SHORT).show()
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
}
