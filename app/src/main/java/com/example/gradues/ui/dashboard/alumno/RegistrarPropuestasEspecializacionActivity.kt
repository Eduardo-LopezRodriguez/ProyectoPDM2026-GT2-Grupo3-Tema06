package com.example.gradues.ui.dashboard.alumno

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.gradues.R
import com.example.gradues.data.dao.PropuestaEspecializacionAlumnoDao
import com.example.gradues.data.db.DatabaseHelper
import com.example.gradues.data.model.PropuestaEspecializacionAlumnoModel
import com.example.gradues.databinding.ActivityRegistrarPropuestasEspecializacionBinding
import com.example.gradues.utils.SessionManager
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class RegistrarPropuestasEspecializacionActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegistrarPropuestasEspecializacionBinding
    private lateinit var propuestaDao: PropuestaEspecializacionAlumnoDao
    private lateinit var sessionManager: SessionManager
    private var propuestaSeleccionArchivo: Int = 0
    private var archivoPropuesta1: Uri? = null
    private var archivoPropuesta2: Uri? = null

    private val selectorPdf = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode != RESULT_OK) return@registerForActivityResult

        val uri = result.data?.data ?: return@registerForActivityResult
        if (!esPdfValido(uri)) {
            mostrarMensaje("Solo se permiten archivos PDF")
            return@registerForActivityResult
        }

        result.data?.flags?.let { flags ->
            val permisos = flags and (Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
            try {
                contentResolver.takePersistableUriPermission(uri, permisos and Intent.FLAG_GRANT_READ_URI_PERMISSION)
            } catch (_: SecurityException) {
                // Some providers do not support persistable permissions.
            }
        }

        when (propuestaSeleccionArchivo) {
            1 -> {
                archivoPropuesta1 = uri
                mostrarArchivoSeleccionado(1, uri)
            }
            2 -> {
                archivoPropuesta2 = uri
                mostrarArchivoSeleccionado(2, uri)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegistrarPropuestasEspecializacionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        propuestaDao = PropuestaEspecializacionAlumnoDao(DatabaseHelper(this))
        sessionManager = SessionManager(this)

        configurarEventos()
    }

    private fun configurarEventos() {
        binding.btnBackRegistrarEspecializacion.setOnClickListener { finish() }
        findViewById<View>(R.id.layoutAdjuntarPropuesta1Especializacion).setOnClickListener {
            abrirSelectorPdf(1)
        }
        findViewById<View>(R.id.layoutAdjuntarPropuesta2Especializacion).setOnClickListener {
            abrirSelectorPdf(2)
        }
        findViewById<View>(R.id.btnGuardarPropuesta1Especializacion).setOnClickListener {
            guardarPropuestaIndividual(1, "Borrador")
        }
        findViewById<View>(R.id.btnGuardarPropuesta2Especializacion).setOnClickListener {
            guardarPropuestaIndividual(2, "Borrador")
        }
        binding.btnGuardarBorradoresEspecializacion.setOnClickListener {
            guardarPropuestasConDatos("Borrador")
        }
        binding.btnEnviarPropuestasEspecializacion.setOnClickListener {
            guardarPropuestasConDatos("Pendiente")
        }
        binding.itemInicioRegistrarEspecializacion.setOnClickListener { finish() }
        binding.itemGrupoRegistrarEspecializacion.setOnClickListener {
            startActivity(Intent(this, DetalleSubgrupoEspecializacionActivity::class.java))
        }
        binding.itemPerfilRegistrarEspecializacion.setOnClickListener { mostrarMensaje("Perfil pendiente de implementar") }
        binding.btnNotificacionesRegistrarEspecializacion.setOnClickListener { mostrarMensaje("Notificaciones pendientes de implementar") }
        binding.btnPerfilRegistrarEspecializacion.setOnClickListener { mostrarMensaje("Perfil pendiente de implementar") }
    }

    private fun mostrarMensaje(mensaje: String) {
        Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show()
    }

    private fun abrirSelectorPdf(numeroPropuesta: Int) {
        propuestaSeleccionArchivo = numeroPropuesta
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "application/pdf"
            putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("application/pdf"))
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
        }
        selectorPdf.launch(intent)
    }

    private fun esPdfValido(uri: Uri): Boolean {
        val mimeType = contentResolver.getType(uri)
        if (mimeType == "application/pdf") return true

        val nombre = obtenerNombreArchivo(uri)
        return nombre.endsWith(".pdf", ignoreCase = true)
    }

    private fun mostrarArchivoSeleccionado(numeroPropuesta: Int, uri: Uri) {
        val nombre = obtenerNombreArchivo(uri).ifBlank { "archivo.pdf" }
        val textViewId = if (numeroPropuesta == 1) {
            R.id.tvArchivoSeleccionadoPropuesta1Especializacion
        } else {
            R.id.tvArchivoSeleccionadoPropuesta2Especializacion
        }

        findViewById<TextView>(textViewId).text = "Archivo seleccionado: $nombre"
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

    private fun guardarPropuestaIndividual(numeroPropuesta: Int, estado: String) {
        val datos = leerPropuesta(numeroPropuesta)

        if (!datos.esValida()) {
            mostrarMensaje("Complete el título y la descripción de la propuesta $numeroPropuesta.")
            return
        }

        val guardada = guardarPropuestas(listOf(datos), estado)
        mostrarMensaje(if (guardada) "Propuesta guardada" else "No se pudo guardar la propuesta")
    }

    private fun guardarPropuestasConDatos(estado: String) {
        val propuestasConDatos = listOf(leerPropuesta(1), leerPropuesta(2))
            .filter { it.tieneContenido() }

        if (propuestasConDatos.isEmpty()) {
            mostrarMensaje("Ingrese al menos una propuesta.")
            return
        }

        val incompletas = propuestasConDatos.filterNot { it.esValida() }
        if (incompletas.isNotEmpty()) {
            mostrarMensaje("Cada propuesta debe tener título y descripción.")
            return
        }

        val guardadas = guardarPropuestas(propuestasConDatos, estado)
        val mensaje = when {
            !guardadas -> "No se pudieron guardar las propuestas"
            estado == "Pendiente" -> "Propuestas enviadas"
            else -> "Borradores guardados"
        }
        mostrarMensaje(mensaje)
    }

    private fun guardarPropuestas(propuestas: List<DatosPropuesta>, estado: String): Boolean {
        val idSesion = sessionManager.getIdUsuario().trim()
        if (idSesion.isEmpty()) {
            mostrarMensaje("No se encontró la sesión del alumno.")
            return false
        }

        val idTrabajo = propuestaDao.obtenerIdTrabajoEspecializacionActivo(idSesion)
        if (idTrabajo == null) {
            mostrarMensaje("No se encontró un subgrupo de especialización activo.")
            return false
        }

        val fechaRegistro = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date())

        return propuestas.all { datos ->
            propuestaDao.insertarPropuesta(
                PropuestaEspecializacionAlumnoModel(
                    idPropuesta = null,
                    idTrabajoGraduacion = idTrabajo,
                    tituloPropuesta = datos.titulo,
                    descripcionPropuesta = datos.descripcion,
                    estadoPropuesta = estado,
                    observacionPropuesta = null,
                    urlArchivo = datos.urlArchivo,
                    fechaRegistro = fechaRegistro
                )
            )
        }
    }

    private fun leerPropuesta(numeroPropuesta: Int): DatosPropuesta {
        val tituloId = if (numeroPropuesta == 1) {
            R.id.etTituloPropuesta1Especializacion
        } else {
            R.id.etTituloPropuesta2Especializacion
        }

        val descripcionId = if (numeroPropuesta == 1) {
            R.id.etDescripcionPropuesta1Especializacion
        } else {
            R.id.etDescripcionPropuesta2Especializacion
        }

        return DatosPropuesta(
            titulo = findViewById<EditText>(tituloId).text.toString().trim(),
            descripcion = findViewById<EditText>(descripcionId).text.toString().trim(),
            urlArchivo = if (numeroPropuesta == 1) {
                archivoPropuesta1?.toString()
            } else {
                archivoPropuesta2?.toString()
            }
        )
    }

    private data class DatosPropuesta(
        val titulo: String,
        val descripcion: String,
        val urlArchivo: String?
    ) {
        fun tieneContenido(): Boolean = titulo.isNotEmpty() || descripcion.isNotEmpty()
        fun esValida(): Boolean = titulo.isNotEmpty() && descripcion.isNotEmpty()
    }
}
