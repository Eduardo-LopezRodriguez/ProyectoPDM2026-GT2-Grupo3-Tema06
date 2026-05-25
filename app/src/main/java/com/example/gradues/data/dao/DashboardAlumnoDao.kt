package com.example.gradues.data.dao

import com.example.gradues.data.db.DatabaseHelper
import com.example.gradues.data.model.DashboardAlumnoModel
import java.util.Locale

class DashboardAlumnoDao(private val dbHelper: DatabaseHelper) {

    fun obtenerDashboardAlumno(idSesion: String): DashboardAlumnoModel? {
        val db = dbHelper.readableDatabase

        val query = """
            SELECT
                u.idUsuario,
                TRIM(
                    COALESCE(u.primerNombreUsuario, '') || ' ' ||
                    COALESCE(u.segundoNombreUsuario, '') || ' ' ||
                    COALESCE(u.primerApellidoUsuario, '') || ' ' ||
                    COALESCE(u.segundoApellidoUsuario, '')
                ) AS nombreCompleto,
                COALESCE(u.correoUsuario, '') AS correoUsuario,
                COALESCE(u.carnetUsuario, CAST(u.idUsuario AS TEXT)) AS carnetUsuario,

                tg.idTrabajoGraduacion,
                COALESCE(m.tipoModalidad, '') AS tipoModalidad,
                COALESCE(tg.nombreTrabajo, '') AS nombreTrabajo,
                COALESCE(gtgi.codigoGrupoTGI, '') AS codigoGrupoTGI,
                COALESCE(sgtge.nombreSubgrupo, '') AS nombreSubgrupo,
                COALESCE(gte.nombreCurso, '') AS nombreCurso,
                COALESCE(emp.nombreEmpresa, '') AS nombreEmpresa,
                COALESCE(sm.estadoSolicitud, 'Sin solicitud') AS estadoSolicitud,
                COALESCE(sm.nombreTrabajoPropuesto, '') AS nombreTrabajoPropuesto,

                (
                    SELECT COUNT(*)
                    FROM propuesta_perfil pp
                    WHERE pp.idTrabajoGraduacion = tg.idTrabajoGraduacion
                ) AS totalPropuestas,

                (
                    SELECT COUNT(*)
                    FROM propuesta_perfil pp
                    WHERE pp.idTrabajoGraduacion = tg.idTrabajoGraduacion
                      AND UPPER(COALESCE(pp.estadoPropuesta, '')) = 'PENDIENTE'
                ) AS propuestasPendientes,

                (
                    SELECT COUNT(*)
                    FROM propuesta_perfil pp
                    WHERE pp.idTrabajoGraduacion = tg.idTrabajoGraduacion
                      AND UPPER(COALESCE(pp.estadoPropuesta, '')) = 'BORRADOR'
                ) AS propuestasBorradores,

                (
                    SELECT COUNT(*)
                    FROM propuesta_perfil pp
                    WHERE pp.idTrabajoGraduacion = tg.idTrabajoGraduacion
                      AND (
                            UPPER(COALESCE(pp.estadoPropuesta, '')) = 'APROBADA'
                            OR UPPER(COALESCE(pp.estadoPropuesta, '')) = 'SELECCIONADA'
                          )
                ) AS propuestasAprobadas,

                (
                    SELECT COUNT(*)
                    FROM propuesta_perfil pp
                    WHERE pp.idTrabajoGraduacion = tg.idTrabajoGraduacion
                      AND (
                            UPPER(COALESCE(pp.estadoPropuesta, '')) = 'DENEGADA'
                            OR UPPER(COALESCE(pp.estadoPropuesta, '')) = 'DESCARTADA'
                          )
                ) AS propuestasDenegadas,

                (
                    SELECT COUNT(*)
                    FROM propuesta_perfil pp
                    WHERE pp.idTrabajoGraduacion = tg.idTrabajoGraduacion
                      AND (
                            UPPER(COALESCE(pp.estadoPropuesta, '')) = 'CON OBSERVACIÓN'
                            OR UPPER(COALESCE(pp.estadoPropuesta, '')) = 'CON OBSERVACION'
                          )
                ) AS propuestasConObservacion,

                (
                    SELECT ne.nota
                    FROM nota_etapa ne
                    INNER JOIN alumno_trabajo at1
                        ON at1.idAlumnoTrabajo = ne.idAlumnoTrabajo
                    WHERE at1.idUsuario = u.idUsuario
                      AND ne.numeroEtapa = 1
                    LIMIT 1
                ) AS notaEtapa1,

                (
                    SELECT ne.nota
                    FROM nota_etapa ne
                    INNER JOIN alumno_trabajo at2
                        ON at2.idAlumnoTrabajo = ne.idAlumnoTrabajo
                    WHERE at2.idUsuario = u.idUsuario
                      AND ne.numeroEtapa = 2
                    LIMIT 1
                ) AS notaEtapa2,

                (
                    SELECT ne.nota
                    FROM nota_etapa ne
                    INNER JOIN alumno_trabajo at3
                        ON at3.idAlumnoTrabajo = ne.idAlumnoTrabajo
                    WHERE at3.idUsuario = u.idUsuario
                      AND ne.numeroEtapa = 3
                    LIMIT 1
                ) AS notaEtapa3,

                (
                    SELECT ne.nota
                    FROM nota_etapa ne
                    INNER JOIN alumno_trabajo at4
                        ON at4.idAlumnoTrabajo = ne.idAlumnoTrabajo
                    WHERE at4.idUsuario = u.idUsuario
                      AND ne.numeroEtapa = 4
                    LIMIT 1
                ) AS notaEtapa4,

                (
                    SELECT COUNT(*)
                    FROM bitacora b
                    INNER JOIN proyecto_pasantia ppas
                        ON ppas.idProyectoPasantia = b.idProyectoPasantia
                    INNER JOIN alumno_trabajo at5
                        ON at5.idTrabajoGraduacion = ppas.idTrabajoGraduacion
                    WHERE at5.idUsuario = u.idUsuario
                ) AS totalBitacoras,

                (
                    SELECT COUNT(*)
                    FROM bitacora b
                    INNER JOIN proyecto_pasantia ppas
                        ON ppas.idProyectoPasantia = b.idProyectoPasantia
                    INNER JOIN alumno_trabajo at6
                        ON at6.idTrabajoGraduacion = ppas.idTrabajoGraduacion
                    WHERE at6.idUsuario = u.idUsuario
                      AND UPPER(COALESCE(b.estadoBitacora, '')) = 'PENDIENTE'
                ) AS bitacorasPendientes
            FROM usuario u
            LEFT JOIN alumno_trabajo at
                ON at.idUsuario = u.idUsuario
            LEFT JOIN trabajo_graduacion tg
                ON tg.idTrabajoGraduacion = at.idTrabajoGraduacion
            LEFT JOIN modalidad m
                ON m.idModalidad = tg.idModalidad
            LEFT JOIN grupo_tgi gtgi
                ON gtgi.idTrabajoGraduacion = tg.idTrabajoGraduacion
            LEFT JOIN subgrupo_tge sgtge
                ON sgtge.idTrabajoGraduacion = tg.idTrabajoGraduacion
            LEFT JOIN grupo_tge gte
                ON gte.idGrupoTGE = sgtge.idGrupoTGE
            LEFT JOIN proyecto_pasantia pp
                ON pp.idTrabajoGraduacion = tg.idTrabajoGraduacion
            LEFT JOIN empresa emp
                ON emp.idEmpresa = pp.idEmpresa
            LEFT JOIN solicitud_modalidad sm
                ON sm.idUsuario = u.idUsuario
                AND sm.idSolicitudModalidad = (
                    SELECT MAX(sm2.idSolicitudModalidad)
                    FROM solicitud_modalidad sm2
                    WHERE sm2.idUsuario = u.idUsuario
                )
            WHERE CAST(u.idUsuario AS TEXT) = ?
               OR UPPER(COALESCE(u.carnetUsuario, '')) = UPPER(?)
            LIMIT 1
        """.trimIndent()

        val cursor = db.rawQuery(query, arrayOf(idSesion.trim(), idSesion.trim()))

        var resultado: DashboardAlumnoModel? = null

        if (cursor.moveToFirst()) {
            val nombreCompleto = cursor.getString(cursor.getColumnIndexOrThrow("nombreCompleto"))
            val correoUsuario = cursor.getString(cursor.getColumnIndexOrThrow("correoUsuario"))
            val carnetUsuario = cursor.getString(cursor.getColumnIndexOrThrow("carnetUsuario"))

            val idTrabajoGraduacion =
                if (cursor.isNull(cursor.getColumnIndexOrThrow("idTrabajoGraduacion"))) null
                else cursor.getInt(cursor.getColumnIndexOrThrow("idTrabajoGraduacion"))

            val tipoModalidad = cursor.getString(cursor.getColumnIndexOrThrow("tipoModalidad"))
            val nombreTrabajo = cursor.getString(cursor.getColumnIndexOrThrow("nombreTrabajo"))
            val codigoGrupoTGI = cursor.getString(cursor.getColumnIndexOrThrow("codigoGrupoTGI"))
            val nombreSubgrupo = cursor.getString(cursor.getColumnIndexOrThrow("nombreSubgrupo"))
            val nombreCurso = cursor.getString(cursor.getColumnIndexOrThrow("nombreCurso"))
            val nombreEmpresa = cursor.getString(cursor.getColumnIndexOrThrow("nombreEmpresa"))
            val estadoSolicitud = cursor.getString(cursor.getColumnIndexOrThrow("estadoSolicitud"))
            val nombreTrabajoPropuesto = cursor.getString(cursor.getColumnIndexOrThrow("nombreTrabajoPropuesto"))

            val totalPropuestas = cursor.getInt(cursor.getColumnIndexOrThrow("totalPropuestas"))
            val propuestasPendientes = cursor.getInt(cursor.getColumnIndexOrThrow("propuestasPendientes"))
            val propuestasBorradores = cursor.getInt(cursor.getColumnIndexOrThrow("propuestasBorradores"))
            val propuestasAprobadas = cursor.getInt(cursor.getColumnIndexOrThrow("propuestasAprobadas"))
            val propuestasDenegadas = cursor.getInt(cursor.getColumnIndexOrThrow("propuestasDenegadas"))
            val propuestasConObservacion = cursor.getInt(cursor.getColumnIndexOrThrow("propuestasConObservacion"))
            val resumenPropuestas = formatearResumenPropuestas(
                total = totalPropuestas,
                pendientes = propuestasPendientes,
                borradores = propuestasBorradores,
                aprobadas = propuestasAprobadas,
                denegadas = propuestasDenegadas,
                conObservacion = propuestasConObservacion
            )
            val totalBitacoras = cursor.getInt(cursor.getColumnIndexOrThrow("totalBitacoras"))
            val bitacorasPendientes = cursor.getInt(cursor.getColumnIndexOrThrow("bitacorasPendientes"))

            val notaEtapa1 = formatearNota(cursor, "notaEtapa1")
            val notaEtapa2 = formatearNota(cursor, "notaEtapa2")
            val notaEtapa3 = formatearNota(cursor, "notaEtapa3")
            val notaEtapa4 = formatearNota(cursor, "notaEtapa4")

            val tieneTrabajoAsignado = idTrabajoGraduacion != null

            resultado = if (!tieneTrabajoAsignado) {
                DashboardAlumnoModel(
                    nombreCompleto = nombreCompleto,
                    correoUsuario = correoUsuario,
                    carnetUsuario = carnetUsuario,
                    tipoDashboard = "SIN_TRABAJO",
                    tituloSeccionPrincipal = "Aplica a un trabajo de graduación",
                    tituloTarjetaPrincipal = "Aún no tienes trabajo asignado",
                    subtituloTarjetaPrincipal = if (nombreTrabajoPropuesto.isNotBlank()) {
                        "Solicitud actual: $nombreTrabajoPropuesto"
                    } else {
                        "Puedes aplicar a un trabajo registrado en el sistema"
                    },
                    textoBotonPrincipal = "Ver trabajos disponibles",
                    mostrarBloquePropuestas = true,
                    tituloBloqueSecundario = "Estado de solicitud",
                    descripcionBloqueSecundario = if (nombreTrabajoPropuesto.isNotBlank()) {
                        nombreTrabajoPropuesto
                    } else {
                        "Todavía no has realizado una solicitud de modalidad."
                    },
                    estadoBloqueSecundario = "Estado: $estadoSolicitud",
                    textoBotonSecundario = "Ver detalle",
                    mostrarBloqueNotas = false,
                    notaEtapa1 = "--",
                    notaEtapa2 = "--",
                    notaEtapa3 = "--",
                    notaEtapa4 = "--",
                    textoBotonAccionInferior = "Aplicar a trabajo",
                    tieneTrabajoAsignado = false
                )
            } else {
                when (tipoModalidad.trim().lowercase(Locale.getDefault())) {
                    "investigación", "investigacion" -> DashboardAlumnoModel(
                        nombreCompleto = nombreCompleto,
                        correoUsuario = correoUsuario,
                        carnetUsuario = carnetUsuario,
                        tipoDashboard = "INVESTIGACION",
                        tituloSeccionPrincipal = "Mi grupo de investigación",
                        tituloTarjetaPrincipal = if (codigoGrupoTGI.isNotBlank()) {
                            "Grupo de Investigación ${codigoGrupoTGI.removePrefix("TGI-")}"
                        } else {
                            "Mi grupo de investigación"
                        },
                        subtituloTarjetaPrincipal = nombreTrabajo,
                        textoBotonPrincipal = "Ver grupo",
                        mostrarBloquePropuestas = true,
                        tituloBloqueSecundario = "Estado de propuestas",
                        descripcionBloqueSecundario = nombreTrabajo,
                        estadoBloqueSecundario = resumenPropuestas,
                        textoBotonSecundario = "Ver detalle",
                        mostrarBloqueNotas = true,
                        notaEtapa1 = notaEtapa1,
                        notaEtapa2 = notaEtapa2,
                        notaEtapa3 = notaEtapa3,
                        notaEtapa4 = notaEtapa4,
                        textoBotonAccionInferior = "Registrar propuestas",
                        tieneTrabajoAsignado = true
                    )

                    "curso de especialización", "curso de especializacion" -> DashboardAlumnoModel(
                        nombreCompleto = nombreCompleto,
                        correoUsuario = correoUsuario,
                        carnetUsuario = carnetUsuario,
                        tipoDashboard = "ESPECIALIZACION",
                        tituloSeccionPrincipal = "Mi grupo de especialización",
                        tituloTarjetaPrincipal = if (nombreSubgrupo.isNotBlank()) {
                            "Mi subgrupo"
                        } else {
                            "Mi grupo de especialización"
                        },
                        subtituloTarjetaPrincipal = if (nombreCurso.isNotBlank()) nombreCurso else nombreTrabajo,
                        textoBotonPrincipal = "Ver grupo",
                        mostrarBloquePropuestas = true,
                        tituloBloqueSecundario = "Estado de propuestas",
                        descripcionBloqueSecundario = if (nombreTrabajoPropuesto.isNotBlank()) nombreTrabajoPropuesto else nombreTrabajo,
                        estadoBloqueSecundario = resumenPropuestas,
                        textoBotonSecundario = "Ver detalle",
                        mostrarBloqueNotas = true,
                        notaEtapa1 = notaEtapa1,
                        notaEtapa2 = notaEtapa2,
                        notaEtapa3 = notaEtapa3,
                        notaEtapa4 = notaEtapa4,
                        textoBotonAccionInferior = "Registrar propuestas",
                        tieneTrabajoAsignado = true
                    )

                    "pasantía profesional", "pasantia profesional" -> DashboardAlumnoModel(
                        nombreCompleto = nombreCompleto,
                        correoUsuario = correoUsuario,
                        carnetUsuario = carnetUsuario,
                        tipoDashboard = "PASANTIA",
                        tituloSeccionPrincipal = "Mi pasantía",
                        tituloTarjetaPrincipal = "Pasantía profesional",
                        subtituloTarjetaPrincipal = nombreEmpresa.ifBlank { nombreTrabajo },
                        textoBotonPrincipal = "Ver pasantía",
                        mostrarBloquePropuestas = true,
                        tituloBloqueSecundario = "Bitácoras",
                        descripcionBloqueSecundario = "Tienes bitácoras pendientes de entregar",
                        estadoBloqueSecundario = "Estado: $bitacorasPendientes/$totalBitacoras bitácoras",
                        textoBotonSecundario = "Ver detalle",
                        mostrarBloqueNotas = false,
                        notaEtapa1 = "--",
                        notaEtapa2 = "--",
                        notaEtapa3 = "--",
                        notaEtapa4 = "--",
                        textoBotonAccionInferior = "Registrar bitácoras",
                        tieneTrabajoAsignado = true
                    )

                    else -> DashboardAlumnoModel(
                        nombreCompleto = nombreCompleto,
                        correoUsuario = correoUsuario,
                        carnetUsuario = carnetUsuario,
                        tipoDashboard = "SIN_TRABAJO",
                        tituloSeccionPrincipal = "Aplica a un trabajo de graduación",
                        tituloTarjetaPrincipal = "Información no disponible",
                        subtituloTarjetaPrincipal = "No se pudo identificar la modalidad actual",
                        textoBotonPrincipal = "Ver trabajos disponibles",
                        mostrarBloquePropuestas = false,
                        tituloBloqueSecundario = "",
                        descripcionBloqueSecundario = "",
                        estadoBloqueSecundario = "",
                        textoBotonSecundario = "",
                        mostrarBloqueNotas = false,
                        notaEtapa1 = "--",
                        notaEtapa2 = "--",
                        notaEtapa3 = "--",
                        notaEtapa4 = "--",
                        textoBotonAccionInferior = "Aplicar a trabajo",
                        tieneTrabajoAsignado = false
                    )
                }
            }
        }

        cursor.close()
        return resultado
    }

