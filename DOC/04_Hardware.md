# Hardware: candidatos por función
**Fecha:** 2026-11-13 (rev. con la alarma como base)
**Base:** [03_Requisitos.md](03_Requisitos.md), sección 3 (capacidades de percepción)

Regla de trabajo: **máximo 2 candidatos por función**, se compra **1 unidad** de cada uno, se prueba y se decide en fecha. Nada se compra en cantidad hasta que la prueba lo valide.

Los ítems marcados **⚠ validar** son supuestos que la prueba tiene que confirmar antes de decidir.

---

## 1. Premisa: la alarma como base

Gran parte del hardware que necesitamos ya existe, probado y homologado, en un **sistema de alarma**: batería de respaldo, comunicador celular con llamadas de voz, botones de pánico, sensores de puertas, agua, humo y gas, cámaras, y una red de instaladores en todo el país.

Por eso la instalación se piensa como **alarma + cuidado**:

- **La central de alarma** aporta sensores del hogar, botones de pánico, sirena, su propia batería y comunicador, y un **segundo camino de aviso** independiente del hub (CONF-06).
- **Nuestro hub** es el cerebro: recibe los eventos de la alarma, suma voz, cámaras, radares y reloj, cruza las señales y decide.
- **Si la casa ya tiene alarma** compatible, se integra. **Si tiene central de monitoreo**, se le envían también los eventos de cuidado (AVI-10). Ambas cosas son opcionales.
- Como beneficio adicional, la casa queda protegida cuando la persona no está.

**Lo que la alarma no cubre y sigue siendo nuestro:** asistente de voz, análisis en segundo plano, reloj (pulso, temperatura, caídas), radares de caídas en ambientes privados, y la lógica que cruza todo.

---

## 2. Criterios (salen de los requisitos)

| Criterio | Por qué | Requisito |
|---|---|---|
| Los datos llegan al hub **sin pasar por la nube del fabricante** | El camino de emergencia funciona sin internet | CONF-01 |
| **API oficial o protocolo estándar** (SIA DC-09, Contact ID, RTSP/ONVIF, ESPHome, Zigbee, SDK oficial) | Integrar sin ingeniería inversa y poder cambiar de fabricante | ESC-03 |
| **Central de alarma:** salida de eventos **SIA DC-09** hacia nuestro hub y/o **API local oficial** | Recibir sus sensores sin depender de su nube; no repetir el error de la ingeniería inversa | CONF-01, CONF-06 |
| **Prueba real con 1 unidad** antes de comprar más | Validar en el uso, no en la ficha técnica | — |
| Disponible en Argentina / homologación ENACOM | **No excluye para el piloto.** Sí es requisito para el producto | — |

El precio **no** es criterio en esta etapa.

---

## 3. Vista general de la instalación

```
            ┌───────────────────── Internet ─────────────────────┐
            │  (conversación IA, app de la familia, WhatsApp)    │
            └────────────┬──────────────────────────┬────────────┘
                         │                          │
┌────────────────────────┴──────────────┐   ┌───────┴─────────────────────┐
│ HUB LOCAL (en UPS)                    │   │ CENTRAL DE ALARMA           │
│ detección de caídas · visión · voz    │◄──┤ batería propia · 4G con voz │
│ reglas de emergencia · integraciones  │SIA│ sirena · monitoreo opcional │
│ módem celular con voz/SMS             │   └───────┬─────────────────────┘
└──┬─────────────┬───────────┬──────────┘           │ radio propia
   │ WiFi        │ PoE       │ WiFi / ESPHome       ▼
   ▼             ▼           ▼                 Sensores de la alarma:
Satélites     Cámaras     Radares de caída     puertas, agua, humo, gas,
de voz        (ambientes  (baño, dormitorio)   botones de pánico,
(todos los    comunes)    + reloj de la        movimiento
ambientes)                persona
                          + TV / pantalla
```

**Dos caminos de aviso independientes:**
1. **Hub** → llamadas con mensaje detallado, app, WhatsApp, monitoreo.
2. **Central de alarma** → si el hub falla, los botones de pánico y sensores críticos siguen avisando por su propio comunicador.

---

## 4. Candidatos por función

### 4.1 Central de alarma

| | Candidato A | Candidato B |
|---|---|---|
| **Equipo** | Ajax Hub 2 (4G) o Hub 2 Plus | Hikvision AX PRO |
| **A favor** | Envía eventos en **SIA DC-09** a cualquier receptor (nuestro hub puede ser uno); muy buena línea de sensores inalámbricos (incluye botón de pánico, agua, humo, puertas); batería y comunicador celular; difundida | API local (ISAPI); integra cámaras Hikvision (videoverificación) en el mismo ecosistema; muy difundida en Argentina |
| **En contra** | Radio propietaria: sus sensores solo funcionan con su central (aceptable si los eventos salen por SIA) | ⚠ validar alcance y estabilidad de la API local |

