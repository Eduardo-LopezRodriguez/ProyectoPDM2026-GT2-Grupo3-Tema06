package com.example.gradues.ui.dashboard.admin

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.gradues.data.dao.DetalleSolicitudAdminDao
import com.example.gradues.data.db.DatabaseHelper
import com.example.gradues.data.model.DetalleSolicitudAdminModel
import com.example.gradues.databinding.ActivityDetalleSolicitudAdminBinding

class DetalleSolicitudAdminActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetalleSolicitudAdminBinding
    private lateinit var detalleDao: DetalleSolicitudAdminDao

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
            Toast.makeText(
                this,
                "La aprobación se implementará en el siguiente bloque.",
                Toast.LENGTH_LONG
            ).show()
        }

        binding.btnRechazar.setOnClickListener {
            Toast.makeText(
                this,
                "El rechazo se implementará en el siguiente bloque.",
                Toast.LENGTH_LONG
            ).show()
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
}