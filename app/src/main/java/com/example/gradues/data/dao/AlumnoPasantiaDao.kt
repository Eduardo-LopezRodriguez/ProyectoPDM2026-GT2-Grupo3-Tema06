package com.example.gradues.data.dao

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import com.example.gradues.data.db.DatabaseHelper
import com.example.gradues.data.model.AlumnoBitacoraModel
import com.example.gradues.data.model.AlumnoPasantiaDetalleModel
import com.example.gradues.data.model.MemoriaDocenteModel

class AlumnoPasantiaDao(private val dbHelper: DatabaseHelper) {

    fun obtenerDetallePasantiaPorAlumno(idUsuario: String): AlumnoPasantiaDetalleModel? {
        val db = dbHelper.readableDatabase

        val cursor = db.rawQuery(
            """
        SELECT
            pp.idProyectoPasantia,
            tg.idTrabajoGraduacion,
            TRIM(
                COALESCE(u.primerNombreUsuario, '') || ' ' ||
                COALESCE(u.segundoNombreUsuario || ' ', '') ||
                COALESCE(u.primerApellidoUsuario, '') || ' ' ||
                COALESCE(u.segundoApellidoUsuario, '')
            ),
            u.carnetUsuario,
            COALESCE(d.nombreUsuario, ''),
            tg.nombreTrabajo,
            tg.estadoTrabajo,
            tg.cicloAcademico,
            COALESCE(pp.fechaInicioPasantia, ''),
            COALESCE(pp.fechaFinalPasantia, ''),
            COALESCE(pp.estadoPasantia, ''),
            COALESCE(e.nombreEmpresa, ''),
            COALESCE(e.rubroEmpresa, ''),
            COALESCE(p.nombrePersonero, ''),
            COALESCE(p.cargoPersonero, '')
        FROM alumno_trabajo at
        INNER JOIN usuario u
            ON u.idUsuario = at.idUsuario
        INNER JOIN trabajo_graduacion tg
            ON tg.idTrabajoGraduacion = at.idTrabajoGraduacion
        INNER JOIN proyecto_pasantia pp
            ON pp.idTrabajoGraduacion = tg.idTrabajoGraduacion
        INNER JOIN empresa e
            ON e.idEmpresa = pp.idEmpresa
        LEFT JOIN personero p
            ON p.idEmpresa = e.idEmpresa
        LEFT JOIN usuario d
            ON d.idUsuario = tg.idDocenteResponsable
        WHERE CAST(u.idUsuario AS TEXT) = ? OR u.carnetUsuario = ?
        LIMIT 1
        """.trimIndent(),
            arrayOf(idUsuario, idUsuario)
        )

        var detalle: AlumnoPasantiaDetalleModel? = null

        if (cursor.moveToFirst()) {
            detalle = AlumnoPasantiaDetalleModel(
                idProyectoPasantia = cursor.getInt(0),
                idTrabajoGraduacion = cursor.getInt(1),
                nombreAlumno = cursor.getString(2) ?: "",
                carnetAlumno = cursor.getString(3) ?: "",
                nombreDocente = cursor.getString(4) ?: "",
                nombreTrabajo = cursor.getString(5) ?: "",
                estadoTrabajo = cursor.getString(6) ?: "",
                cicloAcademico = cursor.getString(7) ?: "",
                fechaInicioPasantia = cursor.getString(8) ?: "",
                fechaFinalPasantia = cursor.getString(9) ?: "",
                estadoPasantia = cursor.getString(10) ?: "",
                nombreEmpresa = cursor.getString(11) ?: "",
                rubroEmpresa = cursor.getString(12) ?: "",
                nombrePersonero = cursor.getString(13) ?: "",
                cargoPersonero = cursor.getString(14) ?: ""
            )
        }

        cursor.close()
        db.close()
        return detalle
    }

