# Escenarios
**Fecha:** 2026-11-13
**Base:** [01_Premisas.md](01_Premisas.md)

Cada escenario describe una situación concreta: qué pasa, qué percibe el sistema, qué hace y a quién avisa. De acá salen los requisitos. Los tiempos y umbrales son **iniciales, a calibrar en pruebas**.

---

## Niveles de aviso

| Nivel | Cuándo | Cómo llega a la familia |
|---|---|---|
| **Resumen** | Información del día, nada preocupante | Mensaje diario (WhatsApp) |
| **Atención** | Algo cambió y conviene mirarlo (ánimo, rutina, salud leve) | Mensaje en el momento, sin urgencia |
| **Urgente** | Riesgo posible que requiere que alguien actúe hoy o ya (estafa, desconocido, síntoma) | Mensaje + llamada si no se lee |
| **Emergencia** | Riesgo para la vida o la integridad (caída sin respuesta, ACV, SOS) | Llamada a la cadena de contactos hasta que alguien confirme; escalamiento a vecino o servicio de emergencia |

**Canales:** app móvil de la familia (principal), WhatsApp (resumen y avisos con enlace a la app), llamada telefónica (emergencias, siempre).

**Cadena de contactos:** contacto principal → secundario → vecino/persona cercana. Configurable por familia. En la app, quien toma la emergencia ("La tomo yo") detiene la cadena y el resto ve quién está atendiendo.

**Central de monitoreo: opcional.** Si la casa ya tiene servicio de monitoreo, recibe también los eventos de cuidado y lo potenciamos. Si no lo tiene, todo funciona igual. Una emergencia no se da por atendida hasta que **una persona confirma** que la tomó.

**Llamada directa a servicios de emergencia (107 / 911): opcional.** Lo recomendable es que la emergencia la maneje una persona a cargo, que conoce la situación y decide. La llamada directa se habilita para quien está realmente solo o no tiene a nadie que responda rápido, y entra en la cadena después de los contactos que no confirmaron (o en paralelo, si así se configura).

---

## A. Caídas e incidentes físicos

### Regla de validación de caídas

Los falsos positivos son el mayor riesgo para que el sistema se abandone: si la familia recibe alarmas falsas, deja de creerle o lo desconecta. Pero una caída real **siempre** tiene que atenderse. Por eso:

1. **Una sola señal nunca dispara la emergencia.** Una señal sola (impacto en el wearable, persona en el piso en la cámara, sensor del baño, un golpe en el micrófono) **abre una verificación**, no una alerta.
2. **La verificación busca una segunda fuente independiente:**
   - **Otro sensor que confirma** (p. ej., impacto en el wearable + persona en el piso en la cámara o en el sensor del ambiente).
   - **La voz de la persona:** el asistente pregunta "¿Estás bien?, ¿te podés levantar?".
3. **Resultados posibles:**

| Lo que pasa en la verificación | Resultado |
|---|---|
| Dos fuentes confirman la caída | **Emergencia** |
| La persona dice que necesita ayuda, o no puede levantarse | **Emergencia** |
| No responde y ninguna otra fuente contradice la caída | **Emergencia** (el silencio nunca cancela) |
| Otra fuente contradice (p. ej., el reloj registró un golpe pero la cámara la ve caminando en otro ambiente) | Se descarta; queda registrado |
| Dice "estoy bien" y se la ve o se la siente levantarse | Se descarta; queda en el **Resumen** |
| Dice "estoy bien" pero sigue en el piso | Se la acompaña y se vuelve a verificar en pocos minutos; si sigue en el piso, **Emergencia** |

4. **Cada alerta descartada o falsa se registra** para medir la tasa de falsos positivos y ajustar umbrales por persona y por ambiente.

