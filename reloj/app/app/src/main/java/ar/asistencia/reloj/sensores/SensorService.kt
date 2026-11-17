package ar.asistencia.reloj.sensores

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.net.wifi.WifiManager
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.PowerManager
import android.util.Log
import ar.asistencia.reloj.Config
import ar.asistencia.reloj.Estado
import ar.asistencia.reloj.R
import ar.asistencia.reloj.mqtt.Publicador
import com.samsung.android.service.health.tracking.ConnectionListener
import com.samsung.android.service.health.tracking.HealthTracker
import com.samsung.android.service.health.tracking.HealthTrackerException
import com.samsung.android.service.health.tracking.HealthTrackingService
import com.samsung.android.service.health.tracking.data.DataPoint
import com.samsung.android.service.health.tracking.data.HealthTrackerType
import com.samsung.android.service.health.tracking.data.ValueKey
import org.json.JSONObject
import kotlin.math.sqrt

/**
 * Servicio en primer plano que lee los sensores del reloj con el
 * Samsung Health Sensor SDK y envía un resumen a Home Assistant cada
 * [INTERVALO_MS] milisegundos.
 *
 * Sensores (si el reloj los soporta):
 *  - HEART_RATE_CONTINUOUS: pulso, estado y lista de intervalos entre latidos (IBI)
 *  - SKIN_TEMPERATURE_CONTINUOUS: temperatura de piel y ambiente
 *  - ACCELEROMETER_CONTINUOUS: valores crudos X/Y/Z (~25 Hz); se envía un resumen
 */
class SensorService : Service() {

    private lateinit var config: Config
    private lateinit var publicador: Publicador
    private var salud: HealthTrackingService? = null
    private val trackers = mutableListOf<HealthTracker>()
    private val handler = Handler(Looper.getMainLooper())
    private var wakeLock: PowerManager.WakeLock? = null
    private var wifiLock: WifiManager.WifiLock? = null

