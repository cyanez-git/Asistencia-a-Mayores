# Requisitos
**Fecha:** 2026-11-13
**Base:** [01_Premisas.md](01_Premisas.md) · [02_Escenarios.md](02_Escenarios.md)

Los requisitos dicen **qué** tiene que lograr el sistema, no con qué hardware ni con qué tecnología. Cada uno indica de qué escenarios sale. Los valores numéricos son **iniciales, a validar en pruebas**.

**Criticidad:**
- **C (crítico):** si falla, puede haber un daño a la persona. Tiene que funcionar aunque falle todo lo demás.
- **E (esencial):** es parte de la solución v1.

---

## 1. Requisitos funcionales

### 1.1 Compañía y conversación (VOZ)

| ID | Requisito | Crit. | Escenarios |
|---|---|---|---|
| VOZ-01 | Conversar de forma natural y empática, en español rioplatense, con voz cálida y ritmo adecuado a una persona mayor. | E | C1 |
| VOZ-02 | Conversar sobre los temas que le interesan a la persona (historia, familia, gustos, música, deportes, noticias, recetas), con información actualizada. | E | C1 |
| VOZ-03 | Recordar a largo plazo: personas de su vida, historia, gustos, conversaciones anteriores y lo que le pasó en los últimos días, y usarlo en la conversación. | E | C1, C2 |
| VOZ-04 | Tomar la iniciativa para conversar (saludar a la mañana, preguntar cómo durmió, retomar un tema), con una intensidad configurable por persona. | E | C1, C5 |
| VOZ-05 | Responder sin demoras que rompan la conversación (objetivo: menos de 1,5 s entre que la persona termina de hablar y el asistente empieza a responder). | E | C1 |
| VOZ-06 | Escuchar y hablar en cualquier ambiente con cobertura, siguiendo a la persona por la casa. | E | C1, A1, A2, A6 |
| VOZ-07 | Entender a una persona mayor: voz baja, pausas largas, dicción menos clara, sin exigir palabras exactas. | E | Todos |
| VOZ-08 | Recordatorios de medicación, turnos, cumpleaños y tareas, integrados a la conversación, con confirmación de si se cumplieron. | E | C2 |
| VOZ-09 | Mandar mensajes y hacer llamadas a la familia por pedido de voz; leer o reproducir los mensajes que llegan. | E | C3 |
| VOZ-10 | La familia puede cargar recordatorios, mensajes y temas para que el asistente los integre ("decile que el domingo vamos a comer"). | E | C2, C3 |

### 1.2 Interpretación en segundo plano (INT)

| ID | Requisito | Crit. | Escenarios |
|---|---|---|---|
| INT-01 | Aprender la línea base de la persona: rutina diaria (horarios, uso de ambientes), forma de hablar, ánimo habitual, pulso y temperatura habituales. | E | A6, B1, B5, C4 |
| INT-02 | Detectar cambios bruscos en el habla (arrastrada, confusa, sin encontrar palabras) respecto de su línea base. | C | B3 |
| INT-03 | Detectar dificultad para respirar en la voz (frases cortadas, falta de aire, tos persistente). | E | B2 |
| INT-04 | Detectar cambios de ánimo: tono, temas, expresiones de tristeza, desinterés o desesperanza. Las expresiones de no querer vivir se tratan como **Urgente**. | E | C4 |
| INT-05 | Detectar tendencias de varios días o semanas: menos actividad, cambios de sueño, de comida, de uso del baño, de conversación y de visitas. | E | B5, C5 |
| INT-06 | Reconocer a las personas habituales por cara y voz, y detectar personas no reconocidas. | E | D1, D2 |
| INT-07 | Interpretar conversaciones con terceros (en persona o por teléfono en el ambiente) para detectar riesgo: urgencia, pedidos de dinero, datos o claves, presión o incomodidad de la persona mayor. | E | D2, D3 |
| INT-08 | Intervenir con tacto ante una posible estafa ("Eso suena raro, ¿llamamos a tu hijo antes?"). | E | D3 |
| INT-09 | Detectar riesgos domésticos: hornalla o agua sin control, puerta abierta de noche, temperatura extrema en la casa. | E | E3 |

### 1.3 Seguridad física y salud (SEG)

