package com.example.gradues.data.dao

import android.content.ContentValues
import android.database.Cursor
import com.example.gradues.data.db.DatabaseHelper
import com.example.gradues.data.model.RolAdminModel
import com.example.gradues.data.model.UsuarioAdminModel

class UsuarioAdminDao(
    private val dbHelper: DatabaseHelper
) {

    fun listarUsuarios(): List<UsuarioAdminModel> {
        val db = dbHelper.readableDatabase
        val lista = mutableListOf<UsuarioAdminModel>()

        val cursor = db.rawQuery(
            """
            SELECT
                u.idUsuario AS IdUsuario,
                COALESCE(u.nombreUsuario, 'Sin nombre') AS NombreUsuario,
                u.carnetUsuario AS CarnetUsuario,
                u.correoUsuario AS CorreoUsuario,
                COALESCE(r.nombreRol, 'Sin rol') AS NombreRol,
                u.carreraUsuario AS CarreraUsuario,
                COALESCE(u.estadoUsuario, 'Activo') AS EstadoUsuario
            FROM usuario u
            INNER JOIN rol r
                ON r.idRol = u.idRol
            ORDER BY
                r.nombreRol,
                u.nombreUsuario
            """.trimIndent(),
            null
        )

        while (cursor.moveToNext()) {
            lista.add(
                UsuarioAdminModel(
                    idUsuario = cursor.getInt(cursor.getColumnIndexOrThrow("IdUsuario")),
                    nombreUsuario = getString(cursor, "NombreUsuario"),
                    carnetUsuario = getNullableString(cursor, "CarnetUsuario"),
                    correoUsuario = getNullableString(cursor, "CorreoUsuario"),
                    nombreRol = getString(cursor, "NombreRol"),
                    carreraUsuario = getNullableString(cursor, "CarreraUsuario"),
                    estadoUsuario = getString(cursor, "EstadoUsuario")
                )
            )
        }

        cursor.close()
        return lista
    }

    fun listarRoles(): List<RolAdminModel> {
        val db = dbHelper.readableDatabase
        val lista = mutableListOf<RolAdminModel>()

        val cursor = db.rawQuery(
            """
            SELECT
                idRol,
                nombreRol
            FROM rol
            ORDER BY idRol
            """.trimIndent(),
            null
        )

        while (cursor.moveToNext()) {
            lista.add(
                RolAdminModel(
                    idRol = cursor.getInt(cursor.getColumnIndexOrThrow("idRol")),
                    nombreRol = getString(cursor, "nombreRol")
                )
            )
        }

        cursor.close()
        return lista
    }

    fun crearUsuario(
        idRol: Int,
        primerNombre: String,
        segundoNombre: String?,
        primerApellido: String,
        segundoApellido: String?,
        correo: String,
        carnet: String?,
        dui: String?,
        carrera: String?,
        contrasenia: String
    ): Boolean {
        val db = dbHelper.writableDatabase

        val nombreCompleto = listOf(
            primerNombre,
            segundoNombre.orEmpty(),
            primerApellido,
            segundoApellido.orEmpty()
        ).filter { it.isNotBlank() }
            .joinToString(" ")

        val values = ContentValues().apply {
            put("idRol", idRol)
            put("nombreUsuario", nombreCompleto)
            put("contrasenia", contrasenia)
            put("primerNombreUsuario", primerNombre)
            put("segundoNombreUsuario", segundoNombre)
            put("primerApellidoUsuario", primerApellido)
            put("segundoApellidoUsuario", segundoApellido)
            put("correoUsuario", correo)
            put("carnetUsuario", carnet)
            put("duiUsuario", dui)
            put("carreraUsuario", carrera)
            put("estadoUsuario", "Activo")
        }

        val id = db.insert("usuario", null, values)

        return id != -1L
    }

    fun cambiarEstadoUsuario(idUsuario: Int, nuevoEstado: String): Boolean {
        val db = dbHelper.writableDatabase

        val values = ContentValues().apply {
            put("estadoUsuario", nuevoEstado)
        }

        val filas = db.update(
            "usuario",
            values,
            "idUsuario = ?",
            arrayOf(idUsuario.toString())
        )

        return filas > 0
    }

    fun existeCorreo(correo: String): Boolean {
        val db = dbHelper.readableDatabase

        val cursor = db.rawQuery(
            """
            SELECT COUNT(*) AS Total
            FROM usuario
            WHERE UPPER(correoUsuario) = UPPER(?)
            """.trimIndent(),
            arrayOf(correo.trim())
        )

        val existe = obtenerTotal(cursor) > 0
        cursor.close()
        return existe
    }

    fun existeCarnet(carnet: String): Boolean {
        val db = dbHelper.readableDatabase

        val cursor = db.rawQuery(
            """
            SELECT COUNT(*) AS Total
            FROM usuario
            WHERE UPPER(carnetUsuario) = UPPER(?)
            """.trimIndent(),
            arrayOf(carnet.trim())
        )

        val existe = obtenerTotal(cursor) > 0
        cursor.close()
        return existe
    }

    private fun obtenerTotal(cursor: Cursor): Int {
        var total = 0

        if (cursor.moveToFirst()) {
            total = cursor.getInt(cursor.getColumnIndexOrThrow("Total"))
        }

        return total
    }

    private fun getString(cursor: Cursor, column: String): String {
        val index = cursor.getColumnIndexOrThrow(column)
        return if (cursor.isNull(index)) "" else cursor.getString(index)
    }

    private fun getNullableString(cursor: Cursor, column: String): String? {
        val index = cursor.getColumnIndexOrThrow(column)
        val value = if (cursor.isNull(index)) null else cursor.getString(index)
        return value?.ifBlank { null }
    }
}