package com.example.gradues.data.dao

import android.content.ContentValues
import android.database.Cursor
import com.example.gradues.data.db.DatabaseHelper
import com.example.gradues.data.model.AlumnoDisponibleModel
import com.example.gradues.data.model.DocenteDisponibleModel
import com.example.gradues.data.model.GrupoTgeModel
import com.example.gradues.data.model.SubgrupoTgeModel

class GestionCursosAdminDao(
    private val dbHelper: DatabaseHelper
) {

    // ─────────────────────────────────────────────
    // CURSOS (grupo_tge)
    // ─────────────────────────────────────────────

    fun obtenerTodosLosCursos(): List<GrupoTgeModel> {
        val db = dbHelper.readableDatabase
        val lista = mutableListOf<GrupoTgeModel>()

        val cursor = db.rawQuery(
            """
            SELECT
                g.idGrupoTGE,
                g.nombreCurso,
                g.cicloAcademico,
                g.cupoMaximo,
                g.fechaCreacion,
                g.fechaFinal,
                COUNT(s.idSubgrupoTGE) AS totalSubgrupos
            FROM grupo_tge g
            LEFT JOIN subgrupo_tge s ON s.idGrupoTGE = g.idGrupoTGE
            GROUP BY g.idGrupoTGE
            ORDER BY g.idGrupoTGE DESC
            """.trimIndent(),
            null
        )

        while (cursor.moveToNext()) {
            lista.add(
                GrupoTgeModel(
                    idGrupoTGE     = cursor.getInt(cursor.getColumnIndexOrThrow("idGrupoTGE")),
                    nombreCurso    = getString(cursor, "nombreCurso"),
                    cicloAcademico = getString(cursor, "cicloAcademico"),
                    cupoMaximo     = cursor.getInt(cursor.getColumnIndexOrThrow("cupoMaximo")),
                    fechaCreacion  = getString(cursor, "fechaCreacion"),
                    fechaFinal     = getNullableString(cursor, "fechaFinal"),
                    totalSubgrupos = cursor.getInt(cursor.getColumnIndexOrThrow("totalSubgrupos"))
                )
            )
        }

        cursor.close()
        return lista
    }

    fun crearCurso(
        nombreCurso: String,
        cicloAcademico: String,
        cupoMaximo: Int,
        fechaCreacion: String
    ): Boolean {
        val db = dbHelper.writableDatabase

        val values = ContentValues().apply {
            put("nombreCurso", nombreCurso.trim())
            put("cicloAcademico", cicloAcademico.trim())
            put("cupoMaximo", cupoMaximo)
            put("fechaCreacion", fechaCreacion)
            putNull("fechaFinal")
        }

        val resultado = db.insert("grupo_tge", null, values)
        return resultado != -1L
    }

    // ─────────────────────────────────────────────
    // SUBGRUPOS (subgrupo_tge)
    // ─────────────────────────────────────────────

    fun obtenerSubgruposPorCurso(idGrupoTGE: Int): List<SubgrupoTgeModel> {
        val db = dbHelper.readableDatabase
        val lista = mutableListOf<SubgrupoTgeModel>()

        val cursor = db.rawQuery(
            """
            SELECT
                s.idSubgrupoTGE,
                s.idGrupoTGE,
                s.idTrabajoGraduacion,
                s.nombreSubgrupo,
                s.temaAsignado,
                COUNT(at.idAlumnoTrabajo) AS totalAlumnos,
                tg.idDocenteResponsable,
                TRIM(
                    COALESCE(u.primerNombreUsuario, '') || ' ' ||
                    COALESCE(u.segundoNombreUsuario || ' ', '') ||
                    COALESCE(u.primerApellidoUsuario, '') || ' ' ||
                    COALESCE(u.segundoApellidoUsuario, '')
                ) AS nombreDocente
            FROM subgrupo_tge s
            LEFT JOIN trabajo_graduacion tg ON tg.idTrabajoGraduacion = s.idTrabajoGraduacion
            LEFT JOIN usuario u ON u.idUsuario = tg.idDocenteResponsable
            LEFT JOIN alumno_trabajo at ON at.idTrabajoGraduacion = tg.idTrabajoGraduacion
                                       AND COALESCE(at.estadoAlumnoTrabajo, 'Activo') = 'Activo'
            WHERE s.idGrupoTGE = ?
            GROUP BY s.idSubgrupoTGE
            ORDER BY s.idSubgrupoTGE ASC
            """.trimIndent(),
            arrayOf(idGrupoTGE.toString())
        )

        while (cursor.moveToNext()) {
            lista.add(
                SubgrupoTgeModel(
                    idSubgrupoTGE        = cursor.getInt(cursor.getColumnIndexOrThrow("idSubgrupoTGE")),
                    idGrupoTGE           = cursor.getInt(cursor.getColumnIndexOrThrow("idGrupoTGE")),
                    idTrabajoGraduacion  = getNullableInt(cursor, "idTrabajoGraduacion"),
                    nombreSubgrupo       = getString(cursor, "nombreSubgrupo"),
                    temaAsignado         = getNullableString(cursor, "temaAsignado"),
                    totalAlumnos         = cursor.getInt(cursor.getColumnIndexOrThrow("totalAlumnos")),
                    idDocenteResponsable = getNullableInt(cursor, "idDocenteResponsable"),
                    nombreDocente        = getNullableString(cursor, "nombreDocente")
                )
            )
        }

        cursor.close()
        return lista
    }

    fun crearSubgrupo(
        idGrupoTGE: Int,
        nombreSubgrupo: String,
        temaAsignado: String?,
        cicloAcademico: String,
        fechaCreacion: String
    ): Boolean {
        val db = dbHelper.writableDatabase

        db.beginTransaction()
        return try {
            // 1. Crear trabajo_graduacion vinculado al curso de especialización (sin docente aún)
            val valoresTrabajo = ContentValues().apply {
                put("idModalidad", 2)
                putNull("idDocenteResponsable")
                put("nombreTrabajo", nombreSubgrupo.trim())
                put("cicloAcademico", cicloAcademico)
                put("estadoTrabajo", "Activo")
                put("fechaInicioTrabajo", fechaCreacion)
                putNull("fechaFinalTrabajo")
            }
            val idTrabajo = db.insert("trabajo_graduacion", null, valoresTrabajo)
            if (idTrabajo == -1L) return false

            // 2. Crear subgrupo apuntando al trabajo recién creado
            val valoresSubgrupo = ContentValues().apply {
                put("idGrupoTGE", idGrupoTGE)
                put("idTrabajoGraduacion", idTrabajo.toInt())
                put("nombreSubgrupo", nombreSubgrupo.trim())
                if (temaAsignado.isNullOrBlank()) putNull("temaAsignado")
                else put("temaAsignado", temaAsignado.trim())
            }
            val idSubgrupo = db.insert("subgrupo_tge", null, valoresSubgrupo)
            if (idSubgrupo == -1L) return false

            db.setTransactionSuccessful()
            true
        } catch (e: Exception) {
            false
        } finally {
            db.endTransaction()
        }
    }

    // ─────────────────────────────────────────────
    // DOCENTES
    // ─────────────────────────────────────────────

    fun obtenerDocentes(): List<DocenteDisponibleModel> {
        val db = dbHelper.readableDatabase
        val lista = mutableListOf<DocenteDisponibleModel>()

        val cursor = db.rawQuery(
            """
            SELECT
                u.idUsuario,
                TRIM(
                    COALESCE(u.primerNombreUsuario, '') || ' ' ||
                    COALESCE(u.segundoNombreUsuario || ' ', '') ||
                    COALESCE(u.primerApellidoUsuario, '') || ' ' ||
                    COALESCE(u.segundoApellidoUsuario, '')
                ) AS nombreCompleto,
                COALESCE(u.correoUsuario, '') AS correo,
                COALESCE(u.carnetUsuario, '') AS carnet
            FROM usuario u
            INNER JOIN rol r ON r.idRol = u.idRol AND r.nombreRol = 'Docente'
            WHERE COALESCE(u.estadoUsuario, 'Activo') = 'Activo'
            ORDER BY u.primerApellidoUsuario, u.primerNombreUsuario
            """.trimIndent(),
            null
        )

        while (cursor.moveToNext()) {
            lista.add(
                DocenteDisponibleModel(
                    idUsuario      = cursor.getInt(cursor.getColumnIndexOrThrow("idUsuario")),
                    nombreCompleto = getString(cursor, "nombreCompleto"),
                    correo         = getString(cursor, "correo"),
                    carnet         = getString(cursor, "carnet")
                )
            )
        }

        cursor.close()
        return lista
    }

    fun asignarDocenteASubgrupo(idTrabajoGraduacion: Int, idDocente: Int): Boolean {
        val db = dbHelper.writableDatabase

        val values = ContentValues().apply {
            put("idDocenteResponsable", idDocente)
        }

        val filas = db.update(
            "trabajo_graduacion",
            values,
            "idTrabajoGraduacion = ?",
            arrayOf(idTrabajoGraduacion.toString())
        )
        return filas > 0
    }

    // ─────────────────────────────────────────────
    // ALUMNOS
    // ─────────────────────────────────────────────

    /**
     * Devuelve únicamente los alumnos activos que NO están asignados
     * a ningún trabajo_graduacion de modalidad "Curso de especialización" (idModalidad = 2).
     * Marca cuáles ya están asignados al subgrupo actual para poder quitarlos.
     */
    fun obtenerAlumnosParaSubgrupo(idTrabajoGraduacion: Int): List<AlumnoDisponibleModel> {
        val db = dbHelper.readableDatabase
        val lista = mutableListOf<AlumnoDisponibleModel>()

        val cursor = db.rawQuery(
            """
            SELECT
                u.idUsuario,
                TRIM(
                    COALESCE(u.primerNombreUsuario, '') || ' ' ||
                    COALESCE(u.segundoNombreUsuario || ' ', '') ||
                    COALESCE(u.primerApellidoUsuario, '') || ' ' ||
                    COALESCE(u.segundoApellidoUsuario, '')
                ) AS nombreCompleto,
                COALESCE(u.carnetUsuario, '') AS carnet,
                COALESCE(u.correoUsuario, '') AS correo,
                CASE
                    WHEN at_actual.idUsuario IS NOT NULL THEN 1
                    ELSE 0
                END AS asignado
            FROM usuario u
            INNER JOIN rol r ON r.idRol = u.idRol AND r.nombreRol = 'Alumno'
            -- Ya asignado en ESTE subgrupo (para mostrar botón "Quitar")
            LEFT JOIN alumno_trabajo at_actual
                ON at_actual.idUsuario = u.idUsuario
               AND at_actual.idTrabajoGraduacion = ?
               AND COALESCE(at_actual.estadoAlumnoTrabajo, 'Activo') = 'Activo'
            WHERE COALESCE(u.estadoUsuario, 'Activo') = 'Activo'
              AND (
                -- Mostrar si ya está en ESTE subgrupo (para poder quitarlo)
                at_actual.idUsuario IS NOT NULL
                OR
                -- O si NO tiene ningún curso de especialización asignado
                u.idUsuario NOT IN (
                    SELECT at2.idUsuario
                    FROM alumno_trabajo at2
                    INNER JOIN trabajo_graduacion tg2
                        ON tg2.idTrabajoGraduacion = at2.idTrabajoGraduacion
                       AND tg2.idModalidad = 2
                    WHERE COALESCE(at2.estadoAlumnoTrabajo, 'Activo') = 'Activo'
                )
              )
            ORDER BY u.primerApellidoUsuario, u.primerNombreUsuario
            """.trimIndent(),
            arrayOf(idTrabajoGraduacion.toString())
        )

        while (cursor.moveToNext()) {
            lista.add(
                AlumnoDisponibleModel(
                    idUsuario              = cursor.getInt(cursor.getColumnIndexOrThrow("idUsuario")),
                    nombreCompleto         = getString(cursor, "nombreCompleto"),
                    carnet                 = getString(cursor, "carnet"),
                    correo                 = getString(cursor, "correo"),
                    asignadoEnEsteSubgrupo = cursor.getInt(cursor.getColumnIndexOrThrow("asignado")) == 1
                )
            )
        }

        cursor.close()
        return lista
    }

    fun contarAlumnosEnSubgrupo(idTrabajoGraduacion: Int): Int {
        val db = dbHelper.readableDatabase

        val cursor = db.rawQuery(
            """
            SELECT COUNT(*) AS Total
            FROM alumno_trabajo
            WHERE idTrabajoGraduacion = ?
              AND COALESCE(estadoAlumnoTrabajo, 'Activo') = 'Activo'
            """.trimIndent(),
            arrayOf(idTrabajoGraduacion.toString())
        )

        var total = 0
        if (cursor.moveToFirst()) {
            total = cursor.getInt(cursor.getColumnIndexOrThrow("Total"))
        }
        cursor.close()
        return total
    }

    fun asignarAlumnoASubgrupo(idUsuario: Int, idTrabajoGraduacion: Int): AsignacionResultado {
        val db = dbHelper.writableDatabase

        if (contarAlumnosEnSubgrupo(idTrabajoGraduacion) >= 3) {
            return AsignacionResultado.LIMITE_ALCANZADO
        }

        val cursorExiste = db.rawQuery(
            """
            SELECT COUNT(*) AS Total FROM alumno_trabajo
            WHERE idUsuario = ? AND idTrabajoGraduacion = ?
              AND COALESCE(estadoAlumnoTrabajo, 'Activo') = 'Activo'
            """.trimIndent(),
            arrayOf(idUsuario.toString(), idTrabajoGraduacion.toString())
        )
        var yaExiste = false
        if (cursorExiste.moveToFirst()) {
            yaExiste = cursorExiste.getInt(cursorExiste.getColumnIndexOrThrow("Total")) > 0
        }
        cursorExiste.close()

        if (yaExiste) return AsignacionResultado.YA_ASIGNADO

        val values = ContentValues().apply {
            put("idUsuario", idUsuario)
            put("idTrabajoGraduacion", idTrabajoGraduacion)
            put("estadoAlumnoTrabajo", "Activo")
        }

        val resultado = db.insert("alumno_trabajo", null, values)
        return if (resultado != -1L) AsignacionResultado.EXITO else AsignacionResultado.ERROR
    }

    fun desasignarAlumnoDeSubgrupo(idUsuario: Int, idTrabajoGraduacion: Int): Boolean {
        val db = dbHelper.writableDatabase

        val values = ContentValues().apply {
            put("estadoAlumnoTrabajo", "Inactivo")
        }

        val filas = db.update(
            "alumno_trabajo",
            values,
            "idUsuario = ? AND idTrabajoGraduacion = ? AND COALESCE(estadoAlumnoTrabajo, 'Activo') = 'Activo'",
            arrayOf(idUsuario.toString(), idTrabajoGraduacion.toString())
        )
        return filas > 0
    }

    // ─────────────────────────────────────────────
    // HELPERS
    // ─────────────────────────────────────────────

    private fun getString(cursor: Cursor, column: String): String {
        val index = cursor.getColumnIndexOrThrow(column)
        return if (cursor.isNull(index)) "" else cursor.getString(index)
    }

    private fun getNullableString(cursor: Cursor, column: String): String? {
        val index = cursor.getColumnIndexOrThrow(column)
        return if (cursor.isNull(index)) null else cursor.getString(index)
    }

    private fun getNullableInt(cursor: Cursor, column: String): Int? {
        val index = cursor.getColumnIndexOrThrow(column)
        return if (cursor.isNull(index)) null else cursor.getInt(index)
    }

    enum class AsignacionResultado {
        EXITO,
        LIMITE_ALCANZADO,
        YA_ASIGNADO,
        ERROR
    }
}