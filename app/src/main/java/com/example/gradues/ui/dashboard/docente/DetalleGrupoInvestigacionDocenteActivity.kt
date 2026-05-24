// DetalleGrupoInvestigacionDocenteActivity.kt
package com.example.gradues.ui.dashboard.docente

import android.os.Bundle
import android.view.LayoutInflater
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.gradues.R
import com.example.gradues.data.dao.GrupoInvestigacionDocenteDao
import com.example.gradues.data.db.DatabaseHelper
import com.example.gradues.data.model.DocumentoInvestigacionDocenteModel
import com.example.gradues.data.model.EstudianteSubgrupoModel
import com.example.gradues.data.model.GrupoInvestigacionDocenteModel
import com.example.gradues.data.model.NotaEtapaDocenteModel
import com.example.gradues.data.model.PropuestaPerfilDocenteModel
import com.example.gradues.databinding.ActivityDetalleGrupoInvestigacionDocenteBinding
import com.example.gradues.utils.SessionManager
import com.google.android.material.card.MaterialCardView
import androidx.core.view.setMargins
import android.util.Log

class DetalleGrupoInvestigacionDocenteActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetalleGrupoInvestigacionDocenteBinding
    private lateinit var grupoInvestigacionDocenteDao: GrupoInvestigacionDocenteDao
    private lateinit var sessionManager: SessionManager

    private var idGrupoTGI: String? = null
    private var idTrabajoGraduacion: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetalleGrupoInvestigacionDocenteBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)
        grupoInvestigacionDocenteDao = GrupoInvestigacionDocenteDao(DatabaseHelper(this))

        idGrupoTGI = intent.getStringExtra("ID_GRUPO_TGI")
        idTrabajoGraduacion = intent.getStringExtra("ID_TRABAJO_GRADUACION")

        if (idGrupoTGI.isNullOrBlank() || idTrabajoGraduacion.isNullOrBlank()) {
            Log.e(TAG, "Error: ID de grupo o trabajo de graduación no encontrado. idGrupoTGI: $idGrupoTGI, idTrabajoGraduacion: $idTrabajoGraduacion")
            Toast.makeText(this, "Error: ID de grupo o trabajo de graduación no encontrado.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        configurarEventos()
        cargarDetalleGrupoInvestigacion(idGrupoTGI!!, idTrabajoGraduacion!!)
    }

    private fun configurarEventos() {
        binding.btnBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun cargarDetalleGrupoInvestigacion(idGrupo: String, idTrabajo: String) {
        Log.d(TAG, "Cargando detalle para idGrupo: $idGrupo, idTrabajo: $idTrabajo")
        val grupo = grupoInvestigacionDocenteDao.obtenerDetalleGrupoInvestigacion(idGrupo)
        if (grupo != null) {
            Log.d(TAG, "Grupo encontrado: $grupo")
            pintarDetalleGrupoInvestigacion(grupo)
            cargarEstudiantesGrupo(idTrabajo)
            cargarPropuestasPerfil(idTrabajo)
            cargarDocumentosGrupo(idTrabajo)
            cargarNotasGrupo(idTrabajo)
        } else {
            Log.e(TAG, "No se encontró el detalle del grupo de investigación para idGrupo: $idGrupo")
            Toast.makeText(this, "No se encontró el detalle del grupo de investigación.", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun pintarDetalleGrupoInvestigacion(model: GrupoInvestigacionDocenteModel) {
        binding.tvDetalleCodigoGrupo.text = "Código: ${model.codigoGrupoTGI}"
        binding.tvDetalleNombreTrabajoInvestigacion.text = model.nombreTrabajo
        binding.tvDetalleEstadoGrupoInvestigacion.text = "Estado: ${model.estadoGrupo}"
        binding.tvDetalleTotalEstudiantesInvestigacion.text = "Total de Estudiantes: ${model.totalEstudiantes}"
        binding.tvDetalleTotalPropuestasInvestigacion.text = "Total de Propuestas: ${model.totalPropuestas}"
        binding.tvDetalleTotalDocumentosInvestigacion.text = "Total de Documentos: ${model.totalDocumentos}"
    }

    private fun cargarEstudiantesGrupo(idTrabajo: String) {
        val estudiantes = grupoInvestigacionDocenteDao.obtenerEstudiantesGrupoInvestigacion(idTrabajo)
        Log.d(TAG, "Estudiantes cargados para idTrabajo ($idTrabajo): ${estudiantes.size}")
        binding.llEstudiantesGrupoContainer.removeAllViews()

        if (estudiantes.isEmpty()) {
            val tvNoEstudiantes = TextView(this).apply {
                text = "No hay estudiantes en este grupo."
                layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply {
                    setMargins(0, 8.dpToPx(), 0, 0)
                }
                setTextAppearance(androidx.appcompat.R.style.TextAppearance_AppCompat_Small)
            }
            binding.llEstudiantesGrupoContainer.addView(tvNoEstudiantes)
            return
        }

        estudiantes.forEach { estudiante ->
            val cardView = LayoutInflater.from(this).inflate(R.layout.item_estudiante_investigacion_docente, binding.llEstudiantesGrupoContainer, false) as MaterialCardView
            cardView.findViewById<TextView>(R.id.tvNombreEstudianteInvestigacion).text = estudiante.nombreCompleto
            cardView.findViewById<TextView>(R.id.tvCarnetEstudianteInvestigacion).text = "Carnet: ${estudiante.carnetUsuario}"
            cardView.findViewById<TextView>(R.id.tvPromedioEstudianteInvestigacion).text = "Promedio: ${estudiante.promedioNotas}"
            binding.llEstudiantesGrupoContainer.addView(cardView)
        }
    }

    private fun cargarPropuestasPerfil(idTrabajo: String) {
        val propuestas = grupoInvestigacionDocenteDao.obtenerPropuestasPerfilGrupoInvestigacion(idTrabajo)
        Log.d(TAG, "Propuestas cargadas para idTrabajo ($idTrabajo): ${propuestas.size}")
        binding.llPropuestasPerfilContainer.removeAllViews()

        if (propuestas.isEmpty()) {
            val tvNoPropuestas = TextView(this).apply {
                text = "No hay propuestas de perfil registradas."
                layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply {
                    setMargins(0, 8.dpToPx(), 0, 0)
                }
                setTextAppearance(androidx.appcompat.R.style.TextAppearance_AppCompat_Small)
            }
            binding.llPropuestasPerfilContainer.addView(tvNoPropuestas)
            return
        }

        propuestas.forEach { propuesta ->
            val cardView = LayoutInflater.from(this).inflate(R.layout.item_propuesta_perfil_docente, binding.llPropuestasPerfilContainer, false) as MaterialCardView
            cardView.findViewById<TextView>(R.id.tvTituloPropuesta).text = propuesta.tituloPropuesta
            cardView.findViewById<TextView>(R.id.tvEstadoPropuesta).text = "Estado: ${propuesta.estadoPropuesta}"
            cardView.findViewById<TextView>(R.id.tvFechaRegistroPropuesta).text = "Fecha de Registro: ${propuesta.fechaRegistro}"
            cardView.findViewById<TextView>(R.id.tvObservacionPropuesta).text = "Observación: ${propuesta.observacionPropuesta ?: "Sin observaciones."}"
            cardView.findViewById<TextView>(R.id.tvUrlDocumentoPropuesta).text = "Documento: ${propuesta.urlDocumento ?: "No file uploaded."}"

            // TODO: Add click listener to view document if URL exists (future phase)
            binding.llPropuestasPerfilContainer.addView(cardView)
        }
    }

    private fun cargarDocumentosGrupo(idTrabajo: String) {
        val documentos = grupoInvestigacionDocenteDao.obtenerDocumentosGrupoInvestigacion(idTrabajo)
        Log.d(TAG, "Documentos cargados para idTrabajo ($idTrabajo): ${documentos.size}")
        binding.llDocumentosTrabajoContainer.removeAllViews()

        if (documentos.isEmpty()) {
            val tvNoDocumentos = TextView(this).apply {
                text = "No hay documentos registrados para este trabajo."
                layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply {
                    setMargins(0, 8.dpToPx(), 0, 0)
                }
                setTextAppearance(androidx.appcompat.R.style.TextAppearance_AppCompat_Small)
            }
            binding.llDocumentosTrabajoContainer.addView(tvNoDocumentos)
            return
        }

        documentos.forEach { documento ->
            val cardView = LayoutInflater.from(this).inflate(R.layout.item_documento_investigacion_docente, binding.llDocumentosTrabajoContainer, false) as MaterialCardView
            cardView.findViewById<TextView>(R.id.tvNombreDocumentoInvestigacion).text = documento.nombreDocumento
            cardView.findViewById<TextView>(R.id.tvTipoDocumentoInvestigacion).text = "Tipo: ${documento.tipoDocumento}"
            cardView.findViewById<TextView>(R.id.tvVersionDocumentoInvestigacion).text = "Versión: ${documento.versionDocumento}"
            cardView.findViewById<TextView>(R.id.tvEstadoDocumentoInvestigacion).text = "Estado: ${documento.estadoDocumento}"
            cardView.findViewById<TextView>(R.id.tvFechaCargaDocumentoInvestigacion).text = "Fecha de Carga: ${documento.fechaCarga}"
            cardView.findViewById<TextView>(R.id.tvObservacionDocumentoInvestigacion).text = "Observación: ${documento.observacionDocumento ?: "Sin observaciones."}"
            cardView.findViewById<TextView>(R.id.tvUrlDocumentoInvestigacion).text = "Documento: ${documento.urlDocumento ?: "No file uploaded."}"

            // TODO: Add click listener to view document if URL exists (future phase)
            binding.llDocumentosTrabajoContainer.addView(cardView)
        }
    }

    private fun cargarNotasGrupo(idTrabajo: String) {
        val notas = grupoInvestigacionDocenteDao.obtenerNotasGrupoInvestigacion(idTrabajo)
        Log.d(TAG, "Notas cargadas para idTrabajo ($idTrabajo): ${notas.size}")
        binding.llNotasEtapaContainer.removeAllViews()

        if (notas.isEmpty()) {
            val tvNoNotas = TextView(this).apply {
                text = "No hay notas registradas para este grupo."
                layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply {
                    setMargins(0, 8.dpToPx(), 0, 0)
                }
                setTextAppearance(androidx.appcompat.R.style.TextAppearance_AppCompat_Small)
            }
            binding.llNotasEtapaContainer.addView(tvNoNotas)
            return
        }

        notas.forEach { nota ->
            val cardView = LayoutInflater.from(this).inflate(R.layout.item_nota_etapa_docente, binding.llNotasEtapaContainer, false) as MaterialCardView

            cardView.findViewById<TextView>(R.id.tvNombreEtapaNota).text = nota.nombreEtapa
            cardView.findViewById<TextView>(R.id.tvNotaValor).text = "Nota: ${nota.nota?.let { String.format("%.2f", it) } ?: "N/A"}"
            cardView.findViewById<TextView>(R.id.tvNotaObservacion).text = "Observación: ${nota.observacion ?: "Sin observaciones."}"
            cardView.findViewById<TextView>(R.id.tvNotaFechaRegistro).text = "Fecha de Registro: ${nota.fechaRegistro}"
            binding.llNotasEtapaContainer.addView(cardView)
        }
    }

    // Extension function to convert dp to pixels
    private fun Int.dpToPx(): Int = (this * resources.displayMetrics.density).toInt()

    companion object {
        private const val TAG = "DetalleGrupoInvestigacionDocenteActivity"
    }
}