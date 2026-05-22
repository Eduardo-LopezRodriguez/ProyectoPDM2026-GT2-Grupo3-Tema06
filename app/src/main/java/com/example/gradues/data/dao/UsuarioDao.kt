package com.example.gradues.data.dao

import com.example.gradues.data.db.DatabaseHelper
import com.example.gradues.data.entities.Usuario

class UsuarioDao(private val dbHelper: DatabaseHelper) {

    fun login(idUsuario: String, contra: String): Usuario? {
        val db = dbHelper.readableDatabase
        val datoLogin = idUsuario.trim()

        val cursor = db.rawQuery(
            """
            SELECT
                COALESCE(u.carnetUsuario, CAST(u.idUsuario AS TEXT)) AS IdUsuario,
                u.nombreUsuario AS NombreUsuario,
                u.contrasenia AS Contra,
                r.nombreRol AS NombreRol
            FROM usuario u
            INNER JOIN rol r
                ON r.idRol = u.idRol
            WHERE (
                    CAST(u.idUsuario AS TEXT) = ?
                    OR UPPER(u.carnetUsuario) = UPPER(?)
                    OR UPPER(u.correoUsuario) = UPPER(?)
                    OR UPPER(u.nombreUsuario) = UPPER(?)
                  )
              AND u.contrasenia = ?
              AND COALESCE(u.estadoUsuario, 'Activo') = 'Activo'
            LIMIT 1
            """.trimIndent(),
            arrayOf(
                datoLogin,
                datoLogin,
                datoLogin,
                datoLogin,
                contra.trim()
            )
        )

        var usuario: Usuario? = null

        if (cursor.moveToFirst()) {
            usuario = Usuario(
                IdUsuario = cursor.getString(cursor.getColumnIndexOrThrow("IdUsuario")),
                NombreUsuario = cursor.getString(cursor.getColumnIndexOrThrow("NombreUsuario")),
                Contra = cursor.getString(cursor.getColumnIndexOrThrow("Contra")),
                NombreRol = cursor.getString(cursor.getColumnIndexOrThrow("NombreRol"))
            )
        }

        cursor.close()
        return usuario
    }
}