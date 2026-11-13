# Hardware: candidatos por función
**Fecha:** 2026-11-13
**Base:** [03_Requisitos.md](03_Requisitos.md), sección 3 (capacidades de percepción)

Regla de trabajo: **máximo 2 candidatos por función**, se compra **1 unidad** de cada uno, se prueba y se decide en fecha. Nada se compra en cantidad hasta que la prueba lo valide.

Los ítems marcados **⚠ validar** son supuestos que la prueba tiene que confirmar antes de decidir.

---

## 1. Criterios (salen de los requisitos)

| Criterio | Por qué | Requisito |
|---|---|---|
| Los datos llegan al hub de la casa **sin pasar por la nube del fabricante** | El camino de emergencia funciona sin internet | CONF-01 |
| **API oficial o protocolo estándar** (Zigbee, Matter, RTSP/ONVIF, ESPHome, SDK oficial) | Integrar sin ingeniería inversa y poder cambiar de fabricante | ESC-03 |
| **Prueba real con 1 unidad** antes de comprar más | Validar en el uso, no en la ficha técnica | — |
| Disponible en Argentina / homologación ENACOM | **No excluye para el piloto.** Sí es requisito para el producto | — |

El precio **no** es criterio en esta etapa.

---

## 2. Vista general de la instalación

```
                         ┌──────────── Internet ────────────┐
                         │   (conversación IA, WhatsApp)    │
                         └───────────────┬──────────────────┘
                                         │  + canal celular de respaldo
┌────────────────────────────────────────┴──────────────────────────────┐
│ HUB LOCAL (en UPS)                                                    │
│  detección de caídas · visión · voz local · reglas de emergencia      │
│  coordinador Zigbee · módem celular con voz/SMS                       │
└──┬──────────────┬──────────────┬───────────────┬──────────────┬───────┘
   │ WiFi         │ PoE/WiFi     │ Zigbee        │ WiFi/BLE     │ WiFi
   ▼              ▼              ▼               ▼              ▼
Satélites de   Cámaras        Sensores del    Reloj de la    TV / pantalla
voz (todos     (ambientes     hogar + botones persona        (contenido,
los ambientes) comunes)       SOS + radar                    videollamada)
                              (baño, dormit.)
```

---

## 3. Candidatos por función

### 3.1 Hub local (cerebro de la casa)

Necesita correr en la casa: detección de personas y caídas en video, reconocimiento facial y de voz, transcripción de voz, reglas de emergencia y las integraciones. Eso pide **aceleración por GPU**.

| | Candidato A | Candidato B |
|---|---|---|
| **Equipo** | Mini PC x86 con GPU NVIDIA dedicada (clase RTX 4060/5060, 8–16 GB VRAM) | NVIDIA Jetson Orin (AGX Orin 64 GB u Orin NX 16 GB) |
| **A favor** | Máxima compatibilidad de software (Home Assistant, Frigate, Whisper, modelos locales); fácil de conseguir y reemplazar | Bajo consumo (más autonomía con UPS); diseñado para visión en el borde |
| **En contra** | Consumo alto (50–150 W): pesa en la autonomía sin luz | Ecosistema ARM más acotado; algunos modelos requieren adaptación |

**Recomendación inicial:** A, por flexibilidad mientras definimos el software. Medir consumo real para dimensionar la UPS.

### 3.2 Respaldo de energía y comunicación

| Función | Candidato A | Candidato B |
|---|---|---|
| **Energía** (CONF-02: 8 h) | Estación de energía LiFePO4 con función UPS (~1 kWh; p. ej., EcoFlow o Bluetti) | UPS online con baterías externas |
| **Canal celular** (CONF-03) | Router 4G industrial con failover (p. ej., Teltonika RUT241) + **módem LTE con voz** para llamadas y SMS (p. ej., SIMCom SIM7600) | Router 4G con failover + llamadas por VoIP sobre la red celular |

Notas:
- Cámaras por **PoE** desde un switch conectado a la UPS: siguen funcionando sin luz.
- Satélites de voz y radares en enchufes comunes se apagan sin luz. **Definir modo degradado:** sin luz, la detección se sostiene con el reloj (batería propia), los sensores Zigbee (a pila) y las cámaras PoE. ⚠ validar si alcanza o si hace falta respaldo para algunos satélites (baño, dormitorio).
- Router y módem: ⚠ validar llamada de voz real desde Argentina con SIM local.

### 3.3 Oídos y voz (satélites por ambiente)

| | Candidato A | Candidato B |
|---|---|---|
| **Equipo** | Home Assistant Voice Preview Edition | FutureProofHomes Satellite1 |
| **A favor** | Abierto (ESPHome), integración nativa, local | Mejor arreglo de micrófonos y amplificador para parlante externo (sirve también para música) |
| **En contra** | Parlante chico: no sirve para música; captación a distancia ⚠ validar | Menos difundido; ⚠ validar disponibilidad |

**Qué medir en la prueba:**
- Captación de voz baja a 3–4 metros y con la tele prendida.
- Latencia total de la conversación (VOZ-05: menos de 1,5 s). Esto depende sobre todo del software y del servicio de IA, no solo del satélite.
- Calidad de audio para música (VOZ-13).
- Funcionamiento en el baño (humedad).

### 3.4 Ojos (cámaras)

Cámaras con **RTSP/ONVIF** y procesamiento en el hub (p. ej., con Frigate), sin depender de la nube del fabricante.

| | Candidato A | Candidato B |
|---|---|---|
| **Equipo** | Reolink de interior, PoE | Hikvision o Dahua de interior, PoE (línea domo) |
| **A favor** | RTSP local simple, buena visión nocturna, fácil de conseguir | Muy difundidas en Argentina, ONVIF completo, amplia oferta |
| **En contra** | Algunos modelos solo WiFi | Configuración más técnica |