### A1. Caída en un ambiente con cámara, la persona responde
*Caso real 1, variante leve.*
- **Situación:** se cae en el living. Está consciente, golpeada, no se puede levantar sola.
- **Percibe:** cámara (persona en el piso), wearable (impacto), micrófono (golpe, quejido).
- **Hace:** le habla de inmediato con calma: "¿Estás bien? ¿Te podés levantar?". Si dice que no puede o que le duele, no espera más.
- **Avisa:** **Emergencia** a la cadena de contactos, con qué pasó, dónde y desde cuándo. Imagen del momento disponible para la familia.
- **Mientras tanto:** la acompaña por voz, le da indicaciones simples (no moverse si le duele, abrigarse si puede alcanzar algo) y le informa quién viene en camino.

### A2. Caída en el baño o el dormitorio (sin cámara)
- **Situación:** se cae en el baño de noche.
- **Percibe:** sensor del ambiente privado (presencia en el piso / movimiento anormal / sin movimiento), wearable (impacto), micrófono si hay en el ambiente o el más cercano.
- **Hace:** aplica la regla de validación: pregunta por voz y cruza con los otros sensores.
- **Avisa:** **Emergencia** si se confirma, si no responde o si responde que necesita ayuda.

### A3. Caída sin wearable puesto
- **Situación:** se lo sacó para bañarse o dormir.
- **Percibe:** solo cámara o sensor del ambiente, y micrófono.
- **Hace:** igual que A1/A2. El sistema no puede depender de un único dispositivo.
- **Además:** si nota que el wearable no se usa hace tiempo, se lo recuerda con naturalidad en la conversación.

### A4. Desvanecimiento: la persona no responde
*Caso real 1, el más grave.*
- **Situación:** se desmaya y queda inmóvil.
- **Percibe:** caída + inmovilidad + sin respuesta a la voz. Wearable: pulso anormal si lo tiene puesto.
- **Hace:** insiste por voz (más fuerte, por su nombre) durante un tiempo corto.
- **Avisa:** **Emergencia** inmediata, sin esperar a que despierte. Si recupera la conciencia, la acompaña y vuelve a evaluar.
- **Objetivo:** que **nunca** pase más de unos minutos entre la caída y que alguien lo sepa.

### A5. La persona pide ayuda
- **Situación:** dice "ayuda", "me caí", "llamá a mi hija", o aprieta un botón SOS.
- **Hace:** no pregunta si está seguro.
- **Avisa:** **Emergencia** directa.

### A6. Inactividad sin explicación
- **Situación:** no hay movimiento en ningún ambiente por un tiempo inusual para esa persona y ese horario (p. ej., no se levantó a la hora habitual, o lleva mucho tiempo en el baño).
- **Percibe:** todos los sensores sin actividad; comparación con su rutina aprendida.
- **Hace:** la llama por voz en el ambiente donde se la vio por última vez.
- **Avisa:** si responde, nada (o **Atención** si se nota mal). Si no responde, **Emergencia**.

---

## B. Salud (no médica)

### B1. Pulso fuera de lo normal
- **Situación:** el pulso está muy alto en reposo, muy bajo, o irregular.
- **Percibe:** wearable (pulso continuo), comparado con **los valores habituales de esa persona** y con lo que está haciendo (no es lo mismo subir una escalera que estar sentada mirando tele). Los umbrales son personalizados: p. ej., hay medicamentos que bajan el pulso de forma normal.
- **Hace:** cruza con otras señales: ¿está activa o quieta?, ¿cómo habla?, ¿se la ve bien? Le pregunta cómo se siente, sin alarmarla.
- **Avisa:**
  - **Atención:** valor fuera de rango que se normaliza, o tendencia de varios días (p. ej., pulso en reposo más alto que lo habitual toda la semana).
  - **Urgente:** valor fuera de rango sostenido, o acompañado de malestar ("me siento mareada", "me late fuerte el corazón").
  - **Emergencia:** valor extremo junto con otra señal grave (desvanecimiento, caída, no responde, falta de aire).
- **Nota:** se informa como "algo está pasando", nunca como diagnóstico.

### B2. Dificultad para respirar
- **Percibe:** en la conversación, frases cortadas, falta de aire, tos persistente; wearable: pulso alto en reposo.
- **Hace:** le pregunta cómo se siente, sin alarmarla.
- **Avisa:** **Urgente** a la familia. **Emergencia** si empeora o la persona lo pide.

