package com.example.gradues.ui.dashboard.alumno

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.GridLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.gradues.R
import com.example.gradues.data.dao.EspecializacionAlumnoDao
import com.example.gradues.data.db.DatabaseHelper
import com.example.gradues.data.model.DetalleSubgrupoEspecializacionAlumnoModel
import com.example.gradues.data.model.IntegranteSubgrupoAlumnoModel
import com.example.gradues.data.model.JuradoEspecializacionAlumnoModel
import com.example.gradues.data.model.NotaAlumnoModel
import com.example.gradues.data.model.ResumenPropuestasAlumnoModel
import com.example.gradues.databinding.ActivityDetalleSubgrupoEspecializacionBinding
import com.example.gradues.utils.SessionManager
import java.util.Locale

class DetalleSubgrupoEspecializacionActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetalleSubgrupoEspecializacionBinding
    private lateinit var especializacionAlumnoDao: EspecializacionAlumnoDao
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetalleSubgrupoEspecializacionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        especializacionAlumnoDao = EspecializacionAlumnoDao(DatabaseHelper(this))
        sessionManager = SessionManager(this)

        configurarEventos()
    }

    override fun onResume() {
        super.onResume()
        cargarDetalleSubgrupo()
    }

    private fun configurarEventos() {
        binding.btnBackSubgrupoEspecializacion.setOnClickListener { finish() }

        binding.btnVerPropuestasEspecializacion.setOnClickListener {
            startActivity(Intent(this, DetallePropuestasEspecializacionActivity::class.java))
        }

        binding.btnSubirTesinaEspecializacion.setOnClickListener {
            startActivity(Intent(this, TesinaGrupoEspecializacionActivity::class.java))
        }

        binding.cardTesinaGrupoEspecializacion.setOnClickListener {
            startActivity(Intent(this, TesinaGrupoEspecializacionActivity::class.java))
        }

        binding.btnHistorialTesinaEspecializacion.setOnClickListener {
            startActivity(Intent(this, HistorialTesinaEspecializacionActivity::class.java))
        }
        binding.itemInicioSubgrupoEspecializacion.setOnClickListener { finish() }
        binding.itemGrupoSubgrupoEspecializacion.setOnClickListener { mostrarMensaje("Ya estás en Grupo") }
        binding.itemPerfilSubgrupoEspecializacion.setOnClickListener { mostrarMensaje("Perfil pendiente de implementar") }
        binding.btnNotificacionesSubgrupoEspecializacion.setOnClickListener { mostrarMensaje("Notificaciones pendientes de implementar") }
        binding.btnPerfilSubgrupoEspecializacion.setOnClickListener { mostrarMensaje("Perfil pendiente de implementar") }
    }

    private fun cargarDetalleSubgrupo() {
        val idSesion = sessionManager.getIdUsuario().trim()
        if (idSesion.isBlank()) {
            mostrarMensaje("No se encontró la sesión del alumno.")
            return
        }

        val detalle = especializacionAlumnoDao.obtenerDetalleSubgrupoActivo(idSesion)
        if (detalle == null) {
            mostrarEstadoSinDatos()
            return
        }

        renderDetalle(detalle)
    }

    private fun renderDetalle(detalle: DetalleSubgrupoEspecializacionAlumnoModel) {
        binding.tvTituloSubgrupoEspecializacion.text = detalle.nombreSubgrupo
        binding.tvCursoSubgrupoEspecializacion.text = detalle.nombreCurso
        binding.tvDirectorSubgrupoEspecializacion.text = detalle.director?.let {
            "Director: ${it.nombreCompleto}"
        } ?: "Director: no asignado"

        renderIntegrantes(detalle.integrantes)
        renderJurados(detalle.jurados)
        binding.tvResumenPropuestasEspecializacion.text = formatearResumenPropuestas(detalle.resumenPropuestas)

        val tesina = detalle.tesina
        binding.tvTituloTesinaSubgrupoEspecializacion.text = tesina?.tituloDocumento ?: detalle.temaAsignado
        binding.tvEstadoTesinaSubgrupoEspecializacion.text = "Estado: ${tesina?.estadoDocumento ?: "No subida"}"
        binding.tvVersionTesinaSubgrupoEspecializacion.text = "Última versión: ${
            tesina?.versionDocumento?.let { "Versión $it" } ?: "No enviada"
        }"

        renderNotas(detalle.notas)
        binding.tvPromedioAlumnoSubgrupoEspecializacion.text = "Mi Promedio: ${formatearPromedio(detalle.promedioAlumno)}"
        binding.tvPromedioGrupoSubgrupoEspecializacion.text = "Promedio grupal: ${formatearPromedio(detalle.promedioGrupo)}"
    }

    private fun renderIntegrantes(integrantes: List<IntegranteSubgrupoAlumnoModel>) {
        binding.llIntegrantesSubgrupoEspecializacion.removeAllViews()

        if (integrantes.isEmpty()) {
            binding.llIntegrantesSubgrupoEspecializacion.addView(crearTextoSimple("No hay integrantes registrados."))
            return
        }

        integrantes.forEach { integrante ->
            val row = layoutInflater.inflate(
                R.layout.row_integrante_especializacion,
                binding.llIntegrantesSubgrupoEspecializacion,
                false
            )

            row.findViewById<TextView>(R.id.tvNombreIntegranteEspecializacion).text = integrante.nombreCompleto
            row.findViewById<TextView>(R.id.tvCarnetIntegranteEspecializacion).text = integrante.carnetUsuario
            row.findViewById<View>(R.id.btnInfoIntegranteEspecializacion).setOnClickListener {
                mostrarMensaje("${integrante.nombreCompleto} (${integrante.carnetUsuario})")
            }

            binding.llIntegrantesSubgrupoEspecializacion.addView(row)
        }
    }

    private fun renderJurados(jurados: List<JuradoEspecializacionAlumnoModel>) {
        binding.llJuradosSubgrupoEspecializacion.removeAllViews()

        if (jurados.isEmpty()) {
            binding.llJuradosSubgrupoEspecializacion.addView(crearTextoSimple("Aún no hay jurados asignados."))
            return
        }

        jurados.forEach { jurado ->
            val fecha = jurado.fechaAsignacion.ifBlank { "Sin fecha de asignación" }
            binding.llJuradosSubgrupoEspecializacion.addView(
                crearTextoSimple("${jurado.nombreCompleto} • ${jurado.codigoUsuario} • $fecha")
            )
        }
    }

    private fun renderNotas(notas: List<NotaAlumnoModel>) {
        binding.gridNotasSubgrupoEspecializacion.removeAllViews()

        if (notas.isEmpty()) {
            binding.gridNotasSubgrupoEspecializacion.addView(
                crearBadgeNota("Sin notas registradas", destacada = false)
            )
            return
        }

        notas.forEach { nota ->
            val textoNota = nota.nota?.let { String.format(Locale.US, "%.2f", it) } ?: "--"
            binding.gridNotasSubgrupoEspecializacion.addView(
                crearBadgeNota("Etapa ${nota.numeroEtapa}: $textoNota", destacada = nota.nota != null)
            )
        }
    }

    private fun crearTextoSimple(texto: String): TextView {
        return TextView(this).apply {
            this.text = texto
            setTextColor(0xFF555555.toInt())
            textSize = 15f
            setPadding(0, 10, 0, 0)
        }
    }

    private fun crearBadgeNota(texto: String, destacada: Boolean): TextView {
        val params = GridLayout.LayoutParams().apply {
            width = 0
            height = GridLayout.LayoutParams.WRAP_CONTENT
            columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
            setMargins(5, 5, 5, 5)
        }

        return TextView(this).apply {
            text = texto
            layoutParams = params
            gravity = android.view.Gravity.CENTER
            setPadding(10, 10, 10, 10)
            setTextColor(if (destacada) 0xFFFFFFFF.toInt() else 0xFF222222.toInt())
            setTypeface(typeface, android.graphics.Typeface.BOLD)
            setBackgroundResource(if (destacada) R.drawable.bg_badge_green else R.drawable.bg_badge_gray)
        }
    }

    private fun formatearResumenPropuestas(resumen: ResumenPropuestasAlumnoModel): String {
        if (resumen.total == 0) return "Sin propuestas registradas."

        val partes = listOf(
            resumen.pendientes to "pendiente(s)",
            resumen.aprobadas to "aprobada(s)",
            resumen.denegadas to "denegada(s)",
            resumen.borradores to "borrador(es)",
            resumen.seleccionadas to "seleccionada(s)",
            resumen.descartadas to "descartada(s)",
            resumen.conObservacion to "con observación"
        ).filter { it.first > 0 }
            .joinToString(", ") { "${it.first} ${it.second}" }

        return if (partes.isBlank()) {
            "${resumen.total} propuesta(s) registradas."
        } else {
            "${resumen.total} propuesta(s): $partes."
        }
    }

    private fun formatearPromedio(promedio: Double?): String {
        return promedio?.let { String.format(Locale.US, "%.2f", it) } ?: "--"
    }

    private fun mostrarEstadoSinDatos() {
        binding.tvTituloSubgrupoEspecializacion.text = "Sin subgrupo"
        binding.tvCursoSubgrupoEspecializacion.text = "Curso no disponible"
        binding.tvDirectorSubgrupoEspecializacion.text = "Director: no asignado"
        binding.llIntegrantesSubgrupoEspecializacion.removeAllViews()
        binding.llIntegrantesSubgrupoEspecializacion.addView(
            crearTextoSimple("No se encontró un subgrupo de especialización activo para tu sesión.")
        )
        binding.llJuradosSubgrupoEspecializacion.removeAllViews()
        binding.llJuradosSubgrupoEspecializacion.addView(crearTextoSimple("Aún no hay jurados asignados."))
        binding.tvResumenPropuestasEspecializacion.text = "Sin propuestas registradas."
        binding.tvTituloTesinaSubgrupoEspecializacion.text = "Tesina del grupo"
        binding.tvEstadoTesinaSubgrupoEspecializacion.text = "Estado: No subida"
        binding.tvVersionTesinaSubgrupoEspecializacion.text = "Última versión: No enviada"
        renderNotas(emptyList())
        binding.tvPromedioAlumnoSubgrupoEspecializacion.text = "Mi Promedio: --"
        binding.tvPromedioGrupoSubgrupoEspecializacion.text = "Promedio grupal: --"
    }

    private fun mostrarMensaje(mensaje: String) {
        Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show()
    }
}
