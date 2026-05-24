// PasantiaDocenteDao.kt
package com.example.gradues.data.dao

import android.content.ContentValues
import android.database.Cursor
import android.util.Log
import com.example.gradues.data.db.DatabaseHelper
import com.example.gradues.data.model.PasantiaDocenteModel
import com.example.gradues.data.model.BitacoraDocenteModel
import com.example.gradues.data.model.MemoriaDocenteModel
import com.example.gradues.data.model.BitacoraPendienteDocenteModel

class PasantiaDocenteDao(private val dbHelper: DatabaseHelper) {

    companion object {
        private const val TAG = "PasantiaDocenteDao"
    }

    private fun getInternalUserId(userIdString: String): Int? {
        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery(
            """
            SELECT idUsuario
            FROM usuario
            WHERE carnetUsuario = ? OR CAST(idUsuario AS TEXT) = ?
            LIMIT 1
            """.trimIndent(),
            arrayOf(userIdString, userIdString)
        )

        var idUsuario: Int? = null
        if (cursor.moveToFirst()) {
            idUsuario = cursor.getInt(cursor.getColumnIndexOrThrow("idUsuario"))
        }
        cursor.close()
        return idUsuario
    }


    fun obtenerPasantiasAsignadas(idDocente: String): List<PasantiaDocenteModel> {
        val pasantias = mutableListOf<PasantiaDocenteModel>()
        val db = dbHelper.readableDatabase

        val internalIdDocente = getInternalUserId(idDocente)
        if (internalIdDocente == null) {
            Log.d(TAG, "Internal ID for docente $idDocente not found.")
            return emptyList()
        }

        val cursor = db.rawQuery(
            """
            SELECT
                pp.idProyectoPasantia,
                pp.idTrabajoGraduacion,
                tg.nombreTrabajo,
                u.idUsuario AS idEstudiante,
                u.carnetUsuario AS carnetEstudiante,
                COALESCE(u.primerNombreUsuario, '') || ' ' ||
                COALESCE(u.segundoNombreUsuario, '') || ' ' ||
                COALESCE(u.primerApellidoUsuario, '') || ' ' ||
                COALESCE(u.segundoApellidoUsuario, '') AS nombreEstudiante,
                e.idEmpresa,
                e.nombreEmpresa,
                COALESCE(pe.nombrePersonero, 'No asignado') AS nombrePersonero,
                COALESCE(pp.estadoPasantia, 'Pendiente') AS estadoPasantia
            FROM proyecto_pasantia pp
            INNER JOIN trabajo_graduacion tg ON pp.idTrabajoGraduacion = tg.idTrabajoGraduacion
            INNER JOIN alumno_trabajo at ON tg.idTrabajoGraduacion = at.idTrabajoGraduacion
            INNER JOIN usuario u ON at.idUsuario = u.idUsuario
            INNER JOIN empresa e ON pp.idEmpresa = e.idEmpresa
            LEFT JOIN personero pe ON e.idEmpresa = pe.idEmpresa
            WHERE tg.idDocenteResponsable = ?
            AND COALESCE(tg.estadoTrabajo, 'Activo') = 'Activo'
            AND COALESCE(pp.estadoPasantia, 'Activo') = 'Activo'
            GROUP BY pp.idProyectoPasantia, pp.idTrabajoGraduacion, tg.nombreTrabajo,
                     u.idUsuario, u.carnetUsuario, nombreEstudiante, e.idEmpresa,
                     e.nombreEmpresa, pe.nombrePersonero, pp.estadoPasantia
            """.trimIndent(),
            arrayOf(internalIdDocente.toString())
        )

        while (cursor.moveToNext()) {
            val idProyectoPasantia = cursor.getString(cursor.getColumnIndexOrThrow("idProyectoPasantia"))
            val idTrabajoGraduacion = cursor.getString(cursor.getColumnIndexOrThrow("idTrabajoGraduacion"))

            pasantias.add(
                PasantiaDocenteModel(
                    idProyectoPasantia = idProyectoPasantia,
                    idTrabajoGraduacion = idTrabajoGraduacion,
                    nombreTrabajo = cursor.getString(cursor.getColumnIndexOrThrow("nombreTrabajo")),
                    idEstudiante = cursor.getString(cursor.getColumnIndexOrThrow("idEstudiante")),
                    carnetEstudiante = cursor.getString(cursor.getColumnIndexOrThrow("carnetEstudiante")),
                    nombreEstudiante = cursor.getString(cursor.getColumnIndexOrThrow("nombreEstudiante")),
                    idEmpresa = cursor.getString(cursor.getColumnIndexOrThrow("idEmpresa")),
                    nombreEmpresa = cursor.getString(cursor.getColumnIndexOrThrow("nombreEmpresa")),
                    nombrePersonero = cursor.getString(cursor.getColumnIndexOrThrow("nombrePersonero")),
                    estadoPasantia = cursor.getString(cursor.getColumnIndexOrThrow("estadoPasantia")),
                    totalBitacoras = obtenerConteoBitacoras(idTrabajoGraduacion),
                    estadoMemoria = obtenerEstadoMemoria(idTrabajoGraduacion)
                )
            )
        }
        cursor.close()
        return pasantias
    }