| ID | Requisito | Crit. | Escenarios |
|---|---|---|---|
| SEG-01 | Detectar caídas en **todos** los ambientes, incluidos baño y dormitorio, sin cámara en los ambientes privados. | C | A1–A4 |
| SEG-02 | Aplicar la **regla de validación de caídas**: una señal sola abre una verificación; la emergencia requiere una segunda fuente (otro sensor, la voz de la persona o la falta de respuesta sin señal que contradiga). | C | A1–A4 |
| SEG-03 | Detectar caídas aunque la persona no tenga puesto el wearable. Ningún dispositivo solo es imprescindible para detectar una caída. | C | A3 |
| SEG-04 | Detectar inmovilidad y falta de respuesta luego de una caída, y escalar sin esperar (objetivo: aviso a la familia en menos de 2 minutos desde la caída, si no hay respuesta). | C | A4 |
| SEG-05 | Atender un pedido de ayuda por voz ("ayuda", "me caí", "llamá a…") o por botón como **Emergencia** directa, sin pedir confirmación. | C | A5 |
| SEG-06 | Detectar inactividad anormal según la rutina aprendida y el horario (no se levantó, mucho tiempo en el baño, sin movimiento en la casa). | C | A6 |
| SEG-07 | Medir pulso de forma continua y detectar valores altos, bajos o irregulares respecto de **umbrales personalizados**, considerando la actividad del momento. | E | B1 |
| SEG-08 | Medir temperatura corporal (no de grado médico) y detectar fiebre. | E | B4 |
| SEG-09 | Cruzar señales antes de avisar (pulso + actividad + habla + imagen) para subir o bajar el nivel de aviso. | E | B1–B4 |
| SEG-10 | Detectar ingreso de personas a la casa en horarios o situaciones no esperadas. | E | D4 |
| SEG-11 | Acompañar por voz a la persona durante una emergencia: calmarla, darle indicaciones simples, informarle quién viene. | E | A1, A4 |

### 1.4 Avisos y familia (AVI)

| ID | Requisito | Crit. | Escenarios |
|---|---|---|---|
| AVI-01 | Clasificar todo aviso en cuatro niveles: Resumen, Atención, Urgente, Emergencia. | C | Todos |
| AVI-02 | Canal principal con la familia: WhatsApp (sin instalar otra app para recibir avisos). | E | Todos |
| AVI-03 | **Emergencia:** llamar a la cadena de contactos en orden hasta que **una persona confirme** que la tomó. Si nadie confirma, seguir escalando. | C | A1–A6, B3 |
| AVI-04 | **Urgente:** mensaje inmediato + llamada si no se lee en un tiempo configurable. | E | B1, B2, D2, D3 |
| AVI-05 | El mensaje de emergencia se arma con una **plantilla fija** con los datos (qué, dónde, cuándo, últimas señales), sin depender de un modelo de IA para redactarlo. | C | A1–A6 |
| AVI-06 | Resumen diario configurable: cómo estuvo, de qué habló, cómo se la notó, visitas, cumplimiento de recordatorios. | E | C1, D1 |
| AVI-07 | Acceso **opcional** de la familia a transcripciones, audio, imagen o video: por configuración, o habilitado automáticamente cuando la situación lo requiere (p. ej., estafa, desconocido, caída). | E | D2, D3, A1 |
| AVI-08 | Llamada directa a servicios de emergencia (107 / 911): **opcional**, configurable como último eslabón de la cadena o en paralelo. | E | A1–A6 |
| AVI-09 | Configurar contactos, orden de la cadena, horarios, umbrales y nivel de detalle desde un lugar simple para la familia. | E | Todos |

### 1.5 Funcionamiento del sistema (SIS)

| ID | Requisito | Crit. | Escenarios |
|---|---|---|---|
| SIS-01 | Avisar a la familia si un dispositivo deja de funcionar o se queda sin batería, indicando qué área queda sin cobertura. Decírselo a la persona si puede resolverlo. | E | E2 |
| SIS-02 | Avisar a la familia de cortes de luz o internet en la casa. | E | E1 |
| SIS-03 | Registrar cada verificación, alerta, alerta descartada y falso positivo, para medir la tasa y ajustar umbrales. | E | A (regla) |
| SIS-04 | Registrar el estado del sistema y de cada dispositivo para diagnóstico remoto. | E | E2 |

---

## 2. Requisitos no funcionales

### 2.1 Confiabilidad del camino de emergencia (CONF)

| ID | Requisito | Crit. |
|---|---|---|
| CONF-01 | La detección de caídas, inactividad, pedido de ayuda y el envío de la emergencia funcionan **en la casa, sin internet y sin servicios de IA en la nube**. | C |
| CONF-02 | El sistema sigue funcionando sin luz (objetivo: al menos 8 horas de autonomía para el camino de emergencia). | C |
| CONF-03 | Hay un **canal de aviso alternativo** independiente de la conexión a internet de la casa (p. ej., red celular). | C |
| CONF-04 | Una falla en la parte de conversación (IA, nube) no afecta la detección ni los avisos de emergencia. | C |
| CONF-05 | El sistema se autoverifica en forma periódica (dispositivos, canales de aviso) y avisa si algo no funciona. | C |

