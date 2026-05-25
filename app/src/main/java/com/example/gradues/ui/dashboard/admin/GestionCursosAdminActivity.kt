package com.example.gradues.ui.dashboard.admin

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.gradues.R
import com.example.gradues.data.dao.GestionCursosAdminDao
import com.example.gradues.data.db.DatabaseHelper
import com.example.gradues.data.model.GrupoTgeModel
import com.example.gradues.databinding.ActivityGestionCursosAdminBinding

class GestionCursosAdminActivity : AppCompatActivity() {

    private lateinit var binding: ActivityGestionCursosAdminBinding
    private lateinit var dao: GestionCursosAdminDao
    private lateinit var adapter: CursoAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityGestionCursosAdminBinding.inflate(layoutInflater)
        setContentView(binding.root)

        dao = GestionCursosAdminDao(DatabaseHelper(this))

        configurarEventos()
    }

    override fun onResume() {
        super.onResume()
        cargarCursos()
    }

    private fun configurarEventos() {
        binding.btnCrearCurso.setOnClickListener {
            startActivity(Intent(this, CrearCursoAdminActivity::class.java))
        }
    }

    private fun cargarCursos() {
        val cursos = dao.obtenerTodosLosCursos()

        if (cursos.isEmpty()) {
            binding.tvSinCursos.visibility = View.VISIBLE
            binding.lvCursos.visibility = View.GONE
            return
        }

        binding.tvSinCursos.visibility = View.GONE
        binding.lvCursos.visibility = View.VISIBLE

        adapter = CursoAdapter(this, cursos) { curso ->
            val intent = Intent(this, SubgruposCursoAdminActivity::class.java).apply {
                putExtra(SubgruposCursoAdminActivity.EXTRA_ID_GRUPO, curso.idGrupoTGE)
                putExtra(SubgruposCursoAdminActivity.EXTRA_NOMBRE_CURSO, curso.nombreCurso)
                putExtra(SubgruposCursoAdminActivity.EXTRA_CICLO, curso.cicloAcademico)
            }
            startActivity(intent)
        }
        binding.lvCursos.adapter = adapter
    }

    // ── Adapter ──────────────────────────────────────────────────────────────

    private class CursoAdapter(
        context: Context,
        private val items: List<GrupoTgeModel>,
        private val onVerSubgrupos: (GrupoTgeModel) -> Unit
    ) : ArrayAdapter<GrupoTgeModel>(context, 0, items) {

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val view = convertView ?: LayoutInflater.from(context)
                .inflate(R.layout.item_curso_admin, parent, false)

            val curso = items[position]

            view.findViewById<TextView>(R.id.tvNombreCurso).text = curso.nombreCurso
            view.findViewById<TextView>(R.id.tvCicloCurso).text = curso.cicloAcademico
            view.findViewById<TextView>(R.id.tvCupoCurso).text = "Cupo: ${curso.cupoMaximo}"
            view.findViewById<TextView>(R.id.tvSubgruposCurso).text =
                "${curso.totalSubgrupos} subgrupo${if (curso.totalSubgrupos != 1) "s" else ""}"

            view.findViewById<Button>(R.id.btnVerSubgrupos).setOnClickListener {
                onVerSubgrupos(curso)
            }

            return view
        }
    }
}
