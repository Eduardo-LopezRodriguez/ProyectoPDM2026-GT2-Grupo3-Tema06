package com.example.gradues.ui.dashboard.admin

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.gradues.R
import com.example.gradues.data.dao.GestionCursosAdminDao
import com.example.gradues.data.db.DatabaseHelper
import com.example.gradues.data.model.DocenteDisponibleModel
import com.example.gradues.data.model.SubgrupoTgeModel
import com.example.gradues.databinding.ActivitySubgruposCursoAdminBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class SubgruposCursoAdminActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_ID_GRUPO     = "extra_id_grupo"
        const val EXTRA_NOMBRE_CURSO = "extra_nombre_curso"
        const val EXTRA_CICLO        = "extra_ciclo"
    }

    private lateinit var binding: ActivitySubgruposCursoAdminBinding
    private lateinit var dao: GestionCursosAdminDao

    private var idGrupoTGE  = 0
    private var nombreCurso = ""
    private var ciclo       = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySubgruposCursoAdminBinding.inflate(layoutInflater)
        setContentView(binding.root)

        idGrupoTGE  = intent.getIntExtra(EXTRA_ID_GRUPO, 0)
        nombreCurso = intent.getStringExtra(EXTRA_NOMBRE_CURSO) ?: ""
        ciclo       = intent.getStringExtra(EXTRA_CICLO) ?: ""

        dao = GestionCursosAdminDao(DatabaseHelper(this))

        binding.tvTituloCurso.text       = nombreCurso
        binding.tvCicloCursoDetalle.text = ciclo

        configurarEventos()
    }

    override fun onResume() {
        super.onResume()
        cargarSubgrupos()
    }

    private fun configurarEventos() {
        binding.btnCrearSubgrupo.setOnClickListener {
            val intent = Intent(this, CrearSubgrupoAdminActivity::class.java).apply {
                putExtra(CrearSubgrupoAdminActivity.EXTRA_ID_GRUPO, idGrupoTGE)
                putExtra(CrearSubgrupoAdminActivity.EXTRA_NOMBRE_CURSO, nombreCurso)
                putExtra(CrearSubgrupoAdminActivity.EXTRA_CICLO, ciclo)
            }
            startActivity(intent)
        }
    }

    private fun cargarSubgrupos() {
        val subgrupos = dao.obtenerSubgruposPorCurso(idGrupoTGE)

        if (subgrupos.isEmpty()) {
            binding.tvSinSubgrupos.visibility = View.VISIBLE
            binding.lvSubgrupos.visibility    = View.GONE
            return
        }

        binding.tvSinSubgrupos.visibility = View.GONE
        binding.lvSubgrupos.visibility    = View.VISIBLE

        val adapter = SubgrupoAdapter(
            context         = this,
            items           = subgrupos,
            onAsignarAlumnos = { subgrupo ->
                val idTrabajo = subgrupo.idTrabajoGraduacion ?: return@SubgrupoAdapter
                val intent = Intent(this, AsignarEstudiantesAdminActivity::class.java).apply {
                    putExtra(AsignarEstudiantesAdminActivity.EXTRA_ID_TRABAJO, idTrabajo)
                    putExtra(AsignarEstudiantesAdminActivity.EXTRA_NOMBRE_SUBGRUPO, subgrupo.nombreSubgrupo)
                }
                startActivity(intent)
            },
            onAsignarDocente = { subgrupo ->
                val idTrabajo = subgrupo.idTrabajoGraduacion ?: return@SubgrupoAdapter
                mostrarDialogoSeleccionDocente(idTrabajo, subgrupo.nombreSubgrupo)
            }
        )
        binding.lvSubgrupos.adapter = adapter
    }

    private fun mostrarDialogoSeleccionDocente(idTrabajoGraduacion: Int, nombreSubgrupo: String) {
        val docentes = dao.obtenerDocentes()

        if (docentes.isEmpty()) {
            Toast.makeText(this, "No hay docentes activos disponibles.", Toast.LENGTH_SHORT).show()
            return
        }

        val nombres = docentes.map { it.nombreCompleto }.toTypedArray()

        MaterialAlertDialogBuilder(this)
            .setTitle("Asignar docente a\n$nombreSubgrupo")
            .setItems(nombres) { _, index ->
                val docenteSeleccionado = docentes[index]
                val exitoso = dao.asignarDocenteASubgrupo(idTrabajoGraduacion, docenteSeleccionado.idUsuario)

                if (exitoso) {
                    Toast.makeText(
                        this,
                        "Docente asignado: ${docenteSeleccionado.nombreCompleto}",
                        Toast.LENGTH_SHORT
                    ).show()
                    cargarSubgrupos()
                } else {
                    Toast.makeText(
                        this,
                        "Error al asignar el docente. Intente de nuevo.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    // ── Adapter ──────────────────────────────────────────────────────────────

    private class SubgrupoAdapter(
        context: Context,
        private val items: List<SubgrupoTgeModel>,
        private val onAsignarAlumnos: (SubgrupoTgeModel) -> Unit,
        private val onAsignarDocente: (SubgrupoTgeModel) -> Unit
    ) : ArrayAdapter<SubgrupoTgeModel>(context, 0, items) {

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val view = convertView ?: LayoutInflater.from(context)
                .inflate(R.layout.item_subgrupo_admin, parent, false)

            val sub = items[position]

            view.findViewById<TextView>(R.id.tvNombreSubgrupo).text = sub.nombreSubgrupo
            view.findViewById<TextView>(R.id.tvAlumnosSubgrupo).text = "${sub.totalAlumnos}/3"
            view.findViewById<TextView>(R.id.tvTemaSubgrupo).text =
                "Tema: ${sub.temaAsignado ?: "—"}"
            view.findViewById<TextView>(R.id.tvDocenteSubgrupo).text =
                "Docente: ${sub.nombreDocente?.ifBlank { "—" } ?: "—"}"

            view.findViewById<Button>(R.id.btnAsignarEstudiantes).setOnClickListener {
                onAsignarAlumnos(sub)
            }

            view.findViewById<Button>(R.id.btnAsignarDocente).setOnClickListener {
                onAsignarDocente(sub)
            }

            return view
        }
    }
}