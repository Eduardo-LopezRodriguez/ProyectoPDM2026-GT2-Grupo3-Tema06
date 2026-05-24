package com.example.gradues.ui.dashboard.alumno

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.gradues.data.dao.DashboardAlumnoDao
import com.example.gradues.data.db.DatabaseHelper
import com.example.gradues.databinding.ActivityDashboardAlumnoBinding
import com.example.gradues.ui.login.LoginActivity
import com.example.gradues.utils.SessionManager
import com.example.gradues.ui.dashboard.alumno.AlumnoGrupoDetalleActivity
import com.example.gradues.ui.dashboard.alumno.AplicarTrabajoAlumnoActivity
import com.example.gradues.ui.dashboard.alumno.DetalleSolicitudAlumnoActivity

class DashboardAlumnoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDashboardAlumnoBinding
    private lateinit var dashboardAlumnoDao: DashboardAlumnoDao
    private lateinit var sessionManager: SessionManager
    private var tipoDashboardActual: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardAlumnoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        dashboardAlumnoDao = DashboardAlumnoDao(DatabaseHelper(this))
        sessionManager = SessionManager(this)

        validarSesion()
        configurarEventosBase()
        cargarDashboard()
    }

    private fun validarSesion() {
        if (!sessionManager.estaLogueado()) {
            irAlLogin()
            return
        }

        val rol = sessionManager.getRol()?.trim()?.lowercase().orEmpty()
        if (rol != "alumno") {
            Toast.makeText(this, "Acceso no autorizado.", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun configurarEventosBase() {
        binding.btnCerrarSesionAlumno.setOnClickListener {
            sessionManager.cerrarSesion()
            irAlLogin()
        }

        binding.btnMenuAlumno.setOnClickListener {
            Toast.makeText(this, "Menú pendiente de implementar.", Toast.LENGTH_SHORT).show()
        }

        binding.btnNotificacionesAlumno.setOnClickListener {
            Toast.makeText(this, "Notificaciones pendientes de implementar.", Toast.LENGTH_SHORT).show()
        }

        binding.btnPerfilAlumno.setOnClickListener {
            Toast.makeText(this, "Perfil pendiente de implementar.", Toast.LENGTH_SHORT).show()
        }

        binding.itemInicioAlumno.setOnClickListener {
            Toast.makeText(this, "Ya estás en inicio.", Toast.LENGTH_SHORT).show()
        }

        binding.itemGrupoAlumno.setOnClickListener {
            when (tipoDashboardActual) {
                "INVESTIGACION" -> {
                    val intent = Intent(this, AlumnoGrupoDetalleActivity::class.java)
                    startActivity(intent)
                }
                "ESPECIALIZACION" -> {
                    Toast.makeText(this, "Detalle de grupo de especialización pendiente.", Toast.LENGTH_SHORT).show()
                }
                "PASANTIA" -> {
                    val intent = Intent(this, AlumnoPasantiaDetalleActivity::class.java)
                    startActivity(intent)
                }
                else -> {
                    Toast.makeText(this, "Aún no tienes un grupo asignado.", Toast.LENGTH_SHORT).show()
                }
            }
        }

        binding.itemPerfilAlumno.setOnClickListener {
            Toast.makeText(this, "Perfil pendiente de implementar.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun cargarDashboard() {
        val idSesion = sessionManager.getIdUsuario()?.trim().orEmpty()

        if (idSesion.isEmpty()) {
            Toast.makeText(this, "No se encontró la sesión del alumno.", Toast.LENGTH_SHORT).show()
            irAlLogin()
            return
        }

        val dashboard = dashboardAlumnoDao.obtenerDashboardAlumno(idSesion)

        if (dashboard == null) {
            Toast.makeText(this, "No se encontró información del alumno.", Toast.LENGTH_SHORT).show()
            return
        }

        tipoDashboardActual = dashboard.tipoDashboard

        binding.tvSaludoAlumno.text = "¡Hola, ${dashboard.nombreCompleto}!"
        binding.tvCorreoAlumno.text = dashboard.correoUsuario.ifBlank { dashboard.carnetUsuario }

        binding.tvTituloSeccionPrincipal.text = dashboard.tituloSeccionPrincipal
        binding.tvTituloTarjetaPrincipal.text = dashboard.tituloTarjetaPrincipal
        binding.tvSubtituloTarjetaPrincipal.text = dashboard.subtituloTarjetaPrincipal
        binding.btnAccionPrincipalAlumno.text = dashboard.textoBotonPrincipal

        binding.tvTituloBloqueSecundario.text = dashboard.tituloBloqueSecundario
        binding.tvDescripcionBloqueSecundario.text = dashboard.descripcionBloqueSecundario
        binding.tvEstadoBloqueSecundario.text = dashboard.estadoBloqueSecundario
        binding.btnDetalleBloqueSecundario.text = dashboard.textoBotonSecundario

        binding.tvNotaEtapa1Alumno.text = "Etapa 1: ${dashboard.notaEtapa1}"
        binding.tvNotaEtapa2Alumno.text = "Etapa 2: ${dashboard.notaEtapa2}"
        binding.tvNotaEtapa3Alumno.text = "Etapa 3: ${dashboard.notaEtapa3}"
        binding.tvNotaEtapa4Alumno.text = "Etapa 4: ${dashboard.notaEtapa4}"

        binding.btnAccionInferiorAlumno.text = dashboard.textoBotonAccionInferior

        binding.cardBloqueSecundarioAlumno.visibility =
            if (dashboard.mostrarBloquePropuestas) View.VISIBLE else View.GONE

        binding.cardNotasAlumno.visibility =
            if (dashboard.mostrarBloqueNotas) View.VISIBLE else View.GONE

        configurarEventosSegunDashboard(dashboard.tipoDashboard)
    }

    private fun configurarEventosSegunDashboard(tipoDashboard: String) {
        binding.btnAccionPrincipalAlumno.setOnClickListener {
            when (tipoDashboard) {
                "INVESTIGACION" -> {
                    val intent = Intent(this, AlumnoGrupoDetalleActivity::class.java)
                    startActivity(intent)
                }
                "ESPECIALIZACION" -> {
                    Toast.makeText(this, "Abrir detalle del grupo de especialización.", Toast.LENGTH_SHORT).show()
                }
                "PASANTIA" -> {
                    val intent = Intent(this, AlumnoPasantiaDetalleActivity::class.java)
                    startActivity(intent)
                }
                "SIN_TRABAJO" -> {
                    val idSesion = sessionManager.getIdUsuario()?.trim().orEmpty()

                    if (idSesion.isBlank()) {
                        Toast.makeText(this, "No se encontró la sesión del alumno.", Toast.LENGTH_SHORT).show()
                        return@setOnClickListener
                    }

                    val tienePendiente = dashboardAlumnoDao.alumnoTieneSolicitudPendiente(idSesion)

                    if (tienePendiente) {
                        Toast.makeText(
                            this,
                            "Ya tienes una solicitud pendiente. Debes esperar su revisión.",
                            Toast.LENGTH_SHORT
                        ).show()
                        return@setOnClickListener
                    }

                    val intent = Intent(this, AplicarTrabajoAlumnoActivity::class.java)
                    startActivity(intent)
                }
                else -> {
                    Toast.makeText(this, "Acción no disponible.", Toast.LENGTH_SHORT).show()
                }
            }
        }

        binding.btnDetalleBloqueSecundario.setOnClickListener {
            when (tipoDashboard) {
                "INVESTIGACION" -> {
                    val intent = Intent(this, AlumnoRegistrarPropuestaActivity::class.java)
                    startActivity(intent)
                }
                "ESPECIALIZACION" -> {
                    Toast.makeText(this, "Abrir detalle de propuestas de especialización.", Toast.LENGTH_SHORT).show()
                }
                "PASANTIA" -> {
                    val intent = Intent(this, AlumnoBitacorasActivity::class.java)
                    startActivity(intent)
                }
                "SIN_TRABAJO" -> {
                    val intent = Intent(this, DetalleSolicitudAlumnoActivity::class.java)
                    startActivity(intent)
                }
                else -> {
                    Toast.makeText(this, "Detalle no disponible.", Toast.LENGTH_SHORT).show()
                }
            }
        }

        binding.btnAccionInferiorAlumno.setOnClickListener {
            when (tipoDashboard) {
                "INVESTIGACION" -> {
                    val intent = Intent(this, AlumnoRegistrarPropuestaActivity::class.java)
                    startActivity(intent)
                }
                "ESPECIALIZACION" -> {
                    Toast.makeText(this, "Abrir registro de propuestas de especialización.", Toast.LENGTH_SHORT).show()
                }
                "PASANTIA" -> {
                    val intent = Intent(this, AlumnoRegistrarBitacoraActivity::class.java)
                    startActivity(intent)
                }
                "SIN_TRABAJO" -> {
                    Toast.makeText(this, "Abrir pantalla para aplicar a trabajo.", Toast.LENGTH_SHORT).show()
                }
                else -> {
                    Toast.makeText(this, "Acción no disponible.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }



    override fun onResume() {
        super.onResume()
        cargarDashboard()
    }

    private fun irAlLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }
}