**⚠ Validar en ambos (crítico):**
- **¿Qué eventos salen con la alarma desarmada?** Las alarmas están pensadas para cuando no hay nadie. Con la persona en casa (alarma desarmada), necesitamos igual los eventos de botones de pánico, agua, humo, gas y, si es posible, puertas.
- **Sensores de movimiento como sensores de actividad:** muchos detectores de alarma se "duermen" unos minutos después de detectar y no reportan desarmados. Si no sirven para medir actividad, esa función la cubren cámaras y radares.
- Latencia desde el evento hasta que llega al hub.
- Cómo nuestro hub dispara un evento en la central (p. ej., una caída confirmada que debe ir al monitoreo) si se usa el comunicador de la alarma.

**Casa con alarma existente:** si es una marca con SIA DC-09 o API local, se integra. Si no, se evalúa caso por caso.

### 4.2 Hub local (cerebro de la casa)

Necesita correr en la casa: detección de personas y caídas en video, reconocimiento facial y de voz, transcripción, reglas de emergencia, receptor SIA e integraciones. Eso pide **aceleración por GPU**.

| | Candidato A | Candidato B |
|---|---|---|
| **Equipo** | Mini PC x86 con GPU NVIDIA dedicada (clase RTX 4060/5060, 8–16 GB VRAM) | NVIDIA Jetson Orin (AGX Orin 64 GB u Orin NX 16 GB) |
| **A favor** | Máxima compatibilidad de software (Home Assistant, Frigate, Whisper, modelos locales); fácil de conseguir y reemplazar | Bajo consumo (más autonomía con UPS); diseñado para visión en el borde |
| **En contra** | Consumo alto (50–150 W): pesa en la autonomía sin luz | Ecosistema ARM más acotado; algunos modelos requieren adaptación |

**Recomendación inicial:** A, por flexibilidad mientras definimos el software. Medir consumo real para dimensionar la UPS.

### 4.3 Energía y comunicación del hub

La alarma tiene su propia batería y comunicador. El hub necesita los suyos para que el camino principal (con mensajes detallados) también funcione sin luz ni internet.

| Función | Candidato A | Candidato B |
|---|---|---|
| **Energía** (CONF-02: 8 h) | Estación de energía LiFePO4 con función UPS (~1 kWh; p. ej., EcoFlow o Bluetti) | UPS online con baterías externas |
| **Canal celular** (CONF-03) | Router 4G con failover (p. ej., Teltonika RUT241) + módem LTE con voz (p. ej., SIMCom SIM7600) | Router 4G con failover + llamadas por VoIP sobre la red celular |

Notas:
- **Llamadas de voz con chip argentino:** resuelto. Los comunicadores de alarma ya lo hacen (llamada, mensaje grabado, confirmación con tecla).
- Cámaras por **PoE** desde un switch en la UPS: siguen funcionando sin luz.
- Satélites de voz y radares en enchufes comunes se apagan sin luz. **Modo degradado:** sin luz, la detección se sostiene con el reloj, los sensores de la alarma (a batería) y las cámaras PoE. ⚠ validar si alcanza o si hace falta respaldo para algunos satélites (baño, dormitorio).

### 4.4 Oídos y voz (satélites por ambiente)

| | Candidato A | Candidato B |
|---|---|---|
| **Equipo** | Home Assistant Voice Preview Edition | FutureProofHomes Satellite1 |
| **A favor** | Abierto (ESPHome), integración nativa, local | Mejor arreglo de micrófonos y amplificador para parlante externo (sirve también para música) |
| **En contra** | Parlante chico: no sirve para música; captación a distancia ⚠ validar | Menos difundido; ⚠ validar disponibilidad |

**Qué medir:** captación de voz baja a 3–4 m y con la tele prendida; latencia total de la conversación (VOZ-05, depende sobre todo del software); calidad para música (VOZ-13); funcionamiento en el baño.

### 4.5 Ojos (cámaras)

Cámaras con **RTSP/ONVIF**, alimentadas por **PoE**, con procesamiento en el hub (p. ej., Frigate).

| | Candidato A | Candidato B |
|---|---|---|
| **Equipo** | Hikvision de interior, PoE (línea domo) | Reolink de interior, PoE |
| **A favor** | Muy difundidas en Argentina, ONVIF completo; **mismo ecosistema que la alarma Hikvision** (videoverificación para el monitoreo) | RTSP local simple, buena visión nocturna |
| **En contra** | Configuración más técnica | Fuera del ecosistema de la alarma |

**Qué medir:** detección de persona en el piso de día y de noche; cobertura de un ambiente con 1 cámara; reconocimiento facial de visitas en la entrada.