    private fun formatearNota(cursor: android.database.Cursor, columnName: String): String {
        val index = cursor.getColumnIndexOrThrow(columnName)
        return if (cursor.isNull(index)) {
            "--"
        } else {
            String.format(Locale.getDefault(), "%.2f", cursor.getDouble(index))
        }
    }

    private fun formatearResumenPropuestas(
        total: Int,
        pendientes: Int,
        borradores: Int,
        aprobadas: Int,
        denegadas: Int,
        conObservacion: Int
    ): String {
        if (total == 0) return "Sin propuestas registradas"

        val partes = listOf(
            pendientes to "pendientes",
            borradores to "borradores",
            aprobadas to "aprobadas",
            denegadas to "denegadas",
            conObservacion to "con observación"
        ).filter { it.first > 0 }
            .joinToString(", ") { "${it.first} ${it.second}" }

        return if (partes.isBlank()) {
            "$total registradas"
        } else {
            "$total registradas, $partes"
        }
    }

    private fun obtenerEstadoSolicitudAlumno(
        idUsuario: Int,
        db: android.database.sqlite.SQLiteDatabase
    ): Pair<String, String> {
        val query = """
        SELECT
            sm.estadoSolicitud,
            sm.nombreTrabajoPropuesto,
            sm.fechaSolicitud,
            m.tipoModalidad
        FROM solicitud_modalidad sm
        LEFT JOIN modalidad m
            ON m.idModalidad = sm.idModalidad
        WHERE sm.idUsuario = ?
        ORDER BY sm.idSolicitudModalidad DESC
        LIMIT 1
    """.trimIndent()

        val cursor = db.rawQuery(query, arrayOf(idUsuario.toString()))

        var estadoTexto = "Estado: Sin solicitud"
        var detalleTexto = "Todavía no has realizado una solicitud de modalidad."

        if (cursor.moveToFirst()) {
            val estado = cursor.getString(cursor.getColumnIndexOrThrow("estadoSolicitud")).orEmpty()
            val nombreTrabajo = if (cursor.isNull(cursor.getColumnIndexOrThrow("nombreTrabajoPropuesto"))) {
                ""
            } else {
                cursor.getString(cursor.getColumnIndexOrThrow("nombreTrabajoPropuesto"))
            }

            val modalidad = if (cursor.isNull(cursor.getColumnIndexOrThrow("tipoModalidad"))) {
                ""
            } else {
                cursor.getString(cursor.getColumnIndexOrThrow("tipoModalidad"))
            }

            estadoTexto = "Estado: ${estado.ifBlank { "Sin estado" }}"

            detalleTexto = when {
                nombreTrabajo.isNotBlank() && modalidad.isNotBlank() ->
                    "$modalidad - $nombreTrabajo"
                nombreTrabajo.isNotBlank() ->
                    nombreTrabajo
                modalidad.isNotBlank() ->
                    modalidad
                else ->
                    "Solicitud registrada recientemente"
            }
        }

        cursor.close()
        return Pair(estadoTexto, detalleTexto)
    }

    fun alumnoTieneSolicitudPendiente(idSesion: String): Boolean {
        val db = dbHelper.readableDatabase

        val query = """
        SELECT EXISTS(
            SELECT 1
            FROM solicitud_modalidad sm
            INNER JOIN usuario u
                ON u.idUsuario = sm.idUsuario
            WHERE (CAST(u.idUsuario AS TEXT) = ?
               OR UPPER(COALESCE(u.carnetUsuario, '')) = UPPER(?))
              AND UPPER(COALESCE(sm.estadoSolicitud, '')) = 'PENDIENTE'
        )
    """.trimIndent()

        val cursor = db.rawQuery(query, arrayOf(idSesion.trim(), idSesion.trim()))

        var existePendiente = false

        if (cursor.moveToFirst()) {
            existePendiente = cursor.getInt(0) == 1
        }

        cursor.close()
        return existePendiente
    }
}