### B3. Habla arrastrada o confusa de golpe
- **Percibe:** cambio brusco en cómo habla respecto a su forma habitual (palabras mal articuladas, frases sin sentido, no encuentra palabras). Cámara: posible asimetría al moverse.
- **Hace:** le hace preguntas simples ("¿Qué día es hoy?", "Levantá los dos brazos"), de forma natural.
- **Avisa:** **Emergencia**. Un posible ACV no espera: el tiempo importa.

### B4. Fiebre o malestar
- **Percibe:** wearable (temperatura alta, pulso), conversación ("me siento mal", "no tengo hambre"), menos actividad.
- **Avisa:** **Atención**; **Urgente** si persiste o se suma otro síntoma.

### B5. Cambios en la rutina de salud
- **Percibe:** come menos, duerme mucho más o mucho menos, va al baño con frecuencia anormal, se mueve menos durante varios días.
- **Avisa:** **Atención**, con el patrón observado ("hace 4 días que se levanta 2 horas más tarde").

---

## C. Ánimo y compañía

### C1. Compañía diaria
*Caso real 2.*
- **Situación:** la persona quiere conversar.
- **Hace:** conversa de lo que le interesa: su historia, su familia, sus gustos, música, fútbol, noticias, recetas. Recuerda conversaciones anteriores ("¿Cómo te fue con el médico ayer?"). Toma la iniciativa si la persona lleva mucho tiempo sin hablar con nadie.
- **Avisa:** **Resumen** diario a la familia (cómo estuvo, de qué habló, cómo se la notó), con el nivel de detalle que la familia elija.
- **Objetivo:** que la persona *quiera* hablar con el asistente, y que la familia esté tranquila sin tener que llamar para controlar.

### C2. Recordatorios y agenda
- **Hace:** recuerda medicación, turnos médicos, cumpleaños, llamar a alguien. Lo integra a la conversación, no como alarma fría. Puede confirmar si la persona lo hizo.
- **Avisa:** **Atención** si se salteó algo importante (p. ej., no tomó la medicación).
- **Familia:** puede cargar recordatorios y mensajes ("Decile a mamá que el domingo vamos a comer").

### C3. Conectar con la familia
- **Hace:** la persona le pide "mandale un mensaje a Juan" o "llamá a mi hija", y el asistente lo hace. También le lee o reproduce los mensajes que le llegan.

### C3b. Conversar sobre lo que pasó
- **Situación:** la persona tuvo un día con hechos para comentar: habló con su hija, vino una visita, fue al médico, pasó algo en el barrio.
- **Hace:** usa eso como inicio de conversación y reflexiona con ella ("¿Cómo te quedaste después de hablar con Laura? Parecía preocupada por el trabajo"). Retoma temas pendientes de días anteriores.
- **Objetivo:** que la conversación sea personal y continua, como con alguien que la conoce y estuvo ahí.

### C3c. Comentar lo que está viendo o escuchando
- **Situación:** está mirando el noticiero, un programa de chimentos, una película, un partido, o escuchando la radio.
- **Percibe:** qué contenido se está reproduciendo y de qué se habla.
- **Hace:** comenta el tema con naturalidad, en el momento oportuno ("¿Viste lo que dijeron del aumento de las jubilaciones?", "Esa actriz es la que te gustaba en la novela de los 80"). No interrumpe en momentos inoportunos y respeta si la persona quiere ver tranquila.
- **Además:** lo que vio o escuchó sirve después como tema de conversación.

### C3d. Pedir música o contenido
- **Situación:** pide "poneme un tango de Gardel", "quiero escuchar la radio", "mostrame en YouTube el video de Sandro", "poné la misa".
- **Hace:** reproduce la música o la radio en el parlante, y el video en la tele o en una pantalla. Controla volumen, pausa, siguiente, por voz.
- **Además:** puede proponer música según el momento y el ánimo, y aprende sus gustos.

