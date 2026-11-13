# Hardware
**Fecha:** 2026-11-13 (rev. 3)
**Base:** [03_Requisitos.md](03_Requisitos.md), sección 3 (capacidades de percepción)

**Criterio de esta revisión:** empezar por **un equipo por función, el más concreto que va a funcionar bien**. Se compra 1 unidad, se prueba y se decide. El segundo candidato solo aparece si el primero no pasa la prueba.

Los ítems marcados **⚠ validar** son supuestos que la prueba tiene que confirmar.

---

## 1. Criterios

| Criterio | Por qué | Requisito |
|---|---|---|
| Los datos llegan al hub **sin pasar por la nube del fabricante** | El camino de emergencia funciona sin internet | CONF-01 |
| **API oficial o protocolo estándar** (RTSP/ONVIF, ESPHome, Zigbee, SDK oficial, SIA/Contact ID) | Integrar sin ingeniería inversa y poder cambiar de fabricante | ESC-03 |
| **Producto concreto, probado y disponible**, antes que kits o desarrollos a medida | Avanzar con lo que funciona | — |
| Disponible en Argentina / homologación ENACOM | **No excluye para el piloto.** Sí es requisito para el producto | — |

El precio **no** es criterio en esta etapa.

---

## 2. Vista general

```
              ┌──────────────────── Internet ───────────────────┐
              │ (conversación IA, app de la familia, WhatsApp)  │
              └───────────────────────┬─────────────────────────┘
                                      │  + canal celular de respaldo
┌─────────────────────────────────────┴───────────────────────────────┐
│ HUB LOCAL (en UPS)                                                  │
│ detección de caídas · visión · voz local · reglas de emergencia     │
│ coordinador Zigbee · módem celular con voz/SMS                      │
└──┬────────────┬────────────┬─────────────┬────────────┬─────────┬───┘
   │ WiFi       │ PoE        │ WiFi        │ Zigbee     │ WiFi    │ (opcional)
   ▼            ▼            ▼             ▼            ▼         ▼
Satélites    Cámaras     Radares de    Sensores del  Reloj     Alarma
de voz       (ambientes  caída (baño,  hogar +       de la     existente
(todos los   comunes)    dormitorio)   botones SOS   persona   (complemento)
ambientes)               OBLIGATORIOS                + TV
```

**Sensores propios = base.** Radares, cámaras, satélites, reloj y Zigbee son lo que hace funcionar el sistema.
**Alarma = complemento opcional.** Si la casa ya la tiene, se aprovechan sus sensores, su comunicador como segundo camino de aviso y su monitoreo si lo tiene.

---

## 3. Equipo elegido por función

| Función | Equipo | Si no pasa la prueba |
|---|---|---|
| **Hub local** | Mini PC x86 con GPU NVIDIA dedicada (clase RTX 4060/5060, 8–16 GB VRAM) | NVIDIA Jetson Orin |
| **Energía** | Estación de energía LiFePO4 con función UPS (~1 kWh) | UPS online con baterías externas |
| **Canal celular** | Router 4G con failover (Teltonika RUT241) + módem LTE con voz (SIMCom SIM7600) | VoIP sobre la red celular |
| **Oídos y voz** | Home Assistant Voice Preview Edition | FutureProofHomes Satellite1 |
| **Cámaras** | Reolink domo PoE de interior | Hikvision domo PoE |
| **Radar baño/dormitorio** | Aqara FP2 | Seeed MR60FDA2 (radar de caídas con ESPHome) |
| **Zigbee** | Coordinador SMLIGHT SLZB-06 + sensores y botones Aqara | Línea Sonoff |
| **TV** | Google TV Streamer en la tele existente | Pantalla propia (tablet) |
| **Reloj** | Samsung Galaxy Watch5 o posterior + app propia (ver sección 5) | — |

### Por qué cada uno y qué validar

