package com.example.gradues.ui.dashboard.admin

import android.content.Context
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
import com.example.gradues.data.model.AlumnoDisponibleModel
import com.example.gradues.databinding.ActivityAsignarEstudiantesAdminBinding

class AsignarEstudiantesAdminActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_ID_TRABAJO      = "extra_id_trabajo"
        const val EXTRA_NOMBRE_SUBGRUPO = "extra_nombre_subgrupo"
    }

    private lateinit var binding: ActivityAsignarEstudiantesAdminBinding
    private lateinit var dao: GestionCursosAdminDao

    private var idTrabajoGraduacion = 0
    private var nombreSubgrupo      = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityAsignarEstudiantesAdminBinding.inflate(layoutInflater)
        setContentView(binding.root)

        idTrabajoGraduacion = intent.getIntExtra(EXTRA_ID_TRABAJO, 0)
        nombreSubgrupo      = intent.getStringExtra(EXTRA_NOMBRE_SUBGRUPO) ?: ""

        dao = GestionCursosAdminDao(DatabaseHelper(this))

        binding.tvTituloSubgrupo.text    = nombreSubgrupo
        binding.tvSubtituloSubgrupo.text = "Máximo 3 estudiantes por subgrupo"
    }

    override fun onResume() {
        super.onResume()
        cargarAlumnos()
    }

    private fun cargarAlumnos() {
        val alumnos = dao.obtenerAlumnosParaSubgrupo(idTrabajoGraduacion)
        val asignados = alumnos.count { it.asignadoEnEsteSubgrupo }

        binding.tvContadorAsignados.text = "$asignados / 3"

        if (alumnos.isEmpty()) {
            binding.tvSinAlumnos.visibility = View.VISIBLE
            binding.lvAlumnos.visibility    = View.GONE
            return
        }

        binding.tvSinAlumnos.visibility = View.GONE
        binding.lvAlumnos.visibility    = View.VISIBLE

        val adapter = AlumnoAdapter(
            context               = this,
            items                 = alumnos,
            totalAsignados        = asignados,
            idTrabajoGraduacion   = idTrabajoGraduacion,
            dao                   = dao,
            onCambioAsignacion    = { cargarAlumnos() }
        )
        binding.lvAlumnos.adapter = adapter
    }

    // ── Adapter ──────────────────────────────────────────────────────────────

    private class AlumnoAdapter(
        context: Context,
        private val items: List<AlumnoDisponibleModel>,
        private var totalAsignados: Int,
        private val idTrabajoGraduacion: Int,
        private val dao: GestionCursosAdminDao,
        private val onCambioAsignacion: () -> Unit
    ) : ArrayAdapter<AlumnoDisponibleModel>(context, 0, items) {

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val view = convertView ?: LayoutInflater.from(context)
                .inflate(R.layout.item_alumno_asignar, parent, false)

            val alumno = items[position]

            view.findViewById<TextView>(R.id.tvNombreAlumno).text = alumno.nombreCompleto
            view.findViewById<TextView>(R.id.tvCarnetAlumno).text =
                "Carnet: ${alumno.carnet.ifBlank { "—" }}"
            view.findViewById<TextView>(R.id.tvCorreoAlumno).text = alumno.correo

            val btnToggle = view.findViewById<Button>(R.id.btnToggleAsignar)

            if (alumno.asignadoEnEsteSubgrupo) {
                // Ya está asignado → mostrar opción de quitar
                btnToggle.text = "Quitar"
                btnToggle.backgroundTintList =
                    android.content.res.ColorStateList.valueOf(
                        android.graphics.Color.parseColor("#757575")
                    )
                btnToggle.setOnClickListener {
                    val ok = dao.desasignarAlumnoDeSubgrupo(alumno.idUsuario, idTrabajoGraduacion)
                    val msg = if (ok) "Estudiante removido del subgrupo"
                    else "Error al quitar al estudiante"
                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                    if (ok) onCambioAsignacion()
                }
            } else {
                // No está asignado → mostrar opción de asignar
                btnToggle.text = "Asignar"
                btnToggle.backgroundTintList =
                    android.content.res.ColorStateList.valueOf(
                        android.graphics.Color.parseColor("#B71C1C")
                    )

                // Deshabilitar si ya hay 3 asignados
                val lleno = totalAsignados >= 3
                btnToggle.isEnabled = !lleno
                btnToggle.alpha     = if (lleno) 0.4f else 1.0f

                btnToggle.setOnClickListener {
                    val resultado = dao.asignarAlumnoASubgrupo(alumno.idUsuario, idTrabajoGraduacion)
                    val msg = when (resultado) {
                        GestionCursosAdminDao.AsignacionResultado.EXITO ->
                            "Estudiante asignado correctamente"
                        GestionCursosAdminDao.AsignacionResultado.LIMITE_ALCANZADO ->
                            "No se puede asignar: el subgrupo ya tiene 3 estudiantes"
                        GestionCursosAdminDao.AsignacionResultado.YA_ASIGNADO ->
                            "El estudiante ya está asignado"
                        GestionCursosAdminDao.AsignacionResultado.ERROR ->
                            "Error al asignar al estudiante"
                    }
                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                    if (resultado == GestionCursosAdminDao.AsignacionResultado.EXITO) {
                        onCambioAsignacion()
                    }
                }
            }

            return view
        }
    }
}