### 4.6 Sensor sin imagen (baño y dormitorio)

Tiene que detectar **caída e inmovilidad**, no solo presencia. Tecnología: **radar mmWave**. Las alarmas no ofrecen esto.

| | Candidato A | Candidato B |
|---|---|---|
| **Equipo** | Aqara FP2 | Seeed Studio MR60FDA2 (radar 60 GHz de caídas, kit con ESP32 y ESPHome) |
| **A favor** | Producto terminado, zonas configurables, detección de caídas, montaje en techo | Abierto y local por diseño (ESPHome), específico para caídas |
| **En contra** | ⚠ validar que la detección de caídas llegue al hub **sin la nube de Aqara** | Más "kit" que producto: carcasa y montaje a resolver |

**Qué medir:** caídas simuladas en baño y al lado de la cama; acostada en la cama vs. en el piso; falsos positivos durante 2 semanas; vapor de ducha.

### 4.7 Reloj de la persona (wearable)

| | Candidato A | Candidato B |
|---|---|---|
| **Equipo** | Samsung Galaxy Watch (Wear OS) con **app propia** usando el SDK oficial de sensores de Samsung | Bangle.js 2 (firmware abierto) |
| **A favor** | Pulso continuo, **temperatura de piel**, acelerómetro, 5 ATM, se vende oficialmente en Argentina; envía datos por WiFi directo al hub | Totalmente abierto; batería de semanas |
| **En contra** | Batería de 1–2 días (USO-02 pide 5) ⚠ validar acceso al SDK para nuestra app | Sin temperatura de piel; pulso menos preciso; no apto para ducha prolongada; importación |

**Batería del candidato A:** propuesta de **dos relojes en rotación**, uno cargando mientras se usa el otro, con el cambio integrado a una rutina diaria que el asistente recuerda.

**Qué medir:** detección de caídas en el reloj; precisión del pulso; temperatura; envío al hub sin la nube de Samsung; consumo real; uso nocturno.

### 4.8 Sensores del hogar y botones de pánico

**Vienen de la alarma** (4.1): puertas, pérdida de agua, humo, gas y botones de pánico (baño, al lado de la cama), todos de la línea de la central elegida.

**Complemento Zigbee solo si hace falta** algo que la línea de la alarma no tenga o no reporte desarmada (p. ej., temperatura y humedad por ambiente). En ese caso: coordinador SMLIGHT SLZB-06 o Sonoff ZBDongle-E, y sensores Aqara o Sonoff.

Notas:
- "Hornalla prendida sin nadie" sale de **combinar** el detector de gas o temperatura de la cocina con la presencia (cámara o radar). ⚠ validar con la cocina real.
- Botón del baño: ⚠ validar resistencia a salpicaduras.

### 4.9 TV, pantalla y contexto de medios

| | Candidato A | Candidato B |
|---|---|---|
| **Equipo** | Google TV Streamer en la tele que ya tiene la persona | Pantalla propia (tablet de 11–13" en soporte) con nuestra app |
| **A favor** | Usa la tele de siempre; YouTube y apps nativas; se controla desde el hub; el hub sabe qué se está reproduciendo | **Videollamadas con la familia** (APP-09), fotos y mensajes; independiente de la tele |
| **En contra** | No sirve para videollamadas | Una pantalla más en la casa |

Pueden convivir. Propuesta: probar A primero y evaluar B como complemento.

**Contexto de medios (VOZ-12):** sin hardware nuevo. Los satélites escuchan la tele o la radio, el hub transcribe e identifica el contenido, y con Google TV además sabe qué está activo.

**Música (VOZ-13):** por el satélite con parlante adecuado (candidato B de 4.4) o un parlante con integración local.

---

## 5. Plan de pruebas y fechas

| Paso | Qué | Fecha propuesta |
|---|---|---|
| 1 | Compra de 1 unidad de cada candidato (o del recomendado, si se acuerda), incluida la central de alarma con un kit mínimo de sensores (puerta, agua, pánico) | hasta 2026-11-27 |
| 2 | Banco de pruebas en una casa real: alarma + hub + 1 satélite + 1 cámara + 1 radar + reloj | 2026-11-27 a 2026-12-04 |
| 3 | Prueba de 2 semanas con las mediciones de cada sección | 2026-12-04 a 2026-12-18 |
| 4 | **Decisión por función** y registro en este documento | 2026-12-18 |

---

## 6. Decisiones pendientes

1. ¿Compramos **los dos candidatos** de cada función o solo el recomendado y el segundo si el primero falla?
2. ¿En qué **casa real** hacemos el banco de pruebas? ¿Alguna de las casas de los casos ya tiene alarma (y de qué marca)?
3. ¿Estás de acuerdo con la propuesta de **dos relojes en rotación**?
