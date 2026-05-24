// NotasSubgrupoDocenteActivity.kt
package com.example.gradues.ui.dashboard.docente

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup // Added import
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.gradues.R
import com.example.gradues.data.dao.CursoEspecializacionDocenteDao
import com.example.gradues.data.db.DatabaseHelper
import com.example.gradues.data.model.EstudianteSubgrupoModel
import com.example.gradues.data.model.NotaEtapaDocenteModel
import com.example.gradues.databinding.ActivityNotasSubgrupoDocenteBinding
import com.google.android.material.card.MaterialCardView
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class NotasSubgrupoDocenteActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNotasSubgrupoDocenteBinding
    private lateinit var cursoEspecializacionDocenteDao: CursoEspecializacionDocenteDao

    private var idTrabajoGraduacion: String? = null
    private var nombreSubgrupo: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNotasSubgrupoDocenteBinding.inflate(layoutInflater)
        setContentView(binding.root)

        cursoEspecializacionDocenteDao = CursoEspecializacionDocenteDao(DatabaseHelper(this))

        idTrabajoGraduacion = intent.getStringExtra("ID_TRABAJO_GRADUACION")
        nombreSubgrupo = intent.getStringExtra("NOMBRE_SUBGRUPO")

        if (idTrabajoGraduacion.isNullOrBlank()) {
            Toast.makeText(this, "Error: ID de trabajo de graduación no encontrado.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        binding.tvTituloNotasSubgrupo.text = "Notas de Subgrupo: ${nombreSubgrupo ?: "N/A"}"

        configurarEventos()
        cargarEstudiantesConNotas(idTrabajoGraduacion!!)
    }

    private fun configurarEventos() {
        binding.btnBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun cargarEstudiantesConNotas(idTrabajo: String) {
        val estudiantes = cursoEspecializacionDocenteDao.obtenerEstudiantesPorSubgrupo(idTrabajo)
        displayEstudiantesConNotas(estudiantes)
    }

    private fun displayEstudiantesConNotas(estudiantes: List<EstudianteSubgrupoModel>) {
        binding.llEstudiantesContainer.removeAllViews() // Clear previous views

        if (estudiantes.isEmpty()) {
            val tvNoEstudiantes = TextView(this).apply {
                text = "No hay estudiantes en este subgrupo."
                layoutParams = (LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ) as android.view.ViewGroup.MarginLayoutParams).apply {
                    setMargins(0, 16.dpToPx(), 0, 0)
                }
                setTextAppearance(androidx.appcompat.R.style.TextAppearance_AppCompat_Medium)
            }
            binding.llEstudiantesContainer.addView(tvNoEstudiantes)
            return
        }

        estudiantes.forEach { estudiante ->
            val estudianteCardView = LayoutInflater.from(this).inflate(R.layout.item_estudiante_subgrupo_docente, binding.llEstudiantesContainer, false) as MaterialCardView

            estudianteCardView.findViewById<TextView>(R.id.tvNombreEstudianteNotas).text = estudiante.nombreCompleto
            estudianteCardView.findViewById<TextView>(R.id.tvCarnetEstudianteNotas).text = "Carnet: ${estudiante.carnetUsuario}"
            estudianteCardView.findViewById<TextView>(R.id.tvPromedioEstudianteNotas).text = "Promedio: ${estudiante.promedioNotas}"

            // Container for this student's grades
            val notesContainer = LinearLayout(this).apply {
                layoutParams = (LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ) as android.view.ViewGroup.MarginLayoutParams).apply {
                    orientation = LinearLayout.VERTICAL
                    setPadding(0, 16.dpToPx(), 0, 0)
                }
            }

            // Find the intended parent layout within the card
            val parentLayout = estudianteCardView.findViewById<LinearLayout>(R.id.llEstudianteNotasContainer)
            // Add the created notesContainer to the parent layout, or to the card itself if the parent is not found
            if (parentLayout != null) {
                parentLayout.addView(notesContainer)
            } else {
                estudianteCardView.addView(notesContainer)
            }

            cargarNotasEstudiante(estudiante.idUsuario, estudiante.idTrabajoGraduacion, notesContainer)

            // Add button to add/edit notes for this student
            val btnGestionarNotas = Button(this).apply {
                text = "Gestionar Notas"
                layoutParams = (LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT) as android.view.ViewGroup.MarginLayoutParams).apply {
                    setMargins(0, 16.dpToPx(), 0, 0)
                }
                    // Use setBackgroundTintList for compatibility
                setBackgroundTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#6200EE")))
                setTextColor(resources.getColor(R.color.white))
            }

            btnGestionarNotas.setOnClickListener {
                mostrarDialogoGestionarNotas(estudiante.idUsuario, estudiante.nombreCompleto, estudiante.idTrabajoGraduacion) // Pass student ID and name
            }
            notesContainer.addView(btnGestionarNotas)

            binding.llEstudiantesContainer.addView(estudianteCardView)
        }
    }

    private fun cargarNotasEstudiante(idUsuario: String, idTrabajoGraduacion: String, parentLayout: LinearLayout) {
        parentLayout.removeAllViews() // Clear previous notes

        val notas = cursoEspecializacionDocenteDao.obtenerNotasEstudiante(idUsuario, idTrabajoGraduacion)

        if (notas.isEmpty()) {
            val tvNoNotas = TextView(this).apply {
                text = "No hay notas registradas."
                layoutParams = (LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT) as android.view.ViewGroup.MarginLayoutParams).apply {
                    setMargins(0, 8.dpToPx(), 0, 0)
                }
                setTextAppearance(androidx.appcompat.R.style.TextAppearance_AppCompat_Small)
            }
            parentLayout.addView(tvNoNotas)
            return
        }

        notas.forEach { nota ->
            val notaCardView = LayoutInflater.from(this).inflate(R.layout.item_nota_etapa_docente, parentLayout, false) as MaterialCardView

            notaCardView.findViewById<TextView>(R.id.tvNombreEtapaNota).text = nota.nombreEtapa
            notaCardView.findViewById<TextView>(R.id.tvNotaValor).text = "Nota: ${nota.nota?.let { String.format(" %.2f", it) } ?: "N/A"}"
            notaCardView.findViewById<TextView>(R.id.tvNotaObservacion).text = "Observación: ${nota.observacion ?: "Sin observaciones."}"
            notaCardView.findViewById<TextView>(R.id.tvNotaFechaRegistro).text = "Fecha de Registro: ${nota.fechaRegistro}"

            notaCardView.setOnClickListener {
                // Allow editing a note by passing its details to the dialog
                mostrarDialogoGestionarNotas(idUsuario, null, idTrabajoGraduacion, nota) // Pass existing note to pre-fill dialog
            }
            parentLayout.addView(notaCardView)
        }
    }

    private fun mostrarDialogoGestionarNotas(idUsuario: String, nombreEstudiante: String?, idTrabajoGraduacion: String, existingNota: NotaEtapaDocenteModel? = null) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_gestionar_nota, null)
        val spinnerEtapa: Spinner = dialogView.findViewById(R.id.spinnerEtapa)
        val etNota: EditText = dialogView.findViewById(R.id.etNota)
        val etObservacion: EditText = dialogView.findViewById(R.id.etObservacion)
        val tvDialogTitle: TextView = dialogView.findViewById(R.id.tvDialogTitle)

        tvDialogTitle.text = if (existingNota == null) "Agregar Nota para ${nombreEstudiante ?: "Estudiante"}" else "Editar Nota: ${existingNota.nombreEtapa}"

        // Populate spinner with stages
        val etapas = CursoEspecializacionDocenteDao.STAGE_NAME_TO_ID.keys.toList()
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, etapas)
        spinnerEtapa.adapter = adapter

        // Pre-fill if editing existing note
        existingNota?.let {
            etNota.setText(it.nota?.toString() ?: "")
            etObservacion.setText(it.observacion ?: "")
            val etapaIndex = etapas.indexOf(it.nombreEtapa)
            if (etapaIndex != -1) {
                spinnerEtapa.setSelection(etapaIndex)
            }
        }

        MaterialAlertDialogBuilder(this)
            .setView(dialogView)
            .setPositiveButton(if (existingNota == null) "Guardar" else "Actualizar") { dialog, _ ->
                val selectedEtapa = spinnerEtapa.selectedItem.toString()
                val notaText = etNota.text.toString()
                val observacion = etObservacion.text.toString().ifBlank { null }

                if (notaText.isBlank()) {
                    Toast.makeText(this, "La nota no puede estar vacía.", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                val nota = notaText.toDoubleOrNull()
                if (nota == null || nota < 0 || nota > 10) {
                    Toast.makeText(this, "La nota debe ser un número entre 0 y 10.", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                val success = cursoEspecializacionDocenteDao.crearOActualizarNotaConEtapaNombre(
                    idNotaEtapa = existingNota?.idNotaEtapa,
                    idTrabajoGraduacion = idTrabajoGraduacion,
                    idUsuario = idUsuario,
                    nombreEtapa = selectedEtapa,
                    nota = nota,
                    observacion = observacion
                )

                if (success) {
                    Toast.makeText(this, "Nota guardada exitosamente.", Toast.LENGTH_SHORT).show()
                    cargarEstudiantesConNotas(idTrabajoGraduacion!!) // Reload all students and their notes
                } else {
                    Toast.makeText(this, "Error al guardar la nota. Verifique la etapa.", Toast.LENGTH_SHORT).show()
                }
                dialog.dismiss()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    // Extension function to convert dp to pixels
    private fun Int.dpToPx(): Int = (this * resources.displayMetrics.density).toInt()
}