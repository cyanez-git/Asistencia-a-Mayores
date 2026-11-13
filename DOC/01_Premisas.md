# Premisas del proyecto
**Fecha:** 2026-11-13

Estas premisas guían todas las decisiones. Si una decisión posterior las contradice, se revisa la decisión, no la premisa (salvo que se acuerde cambiarla explícitamente).

---

## 1. Hacia dónde apuntamos

Una persona mayor que vive sola, o está sola la mayor parte del día, necesita **compañía** y necesita que **alguien se dé cuenta cuando algo anda mal**. Hoy eso lo cubren los hijos (con un desgaste emocional, de tiempo y de costos muy fuerte) o una persona de compañía.

Nuestra solución se piensa **desde el mismo concepto que una buena persona de compañía**: no es enfermera ni médica, pero conversa, conoce a la persona, nota cuando algo cambia y avisa a tiempo. Es una referencia interna para entender el enfoque, no un mensaje de producto.

## 2. Para quién

- **La persona mayor:** adulta y totalmente funcional. Vive sola o pasa la mayor parte del día sola. No tiene que aprender tecnología: le habla al asistente como le hablaría a alguien.
- **La familia:** recibe avisos y, cuando hace falta, información para entender qué pasó y actuar. Se libera de tener que estar pendiente todo el tiempo.

## 3. Premisas

1. **El asistente de voz es el protagonista.** Su primera función es ser compañía: muy empático, con conversación genuina sobre los temas que le interesan a la persona (sus gustos, su historia, sus noticias, su gente). Tiene que ser alguien con quien la persona *quiera* hablar todos los días.
2. **En segundo plano, el asistente observa e interpreta**, como lo haría una persona de compañía atenta:
   - **Ánimo:** cambios bruscos, señales depresivas evidentes, aislamiento.
   - **Salud (no médica):** dificultad para respirar, habla con dificultad o arrastrada, signos de que algo está pasando.
   - **Seguridad:** quién está en la casa (visita habitual o desconocido), qué tipo de conversación hay, situaciones de riesgo como estafas (p. ej., cuento del tío, pedidos de dinero o datos bancarios).
3. **Ver, oír y sentir.** La solución completa incluye:
   - **Oídos y voz:** micrófonos y parlantes.
   - **Ojos:** cámaras.
   - **Sensores para ambientes privados** (baño, dormitorio), donde no corresponde una cámara.
   - **Algo que lleva puesto la persona** que sense pulso, caídas y temperatura. No a nivel médico: alcanza con que indique que algo está pasando.
4. **Sirve para cualquier vivienda.** La arquitectura es la misma; una casa más grande o con más ambientes solo agrega más cámaras, sensores, micrófonos y parlantes.
5. **La v1 es la solución completa**, no un MVP recortado. Voz, cámaras, sensores de ambientes privados y wearable desde el inicio.
6. **Lo que el mercado ya cubre no se excluye.** Si existe algo que resuelve una parte, nuestra solución lo incluye y lo supera.
7. **No hay precio de referencia.** El costo no limita el diseño en esta etapa.
8. **El único límite de alcance: nada fuera de la casa.**
9. **La emergencia no depende de que todo funcione.** Si una situación grave se detecta, el aviso tiene que salir aunque falle internet, la luz o el modelo de IA.

## 4. Información hacia la familia

- Por defecto, la familia recibe **alertas y resúmenes**.
- **Transcripciones, audio o video son opcionales:** se habilitan porque la familia lo pide o porque **la situación lo requiere** (p. ej., una estafa en curso: la familia tiene que poder ver qué pasó y actuar).

## 5. Privacidad y marco legal

- La integridad de la persona mayor está por encima de todo. **No se recortan funcionalidades por temas de privacidad o legales en esta etapa.**
- Se tiene presente la **Ley 25.326 de Protección de Datos Personales** (datos de la persona, de las visitas y de terceros en llamadas). Se trabaja sobre esto **después de las primeras pruebas**.

## 6. Casos reales que nos guían

1. **Caída sin poder levantarse.** Una vecina se cayó, se desvaneció y al recuperarse no se podía levantar porque estaba muy golpeada. Pasaron muchas horas hasta que alguien se dio cuenta. No se recuperó y falleció. Pasar 10 horas en el piso sin poder moverse es grave a cualquier edad. **Lo que hay que lograr: que nadie quede horas tirado sin que la familia lo sepa.**
2. **Necesidad de compañía diaria.** Varios adultos mayores reclaman presencia o compañía, al menos un rato todos los días. Para los hijos eso implica reorganizar su rutina y estar pendientes todo el tiempo de si está bien, con un desgaste emocional muy fuerte, más allá de visitas y llamadas. **Lo que hay que lograr: que la persona tenga con quién conversar de lo que le interesa, y que la familia pueda estar tranquila sin vigilar.**

---

**Próximo paso:** escenarios concretos (qué pasa, qué detecta el sistema, qué hace y a quién avisa), partiendo de estos casos.