### C4. Cambio brusco de ánimo
- **Percibe:** tono de voz, temas, frases ("no tengo ganas de nada", "para qué seguir"), menos interés en conversar, comparado con su forma habitual.
- **Hace:** la acompaña con empatía, sin interrogarla.
- **Avisa:** **Atención** por cambios sostenidos. **Urgente** si hay expresiones de desesperanza o de no querer vivir.

### C5. Aislamiento progresivo
- **Percibe:** semanas con menos conversación, menos visitas, menos salidas.
- **Avisa:** **Atención** con la tendencia, para que la familia sepa que hace falta más presencia.

---

## D. Seguridad

### D1. Visita habitual
- **Percibe:** reconoce a la persona por cara y/o voz (hija, vecina, persona de limpieza).
- **Hace:** nada especial. Puede saludar si corresponde.
- **Avisa:** queda en el **Resumen** ("Vino Marta a las 16").

### D2. Desconocido en la casa
- **Percibe:** una persona no reconocida.
- **Hace:** presta atención a la conversación: ¿es un técnico esperado, un vendedor, alguien que pide entrar o pide plata?
- **Avisa:** **Atención** si es una visita normal; **Urgente** si hay señales de riesgo (presión, pedido de dinero o datos, la persona mayor incómoda), con imagen y el fragmento de conversación relevante.

### D3. Estafa telefónica (cuento del tío y similares)
- **Situación:** alguien llama haciéndose pasar por un familiar, banco u organismo, con urgencia y pedido de dinero, datos o claves.
- **Percibe:** la conversación telefónica en el ambiente (lo que dice la persona mayor y, si se escucha, el otro lado).
- **Hace:** puede intervenir con tacto durante o después de la llamada ("Eso que te pidieron suena raro, ¿llamamos a tu hijo antes?").
- **Avisa:** **Urgente** de inmediato, con la transcripción o el audio disponible para que la familia entienda qué pasó y actúe (llamar al banco, a la persona mayor, a la policía).

### D4. Ingreso no esperado
- **Situación:** alguien entra de noche o cuando la persona no está en casa.
- **Avisa:** **Urgente** o **Emergencia** según el contexto, con imagen.

---

## E. Situaciones del entorno y del sistema

### E1. Corte de luz o de internet
- **Hace:** el sistema sigue funcionando con respaldo propio y tiene un canal alternativo para avisar.
- **Avisa:** **Atención** a la familia ("Se cortó la luz en lo de mamá"). Si ocurre una emergencia durante el corte, el aviso sale igual (premisa 9).

### E2. Dispositivo que deja de funcionar
- **Situación:** una cámara desconectada, el wearable sin batería, un sensor sin señal.
- **Hace:** se lo dice a la persona si puede resolverlo ("Che, el reloj se quedó sin batería").
- **Avisa:** **Atención** a la familia si no se resuelve, indicando qué área queda sin cobertura.

### E3. Riesgos en la casa
- **Situación:** una hornalla prendida mucho tiempo sin nadie en la cocina, una canilla abierta, la puerta abierta de noche, mucho calor o mucho frío en la casa.
- **Hace:** se lo recuerda a la persona.
- **Avisa:** **Atención**; **Urgente** si no se resuelve.

---

## Preguntas abiertas para los requisitos

1. **Tiempos:** cuánto espera el sistema una respuesta antes de escalar (propuesta inicial: segundos en A4, pocos minutos en A2). Se calibra en pruebas.
2. **Llamada a servicios de emergencia:** ~~si el sistema llama directo o solo la familia~~ → **Decidido:** opcional, ver "Cadena de contactos". Queda abierto cómo se hace técnicamente (qué información se le da al operador y cómo).
3. **Acceso de emergencia a la casa:** cómo entra quien acude si la persona no puede abrir (llave con un vecino, cerradura inteligente).
4. **Aprendizaje de la rutina:** cuántos días necesita el sistema para conocer a la persona antes de detectar cambios.
5. **Iniciativa del asistente:** cuánto conversa por iniciativa propia sin resultar invasivo. Probablemente sea configurable por persona.