    fun obtenerDetallePasantia(idProyectoPasantia: String): PasantiaDocenteModel? {
        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery(
            """
            SELECT
                pp.idProyectoPasantia,
                pp.idTrabajoGraduacion,
                tg.nombreTrabajo,
                u.idUsuario AS idEstudiante,
                u.carnetUsuario AS carnetEstudiante,
                COALESCE(u.primerNombreUsuario, '') || ' ' ||
                COALESCE(u.segundoNombreUsuario, '') || ' ' ||
                COALESCE(u.primerApellidoUsuario, '') || ' ' ||
                COALESCE(u.segundoApellidoUsuario, '') AS nombreEstudiante,
                e.idEmpresa,
                e.nombreEmpresa,
                COALESCE(pe.nombrePersonero, 'No asignado') AS nombrePersonero,
                COALESCE(pp.estadoPasantia, 'Pendiente') AS estadoPasantia
            FROM proyecto_pasantia pp
            INNER JOIN trabajo_graduacion tg ON pp.idTrabajoGraduacion = tg.idTrabajoGraduacion
            INNER JOIN alumno_trabajo at ON tg.idTrabajoGraduacion = at.idTrabajoGraduacion
            INNER JOIN usuario u ON at.idUsuario = u.idUsuario
            INNER JOIN empresa e ON pp.idEmpresa = e.idEmpresa
            LEFT JOIN personero pe ON e.idEmpresa = pe.idEmpresa
            WHERE pp.idProyectoPasantia = ?
            AND COALESCE(tg.estadoTrabajo, 'Activo') = 'Activo'
            AND COALESCE(pp.estadoPasantia, 'Activo') = 'Activo'
            LIMIT 1
            """.trimIndent(),
            arrayOf(idProyectoPasantia)
        )

        var pasantia: PasantiaDocenteModel? = null
        if (cursor.moveToFirst()) {
            val idTrabajoGraduacion = cursor.getString(cursor.getColumnIndexOrThrow("idTrabajoGraduacion"))
            pasantia = PasantiaDocenteModel(
                idProyectoPasantia = cursor.getString(cursor.getColumnIndexOrThrow("idProyectoPasantia")),
                idTrabajoGraduacion = idTrabajoGraduacion,
                nombreTrabajo = cursor.getString(cursor.getColumnIndexOrThrow("nombreTrabajo")),
                idEstudiante = cursor.getString(cursor.getColumnIndexOrThrow("idEstudiante")),
                carnetEstudiante = cursor.getString(cursor.getColumnIndexOrThrow("carnetEstudiante")),
                nombreEstudiante = cursor.getString(cursor.getColumnIndexOrThrow("nombreEstudiante")),
                idEmpresa = cursor.getString(cursor.getColumnIndexOrThrow("idEmpresa")),
                nombreEmpresa = cursor.getString(cursor.getColumnIndexOrThrow("nombreEmpresa")),
                nombrePersonero = cursor.getString(cursor.getColumnIndexOrThrow("nombrePersonero")),
                estadoPasantia = cursor.getString(cursor.getColumnIndexOrThrow("estadoPasantia")),
                totalBitacoras = obtenerConteoBitacoras(idTrabajoGraduacion),
                estadoMemoria = obtenerEstadoMemoria(idTrabajoGraduacion)
            )
        }
        cursor.close()
        return pasantia
    }

