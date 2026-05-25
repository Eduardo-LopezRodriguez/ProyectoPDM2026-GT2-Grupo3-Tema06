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
import com.example.gradues.data.dao.TesinaEspecializacionAlumnoDao
import com.example.gradues.data.db.DatabaseHelper
import com.example.gradues.data.model.HistorialTesinaAlumnoModel
import com.example.gradues.databinding.ActivityHistorialTesinaEspecializacionBinding
import com.example.gradues.utils.SessionManager

class HistorialTesinaEspecializacionActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHistorialTesinaEspecializacionBinding
    private lateinit var tesinaDao: TesinaEspecializacionAlumnoDao
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHistorialTesinaEspecializacionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        tesinaDao = TesinaEspecializacionAlumnoDao(DatabaseHelper(this))
        sessionManager = SessionManager(this)

        configurarEventos()
    }

    override fun onResume() {
        super.onResume()
        cargarHistorial()
    }

    private fun configurarEventos() {
        binding.btnBackHistorialTesinaEspecializacion.setOnClickListener { finish() }
        binding.btnPerfilHistorialTesinaEspecializacion.setOnClickListener {
            mostrarMensaje("Perfil pendiente de implementar")
        }
        binding.itemInicioHistorialTesinaEspecializacion.setOnClickListener { finish() }
        binding.itemGrupoHistorialTesinaEspecializacion.setOnClickListener {
            startActivity(Intent(this, DetalleSubgrupoEspecializacionActivity::class.java))
        }
        binding.itemPerfilHistorialTesinaEspecializacion.setOnClickListener {
            mostrarMensaje("Perfil pendiente de implementar")
        }
    }

    private fun cargarHistorial() {
        val idSesion = sessionManager.getIdUsuario().trim()
        if (idSesion.isBlank()) {
            mostrarHistorial(emptyList())
            return
        }

        mostrarHistorial(tesinaDao.obtenerHistorialTesinaActiva(idSesion))
    }

    private fun mostrarHistorial(historial: List<HistorialTesinaAlumnoModel>) {
        binding.llHistorialTesinaContainer.removeAllViews()
        binding.tvEmptyHistorialTesina.visibility = if (historial.isEmpty()) View.VISIBLE else View.GONE

        historial.forEach { documento ->
            val row = layoutInflater.inflate(
                R.layout.row_historial_tesina_especializacion,
                binding.llHistorialTesinaContainer,
                false
            )

            row.findViewById<TextView>(R.id.tvVersionHistorialTesina).text =
                documento.versionDocumento?.let { "Versión $it" } ?: "Versión sin número"
            row.findViewById<TextView>(R.id.tvEstadoHistorialTesina).text =
                "Estado: ${documento.estadoDocumento}"
            row.findViewById<TextView>(R.id.tvObservacionHistorialTesina).text =
                "Observación: ${documento.observacionDocumento?.ifBlank { null } ?: "Sin observaciones"}"
            row.findViewById<TextView>(R.id.tvArchivoHistorialTesina).text =
                documento.urlDocumento?.ifBlank { null }?.let {
                    "Archivo: ${obtenerNombreArchivo(Uri.parse(it)).ifBlank { it }}"
                } ?: "Archivo: Sin archivo"
            row.findViewById<TextView>(R.id.tvFechaHistorialTesina).text =
                "Fecha: ${documento.fechaSubida?.ifBlank { null } ?: "Sin fecha"}"

            binding.llHistorialTesinaContainer.addView(row)
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
