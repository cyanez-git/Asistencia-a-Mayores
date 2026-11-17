package ar.asistencia.reloj

import android.content.Context

/**
 * Configuración de conexión con Home Assistant (broker MQTT).
 *
 * Se carga desde la notebook con ADB, para no tipear en el reloj:
 *   adb shell am start -n ar.asistencia.reloj/.presentation.MainActivity \
 *     --es host 192.168.0.235 --es usuario reloj --es clave <clave>
 *
 * La clave queda solo en el reloj; no se guarda en el repositorio.
 */
class Config(context: Context) {
    private val prefs = context.getSharedPreferences("config", Context.MODE_PRIVATE)

    var host: String
        get() = prefs.getString("host", "") ?: ""
        set(v) = prefs.edit().putString("host", v).apply()

    var puerto: Int
        get() = prefs.getInt("puerto", 1883)
        set(v) = prefs.edit().putInt("puerto", v).apply()

    var usuario: String
        get() = prefs.getString("usuario", "") ?: ""
        set(v) = prefs.edit().putString("usuario", v).apply()

    var clave: String
        get() = prefs.getString("clave", "") ?: ""
        set(v) = prefs.edit().putString("clave", v).apply()

    /** Identificador del reloj en Home Assistant (permite varios relojes). */
    var id: String
        get() = prefs.getString("id", "reloj1") ?: "reloj1"
        set(v) = prefs.edit().putString("id", v).apply()

    val completa: Boolean get() = host.isNotBlank()
}
