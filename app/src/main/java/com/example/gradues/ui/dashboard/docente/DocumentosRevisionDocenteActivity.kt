// DocumentosRevisionDocenteActivity.kt
package com.example.gradues.ui.dashboard.docente

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.gradues.R
import com.example.gradues.data.dao.DocumentoRevisionDocenteDao
import com.example.gradues.data.db.DatabaseHelper
import com.example.gradues.data.model.DocumentoRevisionDocenteModel
import com.example.gradues.databinding.ActivityDocumentosRevisionDocenteBinding
import com.example.gradues.utils.SessionManager
import com.google.android.material.card.MaterialCardView

class DocumentosRevisionDocenteActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDocumentosRevisionDocenteBinding
    private lateinit var sessionManager: SessionManager
    private lateinit var documentoRevisionDocenteDao: DocumentoRevisionDocenteDao

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDocumentosRevisionDocenteBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)
        documentoRevisionDocenteDao = DocumentoRevisionDocenteDao(DatabaseHelper(this))

        configurarEventos()
        cargarDocumentosEnRevision()
    }

    private fun configurarEventos() {
        binding.btnBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun cargarDocumentosEnRevision() {
        val idDocente = sessionManager.getIdUsuario()
        if (idDocente.isBlank()) {
            Toast.makeText(this, "Sesión de docente no válida.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        val documentos = documentoRevisionDocenteDao.obtenerDocumentosEnRevision(idDocente)
        displayDocumentosEnRevision(documentos)
    }

    private fun displayDocumentosEnRevision(documentos: List<DocumentoRevisionDocenteModel>) {
        binding.llDocumentosRevisionContainer.removeAllViews() // Clear previous views

        if (documentos.isEmpty()) {
            val tvNoDocumentos = TextView(this).apply {
                text = "No hay documentos pendientes de revisión."
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(0, 16.dpToPx(), 0, 0)
                }
                setTextAppearance(androidx.appcompat.R.style.TextAppearance_AppCompat_Medium)
            }
            binding.llDocumentosRevisionContainer.addView(tvNoDocumentos)
            return
        }

        documentos.forEach { documento ->
            val cardView = LayoutInflater.from(this).inflate(R.layout.item_documento_revision_docente, binding.llDocumentosRevisionContainer, false) as MaterialCardView

            cardView.findViewById<TextView>(R.id.tvNombreDocumentoRevision).text = documento.nombreDocumento
            cardView.findViewById<TextView>(R.id.tvTrabajoModalidadRevision).text = "${documento.nombreTrabajo} (${documento.nombreModalidad})"
            cardView.findViewById<TextView>(R.id.tvEstudianteRevision).text = "${documento.nombreEstudiante} (${documento.carnetEstudiante})"
            cardView.findViewById<TextView>(R.id.tvTipoVersionDocumentoRevision).text = "Tipo: ${documento.tipoDocumento} | Versión: ${documento.versionDocumento}"
            cardView.findViewById<TextView>(R.id.tvEstadoDocumentoRevision).text = "Estado: ${documento.estadoDocumento}"
            cardView.findViewById<TextView>(R.id.tvFechaCargaRevision).text = "Fecha de Carga: ${documento.fechaCarga}"

            cardView.setOnClickListener {
                val intent = Intent(this, DetalleDocumentoDocenteActivity::class.java).apply {
                    putExtra("ID_DOCUMENTO", documento.idDocumento)
                }
                startActivity(intent)
            }
            binding.llDocumentosRevisionContainer.addView(cardView)
        }
    }

    // Extension function to convert dp to pixels
    private fun Int.dpToPx(): Int = (this * resources.displayMetrics.density).toInt()
}