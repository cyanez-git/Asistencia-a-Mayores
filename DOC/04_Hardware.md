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
| **Reloj** | **En análisis** (ver sección 5) | — |

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

**Caso de estudio: alarma Tausend (sin monitoreo) de tu casa.** Para entender qué nos puede brindar, necesitamos:
- Modelo de la central (está en la placa o en el manual).
- Qué comunicador tiene: línea telefónica, GSM/4G, WiFi o IP.
- Si tiene salidas PGM.
- Si se puede configurar para reportar a un receptor propio (Contact ID o SIA, por IP o línea).
- Qué app usa y qué muestra.
- Qué sensores tiene instalados.

---

## 5. Reloj: en análisis

Es el dispositivo más exigente: pulso continuo, caídas, temperatura, botón de ayuda, ducha, comodidad, batería y datos directo al hub. Queda **fuera de la primera compra** hasta analizar mejor las opciones.

Opciones vistas hasta ahora:
- **Samsung Galaxy Watch con app propia:** sensores completos (incluida temperatura de piel), pero batería de 1–2 días. Se planteó usar **dos relojes en rotación**; queda como alternativa a evaluar.
- **Bangle.js 2:** abierto y con semanas de batería, pero sensores más limitados.

**Próximo paso:** un análisis más amplio de opciones (relojes, pulseras, colgantes, anillos) contra los requisitos SEG-03, SEG-05, SEG-07, SEG-08 y USO-02.

---

## 6. Plan de pruebas y fechas

| Paso | Qué | Fecha propuesta |
|---|---|---|
| 1 | Relevamiento de la alarma Tausend | hasta 2026-11-20 |
| 2 | Análisis de opciones de reloj y decisión de qué probar | hasta 2026-11-20 |
| 3 | Compra de 1 unidad de cada equipo de la sección 3 | hasta 2026-11-27 |
| 4 | Banco de pruebas en una casa real | 2026-11-27 a 2026-12-04 |
| 5 | Prueba de 2 semanas | 2026-12-04 a 2026-12-18 |
| 6 | Decisión por función y registro en este documento | 2026-12-18 |
