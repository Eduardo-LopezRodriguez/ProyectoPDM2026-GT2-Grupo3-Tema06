package com.example.gradues.ui.dashboard.admin

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.gradues.data.dao.DetalleSolicitudAdminDao
import com.example.gradues.data.db.DatabaseHelper
import com.example.gradues.data.model.DetalleSolicitudAdminModel
import com.example.gradues.databinding.ActivityDetalleSolicitudAdminBinding
import android.widget.EditText
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.example.gradues.data.dao.ResultadoOperacionAdmin

class DetalleSolicitudAdminActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetalleSolicitudAdminBinding
    private lateinit var detalleDao: DetalleSolicitudAdminDao
    private var detalleActual: DetalleSolicitudAdminModel? = null

    private var idReferenciaSolicitud: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityDetalleSolicitudAdminBinding.inflate(layoutInflater)
        setContentView(binding.root)

        detalleDao = DetalleSolicitudAdminDao(DatabaseHelper(this))
        idReferenciaSolicitud = intent.getIntExtra("idReferenciaSolicitud", -1)

        configurarEventos()
        cargarDetalle()
    }

    private fun configurarEventos() {
        binding.btnVolver.setOnClickListener {
            finish()
        }

        binding.btnAprobar.setOnClickListener {
            mostrarDialogoAprobacion()
        }

        binding.btnRechazar.setOnClickListener {
            mostrarDialogoRechazo()
        }
    }

    private fun cargarDetalle() {
        if (idReferenciaSolicitud <= 0) {
            Toast.makeText(
                this,
                "No se recibió la solicitud seleccionada.",
                Toast.LENGTH_LONG
            ).show()
            finish()
            return
        }

        val detalle = detalleDao.obtenerDetalleSolicitud(idReferenciaSolicitud)

        if (detalle == null) {
            Toast.makeText(
                this,
                "No se encontró el detalle de la solicitud.",
                Toast.LENGTH_LONG
            ).show()
            finish()
            return
        }

        detalleActual = detalle
        pintarDetalle(detalle)
    }

    private fun pintarDetalle(detalle: DetalleSolicitudAdminModel) {
        binding.tvCodigoModalidad.text =
            "${detalle.codigoAgrupacionSolicitud} - ${detalle.modalidad}"

        binding.tvEstado.text = "Estado: ${detalle.estadoSolicitud}"
        binding.tvFecha.text = "Fecha: ${detalle.fechaSolicitud}"

        binding.tvTrabajoPropuesto.text =
            "Trabajo propuesto:\n${detalle.nombreTrabajoPropuesto}"

        if (!detalle.cursoSolicitado.isNullOrBlank()) {
            binding.tvCurso.visibility = View.VISIBLE
            binding.tvCurso.text = "Curso solicitado:\n${detalle.cursoSolicitado}"
        } else {
            binding.tvCurso.visibility = View.GONE
        }

        if (!detalle.empresaSolicitada.isNullOrBlank()) {
            binding.tvEmpresa.visibility = View.VISIBLE
            binding.tvEmpresa.text = "Empresa solicitada:\n${detalle.empresaSolicitada}"
        } else {
            binding.tvEmpresa.visibility = View.GONE
        }

        binding.tvTotalSolicitantes.text =
            "Total solicitantes: ${detalle.totalSolicitantes}"

        binding.tvSolicitantes.text =
            "Solicitantes:\n${detalle.solicitantes}"

        binding.tvObservacion.text =
            "Observación:\n${detalle.observacionSolicitud ?: "Sin observación"}"

        val estaPendiente = detalle.estadoSolicitud.equals("Pendiente", ignoreCase = true)

        binding.btnAprobar.isEnabled = estaPendiente
        binding.btnRechazar.isEnabled = estaPendiente

        if (!estaPendiente) {
            binding.btnAprobar.alpha = 0.5f
            binding.btnRechazar.alpha = 0.5f
        } else {
            binding.btnAprobar.alpha = 1f
            binding.btnRechazar.alpha = 1f
        }
    }

    private fun mostrarDialogoAprobacion() {
        val detalle = detalleActual

        if (detalle == null) {
            Toast.makeText(this, "No hay detalle cargado.", Toast.LENGTH_LONG).show()
            return
        }

        if (!detalle.estadoSolicitud.equals("Pendiente", ignoreCase = true)) {
            Toast.makeText(this, "Solo se pueden aprobar solicitudes pendientes.", Toast.LENGTH_LONG).show()
            return
        }

        MaterialAlertDialogBuilder(this)
            .setTitle("Aprobar solicitud")
            .setMessage(
                """
            ¿Deseas aprobar esta solicitud?
            
            Al aprobar, se creará el trabajo de graduación, la asignación de alumnos y el registro correspondiente según la modalidad.
            """.trimIndent()
            )
            .setPositiveButton("Aprobar") { _, _ ->
                aprobarSolicitud()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun aprobarSolicitud() {
        val exito = detalleDao.aprobarSolicitudAgrupada(idReferenciaSolicitud)

        if (exito) {
            Toast.makeText(
                this,
                "Solicitud aprobada correctamente.",
                Toast.LENGTH_LONG
            ).show()

            setResult(RESULT_OK)
            finish()
        } else {
            Toast.makeText(
                this,
                "No se pudo aprobar: ${ResultadoOperacionAdmin.ultimoError}",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun mostrarDialogoRechazo() {
        val detalle = detalleActual

        if (detalle == null) {
            Toast.makeText(this, "No hay detalle cargado.", Toast.LENGTH_LONG).show()
            return
        }

        if (!detalle.estadoSolicitud.equals("Pendiente", ignoreCase = true)) {
            Toast.makeText(this, "Solo se pueden rechazar solicitudes pendientes.", Toast.LENGTH_LONG).show()
            return
        }

        val input = EditText(this).apply {
            hint = "Escriba una observación"
            minLines = 2
            setPadding(32, 16, 32, 16)
        }

        MaterialAlertDialogBuilder(this)
            .setTitle("Rechazar solicitud")
            .setMessage("Ingrese una observación para el rechazo.")
            .setView(input)
            .setPositiveButton("Rechazar") { _, _ ->
                rechazarSolicitud(input.text?.toString().orEmpty())
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun rechazarSolicitud(observacion: String) {
        val exito = detalleDao.rechazarSolicitudAgrupada(
            idReferenciaSolicitud = idReferenciaSolicitud,
            observacion = observacion
        )

        if (exito) {
            Toast.makeText(
                this,
                "Solicitud rechazada correctamente.",
                Toast.LENGTH_LONG
            ).show()

            setResult(RESULT_OK)
            finish()
        } else {
            Toast.makeText(
                this,
                "No se pudo rechazar: ${ResultadoOperacionAdmin.ultimoError}",
                Toast.LENGTH_LONG
            ).show()
        }
    }
}