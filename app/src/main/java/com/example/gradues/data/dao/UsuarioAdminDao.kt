package com.example.gradues.data.dao

import android.content.ContentValues
import android.database.Cursor
import com.example.gradues.data.db.DatabaseHelper
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