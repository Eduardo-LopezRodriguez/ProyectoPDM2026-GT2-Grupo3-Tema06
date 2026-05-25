package com.example.gradues.ui.dashboard.alumno

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.gradues.data.dao.TesinaEspecializacionAlumnoDao
import com.example.gradues.data.db.DatabaseHelper
import com.example.gradues.data.model.TesinaEspecializacionAlumnoModel
import com.example.gradues.databinding.ActivityTesinaGrupoEspecializacionBinding
import com.example.gradues.utils.SessionManager

class TesinaGrupoEspecializacionActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTesinaGrupoEspecializacionBinding
    private lateinit var tesinaDao: TesinaEspecializacionAlumnoDao
    private lateinit var sessionManager: SessionManager
    private var archivoSeleccionado: Uri? = null

    private val selectorPdf = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode != RESULT_OK) return@registerForActivityResult

        val uri = result.data?.data ?: return@registerForActivityResult
        if (!esPdfValido(uri)) {
            mostrarMensaje("Solo se permiten archivos PDF")
            return@registerForActivityResult
        }

        result.data?.flags?.let { flags ->
            val permisos = flags and Intent.FLAG_GRANT_READ_URI_PERMISSION
            try {
                contentResolver.takePersistableUriPermission(uri, permisos)
            } catch (_: SecurityException) {
                // Some providers do not support persistable permissions.
            }
        }

        archivoSeleccionado = uri
        binding.tvArchivoSeleccionadoTesinaEspecializacion.text =
            "Archivo seleccionado: ${obtenerNombreArchivo(uri).ifBlank { "archivo.pdf" }}"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTesinaGrupoEspecializacionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        tesinaDao = TesinaEspecializacionAlumnoDao(DatabaseHelper(this))
        sessionManager = SessionManager(this)

        configurarEventos()
    }

    override fun onResume() {
        super.onResume()
        cargarTesina()
    }

    private fun configurarEventos() {
        binding.btnBackTesinaEspecializacion.setOnClickListener { finish() }
        binding.cardObservacionesTesinaEspecializacion.setOnClickListener {
            mostrarMensaje("Observaciones cargadas desde la última versión")
        }
        binding.layoutAdjuntarTesinaEspecializacion.setOnClickListener {
            abrirSelectorPdf()
        }
        binding.btnSubirNuevaVersionTesinaEspecializacion.setOnClickListener {
            subirNuevaVersion()
        }
        binding.btnHistorialTesinaGrupoEspecializacion.setOnClickListener {
            startActivity(Intent(this, HistorialTesinaEspecializacionActivity::class.java))
        }
        binding.itemInicioTesinaEspecializacion.setOnClickListener { finish() }
        binding.itemGrupoTesinaEspecializacion.setOnClickListener {
            startActivity(Intent(this, DetalleSubgrupoEspecializacionActivity::class.java))
        }
        binding.itemPerfilTesinaEspecializacion.setOnClickListener { mostrarMensaje("Perfil pendiente de implementar") }
        binding.btnNotificacionesTesinaEspecializacion.setOnClickListener { mostrarMensaje("Notificaciones pendientes de implementar") }
        binding.btnPerfilTesinaEspecializacion.setOnClickListener { mostrarMensaje("Perfil pendiente de implementar") }
    }

    private fun cargarTesina() {
        val idSesion = sessionManager.getIdUsuario().trim()
        if (idSesion.isBlank()) {
            mostrarEstadoSinTesina()
            return
        }

        val tesina = tesinaDao.obtenerTesinaActiva(idSesion)
        if (tesina == null) {
            mostrarEstadoSinTesina()
            return
        }

        mostrarTesina(tesina)
    }

    private fun mostrarTesina(tesina: TesinaEspecializacionAlumnoModel) {
        val documento = tesina.documentoActual
        binding.tvTituloTesinaEspecializacion.text = documento?.tituloDocumento ?: tesina.tituloTrabajo
        binding.tvEstadoTesinaEspecializacion.text = "Estado: ${documento?.estadoDocumento ?: "No subida"}"
        binding.tvVersionTesinaEspecializacion.text = "Última versión: ${
            documento?.versionDocumento?.let { "Versión $it" } ?: "No enviada"
        }"
        binding.tvObservacionTesinaEspecializacion.text =
            documento?.observacionDocumento?.ifBlank { null }
                ?: "Todavía no has subido ninguna versión de la tesina."
    }

    private fun mostrarEstadoSinTesina() {
        binding.tvTituloTesinaEspecializacion.text = "Tesina del grupo"
        binding.tvEstadoTesinaEspecializacion.text = "Estado: No subida"
        binding.tvVersionTesinaEspecializacion.text = "Última versión: No enviada"
        binding.tvObservacionTesinaEspecializacion.text =
            "Todavía no has subido ninguna versión de la tesina."
    }

    private fun abrirSelectorPdf() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "application/pdf"
            putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("application/pdf"))
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
        }
        selectorPdf.launch(intent)
    }

    private fun subirNuevaVersion() {
        val uri = archivoSeleccionado
        if (uri == null) {
            mostrarMensaje("Seleccione un archivo PDF")
            return
        }

        val idSesion = sessionManager.getIdUsuario().trim()
        if (idSesion.isBlank()) {
            mostrarMensaje("No se encontró la sesión del alumno.")
            return
        }

        val subida = tesinaDao.subirNuevaVersion(idSesion, uri.toString())
        if (subida) {
            archivoSeleccionado = null
            binding.tvArchivoSeleccionadoTesinaEspecializacion.text = "Sin archivo seleccionado"
            mostrarMensaje("Tesina subida correctamente")
            cargarTesina()
        } else {
            mostrarMensaje("No se pudo subir la tesina")
        }
    }

    private fun esPdfValido(uri: Uri): Boolean {
        val mimeType = contentResolver.getType(uri)
        if (mimeType == "application/pdf") return true

        return obtenerNombreArchivo(uri).endsWith(".pdf", ignoreCase = true)
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