    fun obtenerBitacorasPasantia(idTrabajoGraduacion: String): List<BitacoraDocenteModel> {
        val bitacoras = mutableListOf<BitacoraDocenteModel>()
        val db = dbHelper.readableDatabase

        val cursor = db.rawQuery(
            """
            SELECT
                b.idBitacora,
                b.fechaActividad AS fechaBitacora,
                b.descripcionActividad AS descripcionBitacora,
                b.estadoBitacora
            FROM bitacora b
            INNER JOIN proyecto_pasantia pp ON b.idProyectoPasantia = pp.idProyectoPasantia
            WHERE pp.idTrabajoGraduacion = ?
            ORDER BY b.fechaActividad DESC
            """.trimIndent(),
            arrayOf(idTrabajoGraduacion)
        )

        while (cursor.moveToNext()) {
            bitacoras.add(
                BitacoraDocenteModel(
                    idBitacora = cursor.getString(cursor.getColumnIndexOrThrow("idBitacora")),
                    fechaBitacora = cursor.getString(cursor.getColumnIndexOrThrow("fechaBitacora")),
                    descripcionBitacora = cursor.getString(cursor.getColumnIndexOrThrow("descripcionBitacora")),
                    estadoBitacora = cursor.getString(cursor.getColumnIndexOrThrow("estadoBitacora"))
                )
            )
        }
        cursor.close()
        return bitacoras
    }

    fun obtenerMemoriaPasantia(idTrabajoGraduacion: String): MemoriaDocenteModel? {
        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery(
            """
            SELECT
                mr.idMemoriaResumen,
                mr.contenidoResumen AS tituloMemoria,
                mr.urlDocumento,
                COALESCE(mr.estadoMemoria, 'Pendiente') AS estadoMemoria,
                COALESCE(mr.observacionMemoria, '') AS observacionMemoria
            FROM memoria_resumen mr
            INNER JOIN proyecto_pasantia pp ON mr.idProyectoPasantia = pp.idProyectoPasantia
            WHERE pp.idTrabajoGraduacion = ?
            LIMIT 1
            """.trimIndent(),
            arrayOf(idTrabajoGraduacion)
        )

        var memoria: MemoriaDocenteModel? = null
        if (cursor.moveToFirst()) {
            memoria = MemoriaDocenteModel(
                idMemoriaResumen = cursor.getString(cursor.getColumnIndexOrThrow("idMemoriaResumen")),
                tituloMemoria = cursor.getString(cursor.getColumnIndexOrThrow("tituloMemoria")),
                urlDocumento = cursor.getString(cursor.getColumnIndexOrThrow("urlDocumento")),
                estadoMemoria = cursor.getString(cursor.getColumnIndexOrThrow("estadoMemoria")),
                observacionMemoria = cursor.getString(cursor.getColumnIndexOrThrow("observacionMemoria"))
            )
        }
        cursor.close()
        return memoria
    }

