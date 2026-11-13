# Aprendizajes del proyecto anterior (SAMP v1)
**Fecha:** 2026-11-13

El proyecto anterior (repo `cyanez-git/IA-Mayores`) queda **archivado como consulta**. De ahí no se trae código ni decisiones de hardware: solo lo que aprendimos, resumido acá.

---

## 1. Lo que el mercado nos enseñó

- **La compañía por voz ya está cubierta y se está abaratando.**
  - **Ato** (Argentina): parlante con IA, USD 179 + USD 29/mes. Conversa, manda WhatsApp por voz y lee recordatorios que carga la familia. No detecta caídas.
  - **Serenia** (España): solo software (app, WhatsApp, Alexa), de €0 a €5,80/mes. Sin emergencias.
  - **ElliQ** (EE. UU.): robot de mesa, USD 250 + USD 50–60/mes.
- **El hueco es la seguridad pasiva en casa:** detectar caídas, inactividad y cambios de rutina **sin que la persona tenga que hacer nada**, y avisar a la familia.
- **El precio que acepta el mercado** ronda los USD 180 de equipo + USD 30/mes (referencia: Ato).
- **Riesgo que preocupa al público:** privacidad de los datos de salud. Procesar en la casa y no usar cámara es un argumento de venta.

## 2. Criterios para elegir hardware

1. **Protocolo estándar o API oficial documentada.** Nada de ingeniería inversa.
2. **Más de un fabricante posible** para la misma función.
3. **Los datos llegan a nuestro sistema** sin depender de la nube del fabricante.
4. **Lo instala la familia** sin técnico, en menos de 1 hora.
5. **La seguridad no depende de un solo dispositivo.**
6. **Se consigue en Argentina** con homologación ENACOM.
7. **Comprar 1 unidad y probarla 2 semanas** antes de comprar más.

## 3. Errores a no repetir

| Error | Qué pasó |
|---|---|
| **Elegir hardware antes que el objetivo** | Se compró hardware y después se buscó para qué servía. |
| **Ingeniería inversa** | La Xiaomi Smart Band 9 requería sacar una clave a mano por unidad. Amazfit tiene el mismo problema. |
| **Comprar antes de validar** | Pulsera y sensores terminaron en costo muerto. |
| **Cambiar de arquitectura sin cerrar la anterior** | Raspberry Pi → tablet → Mini PC, con documentos de las tres versiones mezclados. |
| **Demasiadas opciones abiertas a la vez** | Muchas vueltas sin decidir. Máximo 2 candidatos por función y fecha para decidir. |
| **Prometer cobertura fuera de casa** | Sin sensores ni hub afuera, no se puede cumplir. |
| **Emergencia dependiente del LLM o de internet** | La alerta la redactaba el modelo en la nube; si fallaba, no había aviso. |
| **La alerta no llegaba a nadie** | Se generaba el mensaje para la familia pero solo se imprimía en consola. |

## 4. Ideas que valen la pena

- **Doble verificación de caídas:** ningún sensor solo dispara una alerta. Otra fuente confirma y el asistente pregunta "¿Estás bien?". El silencio nunca cancela una alerta.
- **SOS manual = aviso directo**, sin preguntar.
- **El aviso de emergencia es determinístico:** sale de una plantilla con los datos, no del LLM, y tiene respaldo si se corta internet o la luz.
- **WhatsApp como canal con la familia** (idea tomada de Ato): la familia no necesita instalar otra app.
- **Detección de inactividad:** para alguien que vive solo es la señal más útil ("hace X horas que no hay movimiento").
- **Umbrales personalizados** por persona (p. ej., pulso bajo normal con ciertos medicamentos).

## 5. Hardware evaluado (referencia, nada decidido)

| Función | Opciones vistas | Notas |
|---|---|---|
| Reloj/pulsera | Wear OS con app propia, Bangle.js 2, iSmarch X5/X6, relojes 4G para mayores | LTE descartado: no funciona en Argentina y la batería no aguanta. iSmarch ESP32-C6 a medida: USD 10.000 de documentación + pedido mínimo, firmware propio. |
| Caídas sin cámara | Radar de techo (Aqara FP2) | Hay que validar en baño y sin nube. |
| Presencia | Sensores mmWave Zigbee o WiFi | Los ZY-M100 no detectan caídas. |
| Voz | Satélite tipo Home Assistant Voice PE | Uno por ambiente. |
| Botón fijo | Botón SOS Zigbee | Baño y junto a la cama. |
| Respaldo | Mini UPS + módem 4G en el hub | Para que la alerta salga sin luz o sin internet. |

## 6. Orden de trabajo para este proyecto

1. **Objetivo y usuario:** qué problema resolvemos, para quién, y qué no hacemos.
2. **Escenarios concretos** y qué tiene que pasar en cada uno.
3. **Requisitos** que salen de los escenarios.
4. **Hardware:** máximo 2 candidatos por función y fecha para decidir.
5. **Código.**
