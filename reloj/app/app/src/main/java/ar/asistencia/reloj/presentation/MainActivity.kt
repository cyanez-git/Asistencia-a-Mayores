package ar.asistencia.reloj.presentation

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.core.content.ContextCompat
import androidx.wear.compose.foundation.lazy.TransformingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberTransformingLazyColumnState
import androidx.wear.compose.material3.AppScaffold
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.ListHeader
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.Text
import ar.asistencia.reloj.Config
import ar.asistencia.reloj.Estado
import ar.asistencia.reloj.presentation.theme.AsistenteRelojTheme
import ar.asistencia.reloj.sensores.SensorService

class MainActivity : ComponentActivity() {

    private val pedirPermisos =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { resultado ->
            val negados = resultado.filterValues { !it }.keys.map { it.substringAfterLast('.') }
            Estado.error = if (negados.isEmpty()) "" else "Permisos negados: ${negados.joinToString()}"
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        guardarConfiguracion(intent)
        pedirPermisosFaltantes()
        setContent {
            AsistenteRelojTheme { Pantalla(::iniciar, ::detener) }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        guardarConfiguracion(intent)
    }

    /** Toma la configuración enviada desde la notebook con "adb shell am start ... --es ...". */
    private fun guardarConfiguracion(intent: Intent?) {
        val extras = intent?.extras ?: return
        val config = Config(this)
        extras.getString("host")?.let { config.host = it }
        extras.getString("puerto")?.toIntOrNull()?.let { config.puerto = it }
        extras.getString("usuario")?.let { config.usuario = it }
        extras.getString("clave")?.let { config.clave = it }
        extras.getString("id")?.let { config.id = it }
        if (extras.containsKey("host")) Estado.mqtt = "Configurado: ${config.host}"
    }

    private fun pedirPermisosFaltantes() {
        val permisos = buildList {
            add(Manifest.permission.ACTIVITY_RECOGNITION)
            if (Build.VERSION.SDK_INT >= 33) add(Manifest.permission.POST_NOTIFICATIONS)
            if (Build.VERSION.SDK_INT >= 36) {
                add("android.permission.health.READ_HEART_RATE")
            } else {
                add(Manifest.permission.BODY_SENSORS)
            }
            add("com.samsung.android.hardware.sensormanager.permission.READ_ADDITIONAL_HEALTH_DATA")
        }.filter { ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED }
        if (permisos.isNotEmpty()) pedirPermisos.launch(permisos.toTypedArray())
    }

    private fun iniciar() {
        ContextCompat.startForegroundService(this, Intent(this, SensorService::class.java))
    }

    private fun detener() {
        stopService(Intent(this, SensorService::class.java))
    }
}

@Composable
private fun Pantalla(onIniciar: () -> Unit, onDetener: () -> Unit) {
    AppScaffold {
        val estadoLista = rememberTransformingLazyColumnState()
        ScreenScaffold(scrollState = estadoLista) { padding ->
            TransformingLazyColumn(contentPadding = padding, state = estadoLista) {
                item { ListHeader { Text("Asistente") } }
                item { Linea("Servicio: ${Estado.servicio}") }
                item { Linea("Sensores: ${Estado.sensores}") }
                item { Linea("Envío: ${Estado.mqtt}") }
                item { Linea("Pulso: ${Estado.pulso}") }
                item { Linea("Temp.: ${Estado.temperatura}") }
                item { Linea("Acel.: ${Estado.acelerometro}") }
                if (Estado.error.isNotBlank()) item { Linea("⚠ ${Estado.error}") }
                item {
                    Button(onClick = onIniciar, modifier = Modifier.fillMaxWidth()) {
                        Text("Iniciar")
                    }
                }
                item {
                    Button(onClick = onDetener, modifier = Modifier.fillMaxWidth()) {
                        Text("Detener")
                    }
                }
            }
        }
    }
}

@Composable
private fun Linea(texto: String) {
    Text(text = texto, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
}
