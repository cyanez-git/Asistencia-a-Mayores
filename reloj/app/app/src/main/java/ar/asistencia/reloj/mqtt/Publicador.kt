package ar.asistencia.reloj.mqtt

import android.util.Log
import ar.asistencia.reloj.Config
import ar.asistencia.reloj.Estado
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended
import org.eclipse.paho.client.mqttv3.MqttClient
import org.eclipse.paho.client.mqttv3.MqttConnectOptions
import org.eclipse.paho.client.mqttv3.MqttMessage
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence
import org.json.JSONObject
import java.util.concurrent.Executors

/**
 * Envía los datos del reloj a Home Assistant por MQTT.
 *
 * Usa "MQTT discovery": al conectarse publica la descripción de cada sensor y
 * Home Assistant los crea solo (pulso, temperatura de piel, etc.).
 *
 * Tópicos:
 *   asistencia/reloj/<id>/estado      JSON con los valores (cada envío)
 *   asistencia/reloj/<id>/disponible  "online" / "offline" (último deseo)
 */
class Publicador(private val config: Config) {

    private val ejecutor = Executors.newSingleThreadExecutor()
    private var cliente: MqttClient? = null

    private val id get() = config.id
    private val base get() = "asistencia/reloj/$id"
    private val topicoEstado get() = "$base/estado"
    private val topicoDisponible get() = "$base/disponible"

    fun conectar() = ejecutor.execute {
        try {
            val url = "tcp://${config.host}:${config.puerto}"
            val c = MqttClient(url, "asistencia-$id", MemoryPersistence())
            val opciones = MqttConnectOptions().apply {
                isAutomaticReconnect = true
                isCleanSession = true
                keepAliveInterval = 30
                connectionTimeout = 10
                if (config.usuario.isNotBlank()) {
                    userName = config.usuario
                    password = config.clave.toCharArray()
                }
                setWill(topicoDisponible, "offline".toByteArray(), 1, true)
            }
            c.setCallback(object : MqttCallbackExtended {
                override fun connectComplete(reconnect: Boolean, serverURI: String?) {
                    Estado.mqtt = "Conectado"
                    ejecutor.execute { anunciar() }
                }
                override fun connectionLost(cause: Throwable?) {
                    Estado.mqtt = "Reconectando…"
                    Log.w(TAG, "Conexión MQTT perdida", cause)
                }
                override fun messageArrived(topic: String?, message: MqttMessage?) {}
                override fun deliveryComplete(token: IMqttDeliveryToken?) {}
            })
            Estado.mqtt = "Conectando a ${config.host}…"
            c.connect(opciones)
            cliente = c
            // connectComplete() encola anunciar(): corre apenas termina este bloque,
            // con "cliente" ya asignado.
        } catch (e: Exception) {
            Log.e(TAG, "No se pudo conectar a MQTT", e)
            Estado.mqtt = "Error: ${e.message ?: e.javaClass.simpleName}"
        }
    }

    fun publicar(datos: JSONObject) = ejecutor.execute {
        val c = cliente ?: return@execute
        if (!c.isConnected) return@execute
        try {
            c.publish(topicoEstado, datos.toString().toByteArray(), 0, false)
        } catch (e: Exception) {
            Log.w(TAG, "Error al publicar", e)
        }
    }

    fun desconectar() = ejecutor.execute {
        try {
            cliente?.let {
                if (it.isConnected) {
                    it.publish(topicoDisponible, "offline".toByteArray(), 1, true)
                    it.disconnect()
                }
                it.close()
            }
        } catch (e: Exception) {
            Log.w(TAG, "Error al desconectar", e)
        }
        cliente = null
        Estado.mqtt = "Sin conectar"
    }

    /** Publica la descripción de los sensores para que Home Assistant los cree solos. */
    private fun anunciar() {
        val c = cliente ?: return
        val dispositivo = JSONObject()
            .put("identifiers", listOf("asistencia_$id").toJsonArray())
            .put("name", "Reloj ($id)")
            .put("manufacturer", "Samsung")
            .put("model", "Galaxy Watch")

        val sensores = listOf(
            Sensor("pulso", "Pulso", "bpm", null, "mdi:heart-pulse"),
            Sensor("pulso_estado", "Estado del pulso", null, null, "mdi:heart-cog"),
            Sensor("ibi", "Intervalo entre latidos", "ms", null, "mdi:timer-outline"),
            Sensor("temp_piel", "Temperatura de piel", "°C", "temperature", null),
            Sensor("temp_ambiente", "Temperatura ambiente (reloj)", "°C", "temperature", null),
            Sensor("temp_estado", "Estado de temperatura", null, null, "mdi:thermometer-alert"),
            Sensor("acc_max", "Aceleración máxima", null, null, "mdi:axis-arrow"),
            Sensor("acc_media", "Aceleración media", null, null, "mdi:axis-arrow"),
            Sensor("acc_muestras", "Muestras de acelerómetro", null, null, "mdi:counter"),
        )
        for (s in sensores) {
            val cfg = JSONObject()
                .put("name", s.nombre)
                .put("unique_id", "asistencia_${id}_${s.clave}")
                .put("state_topic", topicoEstado)
                .put("value_template", "{{ value_json.${s.clave} }}")
                .put("availability_topic", topicoDisponible)
                .put("device", dispositivo)
            s.unidad?.let { cfg.put("unit_of_measurement", it).put("state_class", "measurement") }
            s.clase?.let { cfg.put("device_class", it) }
            s.icono?.let { cfg.put("icon", it) }
            val topico = "homeassistant/sensor/asistencia_$id/${s.clave}/config"
            c.publish(topico, cfg.toString().toByteArray(), 1, true)
        }
        c.publish(topicoDisponible, "online".toByteArray(), 1, true)
    }

    private data class Sensor(
        val clave: String,
        val nombre: String,
        val unidad: String?,
        val clase: String?,
        val icono: String?,
    )

    private fun List<String>.toJsonArray() = org.json.JSONArray(this)

    companion object {
        private const val TAG = "Publicador"
    }
}