### 2.2 Calidad de detección (DET)

| ID | Requisito |
|---|---|
| DET-01 | Falsos positivos de emergencia por caída: objetivo **menos de 1 por mes** por vivienda, una vez calibrado. |
| DET-02 | Caídas reales no detectadas: objetivo **cero** en las pruebas planificadas. |
| DET-03 | Umbrales ajustables por persona y por ambiente. |

### 2.3 Cobertura y escalabilidad (ESC)

| ID | Requisito |
|---|---|
| ESC-01 | La misma arquitectura sirve para cualquier vivienda; se escala agregando dispositivos por ambiente. |
| ESC-02 | Cada ambiente define qué percepción tiene: voz, imagen (ambientes comunes) o sensor sin imagen (baño, dormitorio). |
| ESC-03 | Agregar o quitar un dispositivo no requiere reconfigurar todo el sistema. |

### 2.4 Uso por la persona mayor (USO)

| ID | Requisito |
|---|---|
| USO-01 | La persona no tiene que aprender a usar nada: le habla al asistente y listo. |
| USO-02 | El wearable es cómodo para usarlo todo el día y la noche, resistente al agua (ducha) y con batería de varios días (objetivo: 5 días o más). |
| USO-03 | Los dispositivos de los ambientes no requieren interacción de la persona (no hay que cargarlos ni tocarlos). |

### 2.5 Instalación y mantenimiento (INS)

| ID | Requisito |
|---|---|
| INS-01 | La instalación es simple y guiada. Objetivo inicial: la familia la hace sin técnico; si resulta necesario un técnico para la v1, se acepta y se registra como aprendizaje. |
| INS-02 | Las actualizaciones se hacen en forma remota sin intervención de la persona. |
| INS-03 | El soporte puede diagnosticar problemas en forma remota. |

### 2.6 Datos y privacidad (DAT)

| ID | Requisito |
|---|---|
| DAT-01 | Audio e imagen se procesan para detectar y se descartan, salvo lo vinculado a un aviso o lo que la familia configuró conservar. |
| DAT-02 | Los datos se guardan cifrados y solo accede quien la familia autorizó. |
| DAT-03 | Todo acceso de la familia a transcripciones, audio o video queda registrado. |
| DAT-04 | Se tiene presente la Ley 25.326; las adecuaciones se definen después de las primeras pruebas, sin recortar funciones. |

---

## 3. Capacidades de percepción que se derivan

Esto es lo que tiene que **percibir** el sistema. Es la entrada para el paso de hardware (máximo 2 candidatos por función, con fecha para decidir).

| Capacidad | Dónde | Para qué | Requisitos |
|---|---|---|---|
| **Oídos y voz** (micrófono + parlante) | Cada ambiente con cobertura, incluido baño y dormitorio | Conversación, pedido de ayuda, verificación de caídas, análisis del habla, conversaciones con terceros | VOZ-01..07, INT-02..08, SEG-02, SEG-05 |
| **Ojos** (cámara) | Ambientes comunes y entrada | Caídas, postura, reconocimiento de personas, visitas, ingresos | SEG-01, SEG-10, INT-06 |
| **Sensor sin imagen** | Baño, dormitorio | Presencia, caída, inmovilidad, tiempo en el ambiente | SEG-01, SEG-06 |
| **Wearable** | En la persona | Pulso continuo, impacto/caída, temperatura, botón de ayuda | SEG-03, SEG-05, SEG-07, SEG-08 |
| **Sensores del hogar** | Cocina, puertas, baño | Hornalla, agua, puerta abierta, temperatura ambiente | INT-09 |
| **Hub local** con respaldo de energía y canal celular | Un lugar fijo | Procesamiento local, camino de emergencia, avisos | CONF-01..05 |

---

## 4. Decisiones pendientes antes de hardware

1. **Wearable:** pulsera, reloj, colgante o combinación. Es el dispositivo con más exigencias (pulso continuo, caídas, temperatura, agua, batería, comodidad).
2. **Sensor sin imagen** para baño y dormitorio: que detecte caída e inmovilidad, no solo presencia.
3. **Qué corre en la casa y qué en la nube:** la conversación probablemente use IA en la nube; la detección y la emergencia, en la casa (CONF-01).
4. **Acceso de emergencia a la casa:** cómo entra quien acude si la persona no puede abrir.
5. **Tiempo de aprendizaje** de la línea base antes de activar la detección de cambios.
