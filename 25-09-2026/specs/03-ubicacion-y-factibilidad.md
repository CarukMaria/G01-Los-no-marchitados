# 03 — Ubicación y factibilidad

## 1. Objetivo

Registrar la ubicación, orientación y condiciones básicas del terreno del proyecto, y devolver una **evaluación preliminar de factibilidad** (no una certificación), usando datos mock/precargados como referencia técnica (incluida referencia conceptual a criterios del INTA).

## 2. Historias de usuario / casos de uso

- Como usuario, quiero indicar la ubicación aproximada de mi terreno, para que Brota use condiciones de referencia de esa zona.
- Como usuario, quiero indicar la orientación de mi terreno o invernadero (N/S/E/O), para recibir una recomendación de orientación adecuada.
- Como usuario, quiero cargar datos básicos del terreno (superficie, pendiente, disponibilidad de agua y electricidad, riesgo de inundación), para obtener una evaluación preliminar de factibilidad.
- Como usuario, quiero ver un espacio preparado para elegir mi ubicación en un mapa (a futuro), aunque en el prototipo sea una demo.

## 3. Requisitos funcionales

**RF-008 — Registrar ubicación**
- Entradas: ubicación de referencia (elegida de una lista mock de ubicaciones precargadas, spec 10, o texto libre) y, opcionalmente, coordenadas (lat/long) ingresadas manualmente.
- Procesamiento: asocia la ubicación al proyecto; si la ubicación coincide con una precargada, trae sus condiciones de referencia (clima, criterios INTA simplificados).
- Salida: `Ubicacion` asociada al proyecto.
- Prioridad: alta.

**RF-009 — Registrar orientación**
- Entradas: orientación elegida entre N, S, E, O o combinaciones simples (ej. NE).
- Procesamiento: compara contra la orientación de referencia sugerida para esa ubicación/cultivo (dato mock) y muestra si coincide o difiere.
- Salida: orientación guardada + mensaje de referencia (nunca "la única orientación correcta").
- Prioridad: media.

**RF-010 — Cargar condiciones del terreno**
- Entradas: superficie, largo, ancho (opcional), pendiente, disponibilidad de agua (sí/no o litros aprox.), disponibilidad eléctrica (sí/no), riesgo de inundación (bajo/medio/alto, dato mock según ubicación o ingresado por el usuario).
- Procesamiento: aplica reglas simples de factibilidad (RN-008 a RN-010).
- Salida: `Ubicacion` completa con resultado de evaluación preliminar.
- Prioridad: alta.

**RF-011 — Mostrar selector de coordenadas (demo)**
- Entradas: ninguna real; el usuario puede "seleccionar en mapa" pero el resultado es un valor mock/demo.
- Procesamiento: la integración con Google Maps queda como placeholder, claramente etiquetada como demo/preparada.
- Salida: coordenadas mock asociadas a la ubicación.
- Prioridad: baja (opcional para el hackatón, útil para la demo visual).

## 4. Reglas de negocio

**RN-008** — Si la superficie disponible es menor a la superficie mínima requerida por la escala definida en spec 02 (dato derivado, ej. m² por módulo), Brota debe marcar la factibilidad como "limitada" y sugerir reducir la escala.

**RN-009** — Si el riesgo de inundación es "alto", Brota debe advertirlo explícitamente como una limitación relevante, sin bloquear el flujo.

**RN-010** — La ausencia de agua o electricidad disponible se marca como limitación crítica: el proyecto puede seguir configurándose, pero la propuesta final debe mostrar esta limitación de forma destacada.

**RN-011** — La orientación sugerida depende de la ubicación y del cultivo/sistema (dato de referencia mock); nunca se presenta una única orientación como universalmente correcta (regla heredada del contexto del proyecto).

## 5. Restricciones técnicas específicas

No se requiere integración real con servicios de mapas, clima ni INTA. Toda la información de referencia por ubicación está precargada (spec 10). El selector de mapa es una demo visual sin conexión real.

## 6. Modelo de datos

**Ubicacion**
- `id`, `proyectoId`, `nombreReferencia` (ej. "Apóstoles, Misiones"), `latitud` (opcional/mock), `longitud` (opcional/mock), `orientacion` (enum N/S/E/O/combinaciones), `superficie`, `largo`, `ancho`, `pendiente`, `disponibilidadAgua` (bool o litros), `disponibilidadElectrica` (bool), `riesgoInundacion` (enum bajo/medio/alto), `resultadoFactibilidad` (enum: "favorable" / "limitada" / "crítica", calculado).

## 7. Entradas y salidas

Entrada: formulario de ubicación y condiciones del terreno.
Salida: `Ubicacion` con `resultadoFactibilidad`, usado en spec 04 (infraestructura), spec 09 (propuesta visual) y como contexto de la guía (spec 08).

## 8. Plan de tareas

| ID | Tarea | Archivos/componentes afectados |
|----|-------|---------------------------------|
| T03-01 | Selector de ubicación de referencia (lista mock) | UI ubicación |
| T03-02 | Formulario de orientación y condiciones del terreno | UI ubicación |
| T03-03 | Lógica de evaluación preliminar de factibilidad (RN-008 a RN-011) | lógica de negocio |
| T03-04 | Placeholder de selección en mapa (demo) | UI ubicación |
| T03-05 | Persistencia de `Ubicacion` | capa de datos |

## 9. Criterios de aceptación

```
CA-10
El usuario puede registrar una ubicación de referencia y ver condiciones asociadas (clima/orientación sugerida) precargadas.

CA-11
El usuario puede indicar orientación real de su terreno y ver si coincide con la sugerida.

CA-12
Si la superficie es insuficiente para la escala elegida, Brota lo advierte como factibilidad limitada.

CA-13
El selector de mapa está visible y claramente marcado como demo/preparado, sin bloquear el flujo si no se usa.
```

## 10. Estrategia de verificación

Probar: ubicación con superficie suficiente vs. insuficiente, riesgo de inundación alto, sin agua/electricidad disponible, orientación coincidente vs. distinta a la sugerida.

## 11. Dependencias

```
Depende de:
01 Proyecto y configuración
02 Planificación productiva (para validar superficie mínima según escala)

Es utilizado por:
04 Infraestructura y equipamiento
09 Propuesta visual
```