**Qué medir:** detección de persona en el piso de día y de noche, ángulo para cubrir un ambiente completo con 1 cámara, reconocimiento facial de visitas en la entrada.

### 3.5 Sensor sin imagen (baño y dormitorio)

Tiene que detectar **caída e inmovilidad**, no solo presencia. Tecnología: **radar mmWave**.

| | Candidato A | Candidato B |
|---|---|---|
| **Equipo** | Aqara FP2 | Seeed Studio MR60FDA2 (radar 60 GHz de caídas, kit con ESP32 y ESPHome) |
| **A favor** | Producto terminado, zonas configurables, detección de caídas, montaje en techo | Abierto y local por diseño (ESPHome), específico para caídas |
| **En contra** | ⚠ validar que la detección de caídas llegue al hub **sin la nube de Aqara** | Más "kit" que producto: carcasa y montaje a resolver |

**Qué medir:** caídas simuladas en baño y al lado de la cama; persona acostada en la cama vs. en el piso; falsos positivos durante 2 semanas; comportamiento con vapor de ducha.

### 3.6 Reloj de la persona (wearable)

El dispositivo más exigente: pulso continuo, caídas, temperatura, botón de ayuda, ducha, comodidad, batería y datos directo al hub.

| | Candidato A | Candidato B |
|---|---|---|
| **Equipo** | Samsung Galaxy Watch (Wear OS) con **app propia** usando el SDK oficial de sensores de Samsung | Bangle.js 2 (firmware abierto) |
| **A favor** | Pulso continuo, **temperatura de piel**, acelerómetro, 5 ATM, se vende oficialmente en Argentina; envía datos por WiFi directo al hub | Totalmente abierto; batería de semanas |
| **En contra** | Batería de 1–2 días (USO-02 pide 5) ⚠ validar acceso al SDK para nuestra app | Sin temperatura de piel; pulso menos preciso; no apto para ducha prolongada; importación |

**Propuesta para la batería del candidato A:** **dos relojes en rotación**. Uno se carga mientras se usa el otro, con el cambio integrado a una rutina diaria (p. ej., al despertarse). El asistente lo recuerda. Sin restricción de precio, resuelve USO-02 sin sacrificar sensores.

**Qué medir:** detección de caídas propia en el reloj; precisión del pulso en reposo y con actividad; lectura de temperatura; envío al hub sin la nube de Samsung; consumo real con medición continua; si la persona lo usa de noche.

### 3.7 Botones SOS fijos y sensores del hogar (Zigbee)

Un solo coordinador Zigbee en el hub y dispositivos de cualquier fabricante compatible.

| Función | Candidato A | Candidato B |
|---|---|---|
| **Coordinador Zigbee** | SMLIGHT SLZB-06 (red/PoE) | Sonoff ZBDongle-E (USB) |
| **Botón SOS** (baño, al lado de la cama) | Aqara Wireless Mini Switch | Sonoff SNZB-01P |
| **Puertas** | Aqara Door & Window Sensor | Sonoff SNZB-04P |
| **Pérdida de agua** | Aqara Water Leak Sensor | Sonoff SNZB-05P |
| **Temperatura y humedad** | Aqara T&H Sensor | Sonoff SNZB-02D |
| **Gas / cocina** | Detector de gas Zigbee (p. ej., Heiman) | Detector de gas Zigbee de otro fabricante |

Notas:
- La detección de "hornalla prendida sin nadie" sale de **combinar** el detector de gas o temperatura en la cocina con la presencia (cámara o radar). ⚠ validar con la cocina real.
- Botón del baño: ⚠ validar resistencia a salpicaduras.

### 3.8 TV, pantalla y contexto de medios

| | Candidato A | Candidato B |
|---|---|---|
| **Equipo** | Google TV Streamer en la tele que ya tiene la persona | Pantalla propia (tablet de 11–13" en soporte) con nuestra app |
| **A favor** | Usa la tele de siempre; YouTube y apps nativas; se controla desde el hub (Cast/ADB); el hub sabe qué se está reproduciendo | Sirve para **videollamadas con la familia**, fotos y mensajes; independiente de la tele |
| **En contra** | No sirve para videollamadas | Una pantalla más en la casa |

Pueden convivir: A para ver contenido en la tele y B para videollamadas y fotos. Propongo probar A primero y evaluar B como complemento.

**Contexto de medios (VOZ-12):** no requiere hardware nuevo. Los satélites ya escuchan la tele o la radio. El hub transcribe e identifica el contenido, y con Google TV además sabe qué app o programa está activo.

**Música (VOZ-13):** por el satélite si tiene parlante adecuado (candidato B de 3.3) o por un parlante con integración local.

---

## 4. Plan de pruebas y fechas

| Paso | Qué | Fecha propuesta |
|---|---|---|
| 1 | Compra de 1 unidad de cada candidato (o del recomendado, si se acuerda) | hasta 2026-11-27 |
| 2 | Banco de pruebas: hub + 1 satélite + 1 cámara + 1 radar + reloj + Zigbee, en una casa real | 2026-11-27 a 2026-12-04 |
| 3 | Prueba de 2 semanas con las mediciones de cada sección | 2026-12-04 a 2026-12-18 |
| 4 | **Decisión por función** y registro en este documento | 2026-12-18 |

---

## 5. Decisiones que necesitamos ahora

1. ¿Compramos **los dos candidatos** de cada función o solo el recomendado y el segundo si el primero falla?
2. ¿En qué **casa real** hacemos el banco de pruebas? (¿la tuya, o la de alguno de los casos?)
3. ¿Estás de acuerdo con la propuesta de **dos relojes en rotación**?
