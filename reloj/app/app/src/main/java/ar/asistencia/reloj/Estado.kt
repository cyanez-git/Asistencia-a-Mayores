package ar.asistencia.reloj

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/** Estado visible en la pantalla del reloj. Lo actualiza el servicio. */
object Estado {
    var servicio by mutableStateOf("Detenido")
    var sensores by mutableStateOf("—")
    var mqtt by mutableStateOf("Sin conectar")
    var pulso by mutableStateOf("—")
    var temperatura by mutableStateOf("—")
    var acelerometro by mutableStateOf("—")
    var error by mutableStateOf("")
}