**Hub: Mini PC con GPU NVIDIA.** Es lo que más software corre sin adaptación: Home Assistant, Frigate (visión), Whisper (voz), modelos locales.
- ⚠ Medir consumo real para dimensionar la UPS.

**Energía y canal celular.**
- Llamadas de voz con chip argentino: resuelto (los comunicadores de alarma ya lo hacen).
- Cámaras por PoE desde un switch en la UPS: siguen funcionando sin luz.
- ⚠ Modo degradado: satélites y radares en enchufes comunes se apagan sin luz. Validar si hace falta respaldo para los de baño y dormitorio.

**Oídos y voz: Home Assistant Voice PE.** Producto oficial, abierto y local, el más difundido para esto.
- ⚠ Captación de voz baja a 3–4 m y con la tele prendida.
- ⚠ Su parlante es chico: para música (VOZ-13) puede hacer falta un parlante aparte.
- La latencia de la conversación (VOZ-05) depende sobre todo del software y del servicio de IA.

**Cámaras: Reolink domo PoE.** RTSP local simple, buena visión nocturna, funciona bien con Frigate.
- ⚠ Detección de persona en el piso, de día y de noche.
- ⚠ Reconocimiento facial en la entrada.

**Radar: Aqara FP2.** Producto terminado, con zonas y detección de caídas, montaje en techo. **Obligatorio en baño y dormitorio.**
- ⚠ **Crítico:** que la detección de caídas llegue al hub **sin la nube de Aqara**. Si no, se pasa al Seeed MR60FDA2.
- ⚠ Caídas simuladas, persona en la cama vs. en el piso, vapor de ducha, falsos positivos durante 2 semanas.

**Zigbee: SLZB-06 + Aqara.** Botones SOS (baño, al lado de la cama), puertas, pérdida de agua, temperatura y humedad, detector de gas.
- ⚠ Botón del baño: resistencia a salpicaduras.
- ⚠ "Hornalla sin nadie": combinar gas/temperatura de la cocina con presencia.

**TV: Google TV Streamer.** Usa la tele que la persona ya conoce; YouTube y apps nativas; el hub sabe qué se está reproduciendo (VOZ-12).
- La pantalla propia para videollamadas (APP-09) queda para después.

---

## 4. Alarma existente (complemento)

Si la casa tiene alarma, puede aportar:
- **Sensores adicionales** (puertas, movimiento, humo).
- **Segundo camino de aviso** (CONF-06): su batería y comunicador siguen funcionando si el hub falla.
- **Central de monitoreo**, si la tiene (AVI-10).

**Formas de integrarla**, de la más a la menos deseable:
1. La central reporta eventos en **SIA DC-09 o Contact ID por IP** a nuestro hub, como si fuera una central de monitoreo.
2. **Salidas programables (PGM)** de la central conectadas a entradas del hub: se reciben estados (alarma, pánico, sabotaje).
3. Nuestro hub **dispara una zona** de la alarma con un relé: así una caída confirmada puede salir también por el comunicador y el monitoreo de la alarma.

### Caso de estudio: Tausend CR600 (con WiFi, sin módulo celular ni monitoreo)

Fuentes: manual CR600 v00.01.27 (2026) y "Comandos por IP" de Tausend.

**Vías de integración, de la más a la menos concreta:**

