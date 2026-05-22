package com.example.gradues.utils

import android.content.Context
import android.content.SharedPreferences

class SessionManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("gradues_session", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_ID = "session_id"
        private const val KEY_NOMBRE = "session_nombre"
        private const val KEY_ROL = "session_rol"
        private const val KEY_LOGGED = "session_logged"
    }

    fun guardarSesion(idUsuario: String, nombre: String, rol: String) {
        prefs.edit()
            .putString(KEY_ID, idUsuario)
            .putString(KEY_NOMBRE, nombre)
            .putString(KEY_ROL, rol)
            .putBoolean(KEY_LOGGED, true)
            .apply()
    }

    fun cerrarSesion() {
        prefs.edit().clear().apply()
    }

    fun estaLogueado(): Boolean {
        return prefs.getBoolean(KEY_LOGGED, false)
    }

    fun getIdUsuario(): String {
        return prefs.getString(KEY_ID, "") ?: ""
    }

    fun getNombre(): String {
        return prefs.getString(KEY_NOMBRE, "") ?: ""
    }

    fun getRol(): String {
        return prefs.getString(KEY_ROL, "") ?: ""
    }
}