    // Últimos valores y acumulados de la ventana actual (protegidos por "this")
    private var pulso: Int? = null
    private var pulsoEstado: Int? = null
    private var ultimoIbi: Int? = null
    private var tempPiel: Float? = null
    private var tempAmbiente: Float? = null
    private var tempEstado: Int? = null
    private var accMax = 0.0
    private var accSuma = 0.0
    private var accMuestras = 0

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        config = Config(this)
        publicador = Publicador(config)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        try {
            iniciarPrimerPlano()
        } catch (e: Exception) {
            // Android solo permite iniciar un servicio de salud con la app en primer plano.
            Log.e(TAG, "No se pudo pasar a primer plano", e)
            Estado.servicio = "Detenido"
            Estado.error = "Abrí la app y tocá Iniciar con la pantalla encendida"
            stopSelf()
            return START_NOT_STICKY
        }
        if (salud != null) return START_NOT_STICKY // ya estaba en marcha
        tomarLocks()
        if (config.completa) publicador.conectar() else Estado.mqtt = "Falta configurar el host"
        conectarSalud()
        handler.postDelayed(envioPeriodico, INTERVALO_MS)
        Estado.servicio = "En marcha"
        // No se reinicia solo: Android no permitiría pasarlo a primer plano desde segundo plano.
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        handler.removeCallbacksAndMessages(null)
        trackers.forEach { runCatching { it.unsetEventListener() } }
        trackers.clear()
        runCatching { salud?.disconnectService() }
        publicador.desconectar()
        wakeLock?.takeIf { it.isHeld }?.release()
        wifiLock?.takeIf { it.isHeld }?.release()
        Estado.servicio = "Detenido"
        Estado.sensores = "—"
        super.onDestroy()
    }

    // ---- Samsung Health Sensor SDK ----

    private fun conectarSalud() {
        Estado.sensores = "Conectando al servicio de Samsung…"
        val listener = object : ConnectionListener {
            override fun onConnectionSuccess() = iniciarTrackers()
            override fun onConnectionEnded() {
                Estado.sensores = "Servicio de Samsung desconectado"
            }
            override fun onConnectionFailed(e: HealthTrackerException) {
                val motivo = when (e.errorCode) {
                    HealthTrackerException.PACKAGE_NOT_INSTALLED -> "falta el servicio de sensores de Samsung"
                    HealthTrackerException.OLD_PLATFORM_VERSION -> "el servicio de sensores de Samsung está desactualizado"
                    else -> e.message ?: "error ${e.errorCode}"
                }
                Estado.sensores = "No conecta: $motivo"
                Log.e(TAG, "Fallo de conexión con Samsung Health", e)
            }
        }
        salud = HealthTrackingService(listener, applicationContext).also { it.connectService() }
    }

    private fun iniciarTrackers() {
        val servicio = salud ?: return
        // Si el servicio de Samsung se reconecta, se vuelve a llamar: evitar listeners duplicados.
        trackers.forEach { runCatching { it.unsetEventListener() } }
        trackers.clear()
        val soportados = servicio.trackingCapability.supportHealthTrackerTypes
        Log.i(TAG, "Trackers soportados: $soportados")
        val deseados = listOf(
            HealthTrackerType.HEART_RATE_CONTINUOUS,
            HealthTrackerType.SKIN_TEMPERATURE_CONTINUOUS,
            HealthTrackerType.ACCELEROMETER_CONTINUOUS,
        )
        val activos = mutableListOf<String>()
        for (tipo in deseados) {
            if (tipo !in soportados) {
                Log.w(TAG, "No soportado: $tipo")
                continue
            }
            try {
                val tracker = servicio.getHealthTracker(tipo)
                tracker.setEventListener(listenerPara(tipo))
                trackers += tracker
                activos += tipo.name
            } catch (e: Exception) {
                Log.e(TAG, "No se pudo iniciar $tipo", e)
                Estado.error = "Error al iniciar $tipo: ${e.message}"
            }
        }
        Estado.sensores = if (activos.isEmpty()) "Ningún sensor disponible"
        else "Activos: " + activos.joinToString { it.substringBefore("_") }
    }

    private fun listenerPara(tipo: HealthTrackerType) = object : HealthTracker.TrackerEventListener {
        override fun onDataReceived(puntos: List<DataPoint>) {
            when (tipo) {
                HealthTrackerType.HEART_RATE_CONTINUOUS -> puntos.forEach(::procesarPulso)
                HealthTrackerType.SKIN_TEMPERATURE_CONTINUOUS -> puntos.forEach(::procesarTemperatura)
                HealthTrackerType.ACCELEROMETER_CONTINUOUS -> puntos.forEach(::procesarAcelerometro)
                else -> Unit
            }
        }
        override fun onFlushCompleted() {}
        override fun onError(error: HealthTracker.TrackerError) {
            val texto = when (error) {
                HealthTracker.TrackerError.PERMISSION_ERROR -> "faltan permisos"
                HealthTracker.TrackerError.SDK_POLICY_ERROR ->
                    "política del SDK: activar el modo desarrollador de Health Sensor Service"
                else -> error.name
            }
            Estado.error = "$tipo: $texto"
            Log.e(TAG, "Error en $tipo: $error")
        }
    }

    @Synchronized
    private fun procesarPulso(p: DataPoint) {
        pulsoEstado = p.getValue(ValueKey.HeartRateSet.HEART_RATE_STATUS)
        pulso = p.getValue(ValueKey.HeartRateSet.HEART_RATE)
        val ibis = p.getValue(ValueKey.HeartRateSet.IBI_LIST)
        val estados = p.getValue(ValueKey.HeartRateSet.IBI_STATUS_LIST)
        if (!ibis.isNullOrEmpty()) {
            // Último intervalo válido (estado 0 = válido)
            val valido = ibis.indices.lastOrNull { estados?.getOrNull(it) == 0 }
            if (valido != null) ultimoIbi = ibis[valido]
        }
    }

    @Synchronized
    private fun procesarTemperatura(p: DataPoint) {
        tempEstado = p.getValue(ValueKey.SkinTemperatureSet.STATUS)
        tempPiel = p.getValue(ValueKey.SkinTemperatureSet.OBJECT_TEMPERATURE)
        tempAmbiente = p.getValue(ValueKey.SkinTemperatureSet.AMBIENT_TEMPERATURE)
    }

    @Synchronized
    private fun procesarAcelerometro(p: DataPoint) {
        val x = (p.getValue(ValueKey.AccelerometerSet.ACCELEROMETER_X) ?: return).toDouble()
        val y = (p.getValue(ValueKey.AccelerometerSet.ACCELEROMETER_Y) ?: return).toDouble()
        val z = (p.getValue(ValueKey.AccelerometerSet.ACCELEROMETER_Z) ?: return).toDouble()
        val magnitud = sqrt(x * x + y * y + z * z)
        if (magnitud > accMax) accMax = magnitud
        accSuma += magnitud
        accMuestras++
    }

    // ---- Envío periódico ----

    private val envioPeriodico = object : Runnable {
        override fun run() {
            enviarResumen()
            handler.postDelayed(this, INTERVALO_MS)
        }
    }

    @Synchronized
    private fun enviarResumen() {
        val datos = JSONObject()
            .put("ts", System.currentTimeMillis())
            .put("pulso", pulso.valido(pulsoEstado == ESTADO_PULSO_OK))
            .put("pulso_estado", pulsoEstado ?: JSONObject.NULL)
            .put("ibi", ultimoIbi ?: JSONObject.NULL)
            .put("temp_piel", tempPiel.redondeado())
            .put("temp_ambiente", tempAmbiente.redondeado())
            .put("temp_estado", tempEstado ?: JSONObject.NULL)
            .put("acc_max", if (accMuestras > 0) accMax.toInt() else JSONObject.NULL)
            .put("acc_media", if (accMuestras > 0) (accSuma / accMuestras).toInt() else JSONObject.NULL)
            .put("acc_muestras", accMuestras)
        publicador.publicar(datos)

        Estado.pulso = pulso?.let { "$it bpm (estado ${pulsoEstado})" } ?: "—"
        Estado.temperatura = tempPiel?.let { "piel %.1f °C (estado %s)".format(it, tempEstado) } ?: "—"
        Estado.acelerometro = if (accMuestras > 0)
            "$accMuestras muestras, máx ${accMax.toInt()}" else "sin datos"

        accMax = 0.0; accSuma = 0.0; accMuestras = 0
    }

    // ---- Infraestructura del servicio ----

    private fun iniciarPrimerPlano() {
        val nm = getSystemService(NotificationManager::class.java)
        nm.createNotificationChannel(
            NotificationChannel(CANAL, "Sensores", NotificationManager.IMPORTANCE_LOW)
        )
        val notificacion = Notification.Builder(this, CANAL)
            .setContentTitle(getString(R.string.app_name))
            .setContentText("Enviando datos de los sensores")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setOngoing(true)
            .build()
        startForeground(1, notificacion, ServiceInfo.FOREGROUND_SERVICE_TYPE_HEALTH)
    }

    @SuppressLint("WakelockTimeout")
    private fun tomarLocks() {
        // Etapa de prueba: priorizamos recibir datos sobre la batería.
        val pm = getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "asistencia:sensores")
            .apply { acquire() }
        val wm = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        @Suppress("DEPRECATION")
        wifiLock = wm.createWifiLock(WifiManager.WIFI_MODE_FULL_HIGH_PERF, "asistencia:wifi")
            .apply { acquire() }
    }

    private fun Int?.valido(ok: Boolean): Any = if (this != null && ok) this else JSONObject.NULL
    private fun Float?.redondeado(): Any =
        this?.let { Math.round(it * 10) / 10.0 } ?: JSONObject.NULL

    companion object {
        private const val TAG = "SensorService"
        private const val CANAL = "sensores"
        private const val INTERVALO_MS = 5_000L
        /** Según el SDK, estado 1 = medición de pulso correcta. */
        private const val ESTADO_PULSO_OK = 1
    }
}