1. **Salidas PGM → hub (sin protocolo).** 2 salidas en placa (8 con expansor), colector abierto a negativo. Funciones útiles (sección [005]): 01 alarma, 02 armada/desarmada, 05 falla, 21 falla de comunicador, 22 **corte de 220V**. Cualquier zona puede dirigirse a una PGM (atributos 6/7, secciones [023]–[030]). Lectura con un ESP32 + ESPHome.
2. **Hub → zona de la central (sin protocolo).** Un relé del hub sobre una zona tipo **10 "Médica 24h"** o **12 "Pánico 24h"**: una caída confirmada dispara también la alarma y su notificación. Es el segundo camino de aviso (CONF-06).
3. **Comandos por IP (documentados por Tausend).** Texto plano por TCP. Relevantes para nosotros:
   - `STSZ` zonas abiertas en este momento (actividad de los sensores aun desarmada), `STSA` estado (READY, ARM, STAY, AWAY…), `STSF` fallas (AC, BAT, COMU…), `STSM` memorias de alarma.
   - `FUNM` evento médico, `FUNP` pánico, `PGMn:1` activar salida.
   - Solo responde a consultas: no envía eventos por su cuenta, hay que consultar periódicamente.
   - **Probado (2026-11-13):** conectada al WiFi de la casa, la central no tiene ningún puerto TCP abierto (escaneo 1–10000). Los comandos por IP **solo funcionan en modo Access Point**, que es temporal y de configuración. **No sirven como integración permanente** salvo que Tausend habilite el acceso local en la red de la casa (consulta pendiente al fabricante).
   - ⚠ Seguridad: `PRG` lee y escribe la configuración (incluido el código maestro) sin pedir clave. Si el puerto queda abierto en la red de la casa, cualquiera en esa red podría usarlo.
4. **Controles remotos Tausend en modo "Médica exclusivo"** (sección [174], opción 5) como botón SOS adicional.

**Descartadas:** reporte a monitoreo por IP (solo formatos Avatec, Bykom/SoftGuard y Tausend IP, sin especificación pública); app Tausend (pasa por su servidor en la nube); comandos por SMS (requieren módulo celular).

---

## 5. Reloj: Samsung Galaxy Watch5 o posterior

**Objetivo de esta etapa:** programar la interfaz y confirmar que recibimos los valores en el hub. Batería, autonomía y años de actualizaciones **no se evalúan ahora**; se revisan cuando el producto esté en marcha.

**Por qué:** el SDK oficial de sensores de Samsung (Samsung Health Sensor SDK) da pulso continuo con intervalos entre latidos, temperatura de piel continua y acelerómetro continuo. Una app propia en el reloj puede enviar los datos por WiFi directo al hub, sin pasar por la nube de Samsung.

**Modelo:** cualquier Galaxy Watch5 o posterior (incluye Pro y Ultra). La Watch4 no sirve: no tiene temperatura de piel continua. Puede ser usado.

**Qué se necesita para desarrollar:**
- Reloj físico (el SDK no funciona en el emulador).
- Celular Android para la configuración inicial del reloj (Galaxy Wearable).
- Modo desarrollador del reloj y de Health Platform activados.
- Registro en Samsung Developers y descarga del SDK. Para publicar en la tienda hace falta un acuerdo de socio con Samsung; para la prueba no.

**Qué validar:**
- ⚠ Recibir en el hub pulso, intervalos entre latidos, temperatura de piel y acelerómetro en forma continua.
- ⚠ Envío por WiFi directo al hub, sin celular de por medio.
- ⚠ Detección de caídas propia a partir del acelerómetro.
- ⚠ Botón o acción de SOS desde el reloj.

---

## 6. Plan de pruebas y fechas

| Paso | Qué | Fecha propuesta |
|---|---|---|
| 1 | Relevamiento de la alarma Tausend → **hecho** (sección 4). Comandos por IP probados: no responden en la red de la casa. Vías confirmadas: PGM y disparo de zona | hasta 2026-11-20 |
| 2 | ~~Análisis de opciones de reloj~~ → **Decidido:** Galaxy Watch5 o posterior | 2026-11-13 |
| 3 | Compra de 1 unidad de cada equipo de la sección 3 | hasta 2026-11-27 |
| 4 | Banco de pruebas en una casa real | 2026-11-27 a 2026-12-04 |
| 5 | Prueba de 2 semanas | 2026-12-04 a 2026-12-18 |
| 6 | Decisión por función y registro en este documento | 2026-12-18 |
