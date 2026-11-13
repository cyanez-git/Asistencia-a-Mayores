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
Satélites    Cámaras     Radares de    Sensores del  Reloj     Puente a
de voz       (ambientes  caída (baño,  hogar +       de la     alarma
(todos los   comunes)    dormitorio)   botones SOS + persona   existente
ambientes)               OBLIGATORIOS  sirenas       + TV      (opcional)
```

**El sistema es también la alarma.** Con sus sensores, sirenas, batería y comunicador, cumple la función de alarma (modos ausente / presente-noche) sin depender de terceros.
**Alarma existente = opcional.** Si la casa ya tiene una, se suma con un **puente genérico** que funciona con cualquier marca (sección 4).

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
| **Sirenas interior y exterior** | Sirenas cableadas 12 V de alarma convencionales, de cualquier marca disponible en Argentina (p. ej., Hikvision cableada), comandadas por relé desde la placa de relés | — |
| **Placa de relés** (sirenas y puente opcional) | Placa genérica ESP32 con 8 relés + 8 entradas optoacopladas, alimentación 9–24 V, gabinete riel DIN, programada con ESPHome | ESP32 + módulo de relés + módulo de entradas optoacopladas por separado |
| **TV** | Google TV Streamer en la tele existente | Pantalla propia (tablet) |
| **Reloj** | Samsung Galaxy Watch5 o posterior + app propia (ver sección 5) | — |

### Por qué cada uno y qué validar

**Hub, etapa 1: mini PC Bangho Cubic A4130** (Intel Core i3-4010U, 8 GB RAM, SSD 120 GB), disponible y dedicada al proyecto. Corre Home Assistant OS, ESPHome, Zigbee, recepción de datos del reloj y reglas de emergencia. **No** corre IA local (transcripción de voz, análisis de video): durante el desarrollo se usan servicios en la nube y las cámaras quedan para la etapa 2.

**Hub, etapa 2: Mini PC con GPU NVIDIA.** Es lo que más software corre sin adaptación: Home Assistant, Frigate (visión), Whisper (voz), modelos locales.
- ⚠ Medir consumo real para dimensionar la UPS.

**Energía y canal celular.**
- Llamadas de voz con chip argentino: resuelto (los comunicadores de alarma ya lo hacen).
- Cámaras por PoE desde un switch en la UPS: siguen funcionando sin luz.
- ⚠ Modo degradado: satélites y radares en enchufes comunes se apagan sin luz. Validar si hace falta respaldo para los de baño y dormitorio.

**Oídos y voz: Home Assistant Voice PE.** Producto oficial, abierto y local, el más difundido para esto.
- ⚠ Captación de voz baja a 3–4 m y con la tele prendida.
- ⚠ Su parlante es chico: para música (VOZ-13) puede hacer falta un parlante aparte.
- La latencia de la conversación (VOZ-05) depende sobre todo del software y del servicio de IA.

**Cámara, etapa 1: Seisa IP 1080p (disponible).** Firmware Xiongmai (chip XM530), probada en la red de la casa: responde por **RTSP** (puerto 554) y **ONVIF 2.41** (puerto 8899). Sirve para integrar la cámara al sistema (vista en vivo, imagen en los avisos). ⚠ Este tipo de firmware suele conectarse a servidores externos (puerto 34567 abierto, nube XMEye): bloquearle la salida a internet en el router. ⚠ Cambiar la clave de fábrica.

**Cámaras, etapa 2: Reolink domo PoE.** RTSP local simple, buena visión nocturna, funciona bien con Frigate.
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

## 4. Puente genérico a una alarma existente (opcional)

Para la casa que ya tiene alarma y quiere conservarla (ALA-06). Funciona con **cualquier marca** porque usa una conexión **eléctrica** con dos cosas que tienen casi todas las centrales, no el protocolo del fabricante:

1. **Salidas PGM de la central → entradas del puente.** Se programan en la central para informar: disparo de alarma, armada/desarmada, falla y **corte de luz**. Cualquier zona de la central (p. ej., un botón de pánico) puede dirigirse a una PGM.
2. **Relé del puente → una zona 24 h de la central** (tipo "médica" o "pánico"). Una caída confirmada por nuestro sistema dispara también la alarma existente y sale por su comunicador y su monitoreo. Es el **segundo camino de aviso** (CONF-06).

**Instalación:** la hace el técnico de alarmas de la casa, en las borneras de la central. Se programa qué evento sale por cada PGM y qué zona queda como médica/pánico.

**Equipo:** la misma placa de relés que comanda las sirenas (sección 3), con ESPHome.

**Especificación genérica de la placa** (cualquier marca que cumpla):
- Microcontrolador **ESP32** (no ESP8266 / ESP-01).
- **3 o más relés:** sirena interior, sirena exterior, disparo de zona médica.
- **3 o más entradas digitales optoacopladas** de 12 V: lectura de PGM (deben aceptar salidas que conectan a negativo).
- Alimentación **12 V** (o rango 9–24 V), para tomarla de la fuente con batería.
- Ethernet con PoE deseable; WiFi alcanza para la prueba.
- Montaje en gabinete o riel DIN.

⚠ Validar: el mapa de pines (qué GPIO maneja cada relé y cada entrada), si no viene documentado, y que las entradas lean las PGM de la alarma de prueba.

**Sirenas:** cableadas de 12 V, de las que se usan en cualquier alarma, porque son las que se consiguen en todo el país y no dependen de un ecosistema. Las sirenas **inalámbricas** de las marcas de alarma (p. ej., Hikvision AX PRO) usan radio propietaria y solo funcionan con su propia central: no sirven.

### Banco de pruebas: Tausend CR600

Se usa la alarma Tausend CR600 de la casa de prueba (con WiFi, sin módulo celular ni monitoreo) para validar el puente. Según su manual (v00.01.27):
- PGM útiles (sección [005]): 01 alarma, 02 armada/desarmada, 05 falla, 22 corte de 220 V. Zonas dirigibles a PGM con los atributos 6/7 (secciones [023]–[030]). Salidas de colector abierto a negativo.
- Zonas 24 h para el disparo: tipo 10 "Médica 24h" o tipo 12 "Pánico 24h".

**Integración por protocolo, probada y descartada (2026-11-13):** los "Comandos por IP" de Tausend solo responden en modo Access Point (configuración temporal); conectada al WiFi de la casa no expone ningún puerto. El reporte por IP usa formatos sin especificación pública (Avatec, Bykom/SoftGuard, Tausend IP). Esto confirma que el puente eléctrico es el camino genérico.

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
| 1 | Relevamiento de la alarma Tausend → **hecho** (sección 4). Queda como banco de pruebas del puente genérico | hasta 2026-11-20 |
| 2 | ~~Análisis de opciones de reloj~~ → **Decidido:** Galaxy Watch5 o posterior | 2026-11-13 |
| 3 | Compra de 1 unidad de cada equipo de la sección 3 | hasta 2026-11-27 |
| 4 | Banco de pruebas en una casa real | 2026-11-27 a 2026-12-04 |
| 5 | Prueba de 2 semanas | 2026-12-04 a 2026-12-18 |
| 6 | Decisión por función y registro en este documento | 2026-12-18 |