    fun aprobarMemoria(idMemoriaResumen: String): Boolean {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put("estadoMemoria", "Aprobada")
        }
        val rowsAffected = db.update("memoria_resumen", values, "idMemoriaResumen = ?", arrayOf(idMemoriaResumen))
        return rowsAffected > 0
    }

    private fun obtenerConteoBitacoras(idTrabajoGraduacion: String): Int {
        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery(
            """
            SELECT COUNT(*) AS totalBitacoras 
            FROM bitacora b
            INNER JOIN proyecto_pasantia pp ON b.idProyectoPasantia = pp.idProyectoPasantia
            WHERE pp.idTrabajoGraduacion = ?
            """.trimIndent(),
            arrayOf(idTrabajoGraduacion)
        )
        var count = 0
        if (cursor.moveToFirst()) {
            count = cursor.getInt(cursor.getColumnIndexOrThrow("totalBitacoras"))
        }
        cursor.close()
        return count
    }

    private fun obtenerEstadoMemoria(idTrabajoGraduacion: String): String {
        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery(
            """
            SELECT COALESCE(mr.estadoMemoria, 'Pendiente') AS estadoMemoria 
            FROM memoria_resumen mr
            INNER JOIN proyecto_pasantia pp ON mr.idProyectoPasantia = pp.idProyectoPasantia
            WHERE pp.idTrabajoGraduacion = ? 
            LIMIT 1
            """.trimIndent(),
            arrayOf(idTrabajoGraduacion)
        )
        var estado = "No Enviada"
        if (cursor.moveToFirst()) {
            estado = cursor.getString(cursor.getColumnIndexOrThrow("estadoMemoria"))
        }
        cursor.close()
        return estado
    }

    fun obtenerBitacorasPendientes(idDocente: String): List<BitacoraPendienteDocenteModel> {
        val bitacorasPendientes = mutableListOf<BitacoraPendienteDocenteModel>()
        val db = dbHelper.readableDatabase

        val internalIdDocente = getInternalUserId(idDocente)
        if (internalIdDocente == null) {
            Log.d(TAG, "Internal ID for docente $idDocente not found.")
            return emptyList()
        }

        val cursor = db.rawQuery(
            """
            SELECT
                b.idBitacora,
                b.idProyectoPasantia,
                pp.idTrabajoGraduacion,
                b.tituloActividad,
                b.descripcionActividad,
                b.fechaActividad,
                b.totalHorasTrabajadas,
                b.estadoBitacora,
                COALESCE(b.observacionBitacora, '') AS observacionBitacora,
                COALESCE(u.primerNombreUsuario, '') || ' ' ||
                COALESCE(u.segundoNombreUsuario || ' ', '') ||
                COALESCE(u.primerApellidoUsuario, '') || ' ' ||
                COALESCE(u.segundoApellidoUsuario, '') AS nombreEstudiante,
                u.carnetUsuario AS carnetEstudiante,
                tg.nombreTrabajo,
                e.nombreEmpresa
            FROM bitacora b
            INNER JOIN proyecto_pasantia pp ON b.idProyectoPasantia = pp.idProyectoPasantia
            INNER JOIN trabajo_graduacion tg ON pp.idTrabajoGraduacion = tg.idTrabajoGraduacion
            INNER JOIN alumno_trabajo at ON tg.idTrabajoGraduacion = at.idTrabajoGraduacion
            INNER JOIN usuario u ON at.idUsuario = u.idUsuario
            INNER JOIN empresa e ON pp.idEmpresa = e.idEmpresa
            WHERE tg.idDocenteResponsable = ?
            AND b.estadoBitacora = 'Pendiente'
            ORDER BY b.fechaActividad ASC
            """.trimIndent(),
            arrayOf(internalIdDocente.toString())
        )

        while (cursor.moveToNext()) {
            bitacorasPendientes.add(
                BitacoraPendienteDocenteModel(
                    idBitacora = cursor.getString(cursor.getColumnIndexOrThrow("idBitacora")),
                    idProyectoPasantia = cursor.getString(cursor.getColumnIndexOrThrow("idProyectoPasantia")),
                    idTrabajoGraduacion = cursor.getString(cursor.getColumnIndexOrThrow("idTrabajoGraduacion")),
                    tituloActividad = cursor.getString(cursor.getColumnIndexOrThrow("tituloActividad")),
                    descripcionActividad = cursor.getString(cursor.getColumnIndexOrThrow("descripcionActividad")),
                    fechaActividad = cursor.getString(cursor.getColumnIndexOrThrow("fechaActividad")),
                    totalHorasTrabajadas = cursor.getInt(cursor.getColumnIndexOrThrow("totalHorasTrabajadas")),
                    estadoBitacora = cursor.getString(cursor.getColumnIndexOrThrow("estadoBitacora")),
                    observacionBitacora = cursor.getString(cursor.getColumnIndexOrThrow("observacionBitacora")),
                    nombreEstudiante = cursor.getString(cursor.getColumnIndexOrThrow("nombreEstudiante")),
                    carnetEstudiante = cursor.getString(cursor.getColumnIndexOrThrow("carnetEstudiante")),
                    nombreTrabajo = cursor.getString(cursor.getColumnIndexOrThrow("nombreTrabajo")),
                    nombreEmpresa = cursor.getString(cursor.getColumnIndexOrThrow("nombreEmpresa"))
                )
            )
        }
        cursor.close()
        return bitacorasPendientes
    }

    fun obtenerDetalleBitacora(idBitacora: String): BitacoraPendienteDocenteModel? {
        val db = dbHelper.readableDatabase

        val cursor = db.rawQuery(
            """
            SELECT
                b.idBitacora,
                b.idProyectoPasantia,
                pp.idTrabajoGraduacion,
                b.tituloActividad,
                b.descripcionActividad,
                b.fechaActividad,
                b.totalHorasTrabajadas,
                b.estadoBitacora,
                COALESCE(b.observacionBitacora, '') AS observacionBitacora,
                COALESCE(u.primerNombreUsuario, '') || ' ' ||
                COALESCE(u.segundoNombreUsuario || ' ', '') ||
                COALESCE(u.primerApellidoUsuario, '') || ' ' ||
                COALESCE(u.segundoApellidoUsuario, '') AS nombreEstudiante,
                u.carnetUsuario AS carnetEstudiante,
                tg.nombreTrabajo,
                e.nombreEmpresa
            FROM bitacora b
            INNER JOIN proyecto_pasantia pp ON b.idProyectoPasantia = pp.idProyectoPasantia
            INNER JOIN trabajo_graduacion tg ON pp.idTrabajoGraduacion = tg.idTrabajoGraduacion
            INNER JOIN alumno_trabajo at ON tg.idTrabajoGraduacion = at.idTrabajoGraduacion
            INNER JOIN usuario u ON at.idUsuario = u.idUsuario
            INNER JOIN empresa e ON pp.idEmpresa = e.idEmpresa
            WHERE b.idBitacora = ?
            LIMIT 1
            """.trimIndent(),
            arrayOf(idBitacora)
        )

        var bitacora: BitacoraPendienteDocenteModel? = null
        if (cursor.moveToFirst()) {
            bitacora = BitacoraPendienteDocenteModel(
                idBitacora = cursor.getString(cursor.getColumnIndexOrThrow("idBitacora")),
                idProyectoPasantia = cursor.getString(cursor.getColumnIndexOrThrow("idProyectoPasantia")),
                idTrabajoGraduacion = cursor.getString(cursor.getColumnIndexOrThrow("idTrabajoGraduacion")),
                tituloActividad = cursor.getString(cursor.getColumnIndexOrThrow("tituloActividad")),
                descripcionActividad = cursor.getString(cursor.getColumnIndexOrThrow("descripcionActividad")),
                fechaActividad = cursor.getString(cursor.getColumnIndexOrThrow("fechaActividad")),
                totalHorasTrabajadas = cursor.getInt(cursor.getColumnIndexOrThrow("totalHorasTrabajadas")),
                estadoBitacora = cursor.getString(cursor.getColumnIndexOrThrow("estadoBitacora")),
                observacionBitacora = cursor.getString(cursor.getColumnIndexOrThrow("observacionBitacora")),
                nombreEstudiante = cursor.getString(cursor.getColumnIndexOrThrow("nombreEstudiante")),
                carnetEstudiante = cursor.getString(cursor.getColumnIndexOrThrow("carnetEstudiante")),
                nombreTrabajo = cursor.getString(cursor.getColumnIndexOrThrow("nombreTrabajo")),
                nombreEmpresa = cursor.getString(cursor.getColumnIndexOrThrow("nombreEmpresa"))
            )
        }
        cursor.close()
        return bitacora
    }

    fun actualizarEstadoYObservacionBitacora(idBitacora: String, nuevoEstado: String, nuevaObservacion: String?): Boolean {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put("estadoBitacora", nuevoEstado)
            put("observacionBitacora", nuevaObservacion)
        }
        val rowsAffected = db.update("bitacora", values, "idBitacora = ?", arrayOf(idBitacora))
        return rowsAffected > 0
    }
}