    fun obtenerBitacorasPorAlumno(idUsuario: String): List<AlumnoBitacoraModel> {
        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery(
            """
            SELECT
                b.idBitacora,
                b.idProyectoPasantia,
                COALESCE(b.fechaActividad, ''),
                COALESCE(b.tituloActividad, ''),
                COALESCE(b.descripcionActividad, ''),
                COALESCE(b.totalHorasTrabajadas, 0),
                COALESCE(b.estadoBitacora, 'Pendiente'),
                COALESCE(b.observacionBitacora, '')
            FROM bitacora b
            INNER JOIN proyecto_pasantia pp
                ON pp.idProyectoPasantia = b.idProyectoPasantia
            INNER JOIN alumno_trabajo at
                ON at.idTrabajoGraduacion = pp.idTrabajoGraduacion
            INNER JOIN usuario u
                ON u.idUsuario = at.idUsuario
            WHERE CAST(u.idUsuario AS TEXT) = ? OR u.carnetUsuario = ?
            ORDER BY b.fechaActividad DESC, b.idBitacora DESC
            """.trimIndent(),
            arrayOf(idUsuario, idUsuario)
        )

        val bitacoras = mutableListOf<AlumnoBitacoraModel>()
        while (cursor.moveToNext()) {
            bitacoras.add(
                AlumnoBitacoraModel(
                    idBitacora = cursor.getInt(0),
                    idProyectoPasantia = cursor.getInt(1),
                    fechaActividad = cursor.getString(2) ?: "",
                    tituloActividad = cursor.getString(3) ?: "",
                    descripcionActividad = cursor.getString(4) ?: "",
                    totalHorasTrabajadas = cursor.getInt(5),
                    estadoBitacora = cursor.getString(6) ?: "Pendiente",
                    observacionBitacora = cursor.getString(7) ?: ""
                )
            )
        }

        cursor.close()
        db.close()
        return bitacoras
    }

    fun obtenerMemoriaPorAlumno(idUsuario: String): MemoriaDocenteModel? {
        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery(
            """
            SELECT
                mr.idMemoriaResumen,
                COALESCE(mr.contenidoResumen, ''),
                mr.urlDocumento,
                COALESCE(mr.estadoMemoria, 'Pendiente'),
                COALESCE(mr.observacionMemoria, '')
            FROM memoria_resumen mr
            INNER JOIN proyecto_pasantia pp
                ON pp.idProyectoPasantia = mr.idProyectoPasantia
            INNER JOIN alumno_trabajo at
                ON at.idTrabajoGraduacion = pp.idTrabajoGraduacion
            INNER JOIN usuario u
                ON u.idUsuario = at.idUsuario
            WHERE CAST(u.idUsuario AS TEXT) = ? OR u.carnetUsuario = ?
            LIMIT 1
            """.trimIndent(),
            arrayOf(idUsuario, idUsuario)
        )

        var memoria: MemoriaDocenteModel? = null
        if (cursor.moveToFirst()) {
            memoria = MemoriaDocenteModel(
                idMemoriaResumen = cursor.getString(0),
                tituloMemoria = cursor.getString(1) ?: "",
                urlDocumento = cursor.getString(2),
                estadoMemoria = cursor.getString(3) ?: "Pendiente",
                observacionMemoria = cursor.getString(4) ?: ""
            )
        }

        cursor.close()
        db.close()
        return memoria
    }

    fun registrarBitacoraAlumno(
        idUsuario: String,
        fechaActividad: String,
        tituloActividad: String,
        descripcionActividad: String,
        totalHorasTrabajadas: Int
    ): Boolean {
        val idProyectoPasantia = obtenerIdProyectoPasantiaPorAlumno(idUsuario) ?: return false
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put("idProyectoPasantia", idProyectoPasantia)
            put("fechaActividad", fechaActividad)
            put("tituloActividad", tituloActividad)
            put("descripcionActividad", descripcionActividad)
            put("totalHorasTrabajadas", totalHorasTrabajadas)
            put("estadoBitacora", "Pendiente")
            putNull("observacionBitacora")
        }

        val resultado = db.insert("bitacora", null, values)
        db.close()
        return resultado != -1L
    }

    private fun obtenerIdProyectoPasantiaPorAlumno(idUsuario: String): Int? {
        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery(
            """
            SELECT pp.idProyectoPasantia
            FROM proyecto_pasantia pp
            INNER JOIN alumno_trabajo at
                ON at.idTrabajoGraduacion = pp.idTrabajoGraduacion
            INNER JOIN usuario u
                ON u.idUsuario = at.idUsuario
            WHERE CAST(u.idUsuario AS TEXT) = ? OR u.carnetUsuario = ?
            LIMIT 1
            """.trimIndent(),
            arrayOf(idUsuario, idUsuario)
        )

        val idProyecto = if (cursor.moveToFirst()) cursor.getInt(0) else null
        cursor.close()
        db.close()
        return idProyecto
    }
}
