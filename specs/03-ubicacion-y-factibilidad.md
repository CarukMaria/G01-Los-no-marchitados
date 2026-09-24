# SPEC 03 — Ubicación y factibilidad

## 1. Objetivo
Registrar las condiciones básicas del terreno, obtener los criterios de referencia de la localidad (orientación recomendada, clima), calcular **cuántos módulos admite el terreno** y emitir una **evaluación preliminar de factibilidad** con un semáforo y observaciones.

**Qué significa "factibilidad" en el prototipo**: que, con los datos ingresados y reglas simplificadas, el terreno no presenta impedimentos en superficie/dimensiones, disponibilidad de agua y electricidad, pendiente, riesgo de inundación y orientación respecto del criterio de referencia. **No** es un estudio de suelo, ingeniería ni un certificado de factibilidad constructiva (`AVISO_FACTIBILIDAD`).

## 2. Historias de usuario / casos de uso
- **HU-04** — Como productor, quiero cargar las condiciones de mi terreno y ver si es adecuado y cuánto puedo instalar, con la orientación recomendada para mi zona, para saber si conviene seguir con el proyecto.

## 3. Requisitos funcionales

### RF-008 — Condiciones del terreno
- **Descripción**: capturar las condiciones básicas del terreno.
- **Entradas**: `ejeLargoTerreno` ∈ {`EO`, `NS`} (hacia dónde corre el lado más largo del terreno, referido a los puntos cardinales), `pendientePct` (0–100), `riesgoInundacion` ∈ {`BAJO`, `MEDIO`, `ALTO`}, `aguaDisponible` ∈ {`SI`, `LIMITADA`, `NO`}, `electricidad` ∈ {`RED`, `ALTERNATIVA`, `NO`}. Las dimensiones y la localidad se toman del `Proyecto` (spec 01).
- **Procesamiento**: validar (RN-012); guardar en `CondicionesTerreno`.
- **Salida**: `CondicionesTerreno` persistido; paso 2 parcialmente completo.
- **Reglas relacionadas**: RN-012.
- **Prioridad**: Alta.
- **Criterios de aceptación**:
  - CA-01: todos los campos son obligatorios y se valida cada rango/enumeración.
  - CA-02: `pendientePct` fuera de [0, 100] se rechaza.
  - CA-03: al cambiar cualquier condición se recalcula la evaluación y los pasos posteriores quedan desactualizados.

### RF-009 — Criterios de ubicación (referencia INTA / mock)
- **Descripción**: obtener los criterios de referencia de la localidad del proyecto.
- **Entradas**: `localidadId`.
- **Procesamiento**: invocar `ServicioCriteriosUbicacion.obtenerCriterios(localidadId)` (mock en el prototipo, `specs/08`). Devuelve `CriterioUbicacion` con `ejeLargoRecomendado`, `justificacionEje`, resumen climático, `fuente` y `estadoValidacion`.
- **Salida**: criterio de la localidad con su fuente. Si `estadoValidacion = VALOR_DEMO_NO_VALIDADO` se muestra una nota visible ("criterio de demostración pendiente de validación").
- **Reglas relacionadas**: RN-013, RN-014.
- **Prioridad**: Alta.
- **Criterios de aceptación**:
  - CA-01: para Apóstoles se muestra el eje recomendado, su justificación y su fuente.
  - CA-02: la orientación recomendada **depende de la localidad** (dos localidades del catálogo pueden tener ejes distintos).
  - CA-03: si el servicio falla, se usa el dato semilla y se muestra una advertencia; el flujo no se bloquea.
  - CA-04: ningún valor de orientación está escrito en la lógica de evaluación.

### RF-010 — Capacidad del terreno y layout del invernadero
- **Descripción**: calcular el eje de implantación, la cantidad máxima de módulos por modalidad y las dimensiones del invernadero para una cantidad dada de módulos.
- **Entradas**: `Proyecto.ubicacion` (`largoM`, `anchoM`), `CondicionesTerreno.ejeLargoTerreno`, `CriterioUbicacion.ejeLargoRecomendado`, módulos del catálogo, `ParametrosGlobales`.
- **Procesamiento** (función `calcularCapacidad` y función `calcularLayout`):
  ```
  dimEnEje(eje):   si ejeLargoTerreno = eje  → largoM ; si no → anchoM
  largoDisp(eje)  = dimEnEje(eje) − 2 × retiroPerimetralM
  transvDisp(eje) = dimEnEje(otroEje) − 2 × retiroPerimetralM

  anchoInvernaderoM = filasPorInvernadero × anchoModulo + pasilloCentralM + 2 × pasilloLateralM
  maxModulos(eje, módulo) =
        0                                          si transvDisp(eje) < anchoInvernaderoM
                                                     o largoDisp(eje) < zonaTecnicaM + largoModulo
        filasPorInvernadero × floor((largoDisp(eje) − zonaTecnicaM) / largoModulo)   en otro caso

  Eje de implantación (por modalidad):
     si maxModulos(recomendado) ≥ 1 → eje = recomendado
     si no, si maxModulos(otro) ≥ 1  → eje = otro (advertencia FAC-06)
     si no                           → eje = recomendado, capacidad 0
  capacidadPorModalidad[mod] = { eje, maxModulos(eje, módulo(mod)), maxModulosEjeAlterno }

  calcularLayout(n, módulo):
     modulosPorFila      = [ ceil(n / 2), floor(n / 2) ]
     largoInvernaderoM   = ceil(n / 2) × largoModulo + zonaTecnicaM
     areaInvernaderoM2   = anchoInvernaderoM × largoInvernaderoM
     alturaLateralM, alturaCumbreraM = de ParametrosGlobales
  ```
- **Salida**: `capacidadPorModalidad` y `Layout` `{ejeInvernadero, anchoInvernaderoM, largoInvernaderoM, areaInvernaderoM2, modulosPorFila}`.
- **Reglas relacionadas**: RN-006, RN-013, RN-015.
- **Prioridad**: Alta.
- **Criterios de aceptación**:
  - CA-01: terreno 20 × 15 m, `ejeLargoTerreno = EO`, eje recomendado `EO`, retiro 1,5 m → NFT: `maxModulos = 8`, `anchoInvernaderoM = 4,8`.
  - CA-02: mismo terreno con `ejeLargoTerreno = NS` (eje recomendado `EO`) → NFT: `maxModulos = 6` y `maxModulosEjeAlterno = 8`.
  - CA-03: `calcularLayout(8, NFT)` → `largoInvernaderoM = 14,4`, `areaInvernaderoM2 = 69,12`, `modulosPorFila = [4, 4]`.
  - CA-04: `calcularLayout(5, NFT)` → `modulosPorFila = [3, 2]`, `largoInvernaderoM = 11,4`.
  - CA-05: terreno 5 × 5 m → `maxModulos = 0`.

### RF-011 — Evaluación preliminar de factibilidad
- **Descripción**: evaluar reglas simples y emitir semáforo global y observaciones.
- **Entradas**: `Proyecto`, `CondicionesTerreno`, `CriterioUbicacion`, resultado de RF-010, opcionalmente `ConfiguracionProductiva` (para revalidar con `nModulos`).
- **Procesamiento**: aplicar las reglas `FAC-01…FAC-07` (RN-014); `estadoGlobal` = `ROJO` si hay alguna observación `CRITICA`, `AMARILLO` si hay alguna `ADVERTENCIA`, `VERDE` en otro caso.
- **Salida**: `EvaluacionFactibilidad` `{estadoGlobal, observaciones[{regla, severidad, mensaje}], capacidadPorModalidad, aviso}`.
- **Reglas relacionadas**: RN-011, RN-014, RN-016.
- **Prioridad**: Alta.
- **Criterios de aceptación**:
  - CA-01: escenario demo (terreno 20×15, EO, pendiente 1 %, inundación BAJO, agua SI, electricidad RED) → `VERDE`.
  - CA-02: pendiente 4 % → `AMARILLO` con observación FAC-04.
  - CA-03: agua `NO` → `ROJO` con observación FAC-02; se permite continuar solo si hay capacidad ≥ 1 (RN-016).
  - CA-04: terreno 5 × 5 m → `ROJO` con FAC-01 y se bloquea el avance.
  - CA-05: toda evaluación incluye el `AVISO_FACTIBILIDAD`.
  - CA-06: cada observación indica la regla que la originó y una recomendación breve.

## 4. Reglas de negocio
- **RN-011**: la evaluación es preliminar y siempre debe mostrar sus limitaciones (`AVISO_FACTIBILIDAD`).
- **RN-012**: validaciones — `pendientePct` ∈ [0, 100]; enumeraciones exactas; todos obligatorios.
- **RN-013**: el eje recomendado proviene del `CriterioUbicacion` de la localidad; el sistema **no asume una única orientación universal**. Si el terreno no admite el eje recomendado se usa el otro con advertencia.
- **RN-014**: reglas de evaluación:

| ID | Condición | Severidad |
|---|---|---|
| FAC-01 | `capacidad = 0` para la modalidad (o para todas si aún no hay configuración); o `nModulos > maxModulos` | `CRITICA` |
| FAC-02 | agua `SI` → sin observación; `LIMITADA` → `ADVERTENCIA`; `NO` → `CRITICA` | según valor |
| FAC-03 | electricidad `RED` → ninguna; `ALTERNATIVA` → `ADVERTENCIA`; `NO` → `CRITICA` | según valor |
| FAC-04 | pendiente ≤ 2 % → ninguna; > 2 % y ≤ 5 % → `ADVERTENCIA` (requiere nivelación); > 5 % → `CRITICA` | según valor |
| FAC-05 | inundación `BAJO` → ninguna; `MEDIO` → `ADVERTENCIA`; `ALTO` → `CRITICA` | según valor |
| FAC-06 | eje de implantación ≠ recomendado → `ADVERTENCIA`; si `maxModulosEjeAlterno > maxModulos` → `INFO` indicando que otro eje admitiría más módulos pero se aparta del criterio | según caso |
| FAC-07 | informar resumen climático de la localidad (heladas, calor) | `INFO` (no afecta el semáforo) |

- **RN-015**: el layout es una función de `n`, del módulo y de parámetros; el croquis (spec 07) y la superficie de invernadero (spec 05) usan exclusivamente este layout.
- **RN-016**: una evaluación `ROJO` permite continuar con un aviso persistente, **excepto** cuando FAC-01 es `CRITICA` (no existe configuración posible), que bloquea el avance.

## 5. Restricciones técnicas específicas
- La consulta de criterios se hace **solo** a través de la interfaz `ServicioCriteriosUbicacion` (mock semilla en el prototipo).
- Puntos de extensión (**no implementar**): mapas, datos topográficos y de elevación, imágenes satelitales, medición con cámara, riesgo de inundación oficial, análisis avanzado del terreno. Su lugar previsto es reemplazar el origen de las entradas de RF-008/RF-009.
- Simplificaciones declaradas del prototipo: terreno rectangular, eje largo solo `EO`/`NS`, layout de 2 filas, sin exposición solar ni sombras, sin profundidad para infraestructura subterránea (los tanques son superficiales).

## 6. Modelo de datos

**CondicionesTerreno**
| Atributo | Tipo | Oblig. | Relación |
|---|---|---|---|
| `proyectoId` | texto | Sí | → `Proyecto` |
| `ejeLargoTerreno` | `EO`/`NS` | Sí | |
| `pendientePct` | número 0–100 | Sí | |
| `riesgoInundacion` | `BAJO`/`MEDIO`/`ALTO` | Sí | |
| `aguaDisponible` | `SI`/`LIMITADA`/`NO` | Sí | |
| `electricidad` | `RED`/`ALTERNATIVA`/`NO` | Sí | |

**CriterioUbicacion** (catálogo, `specs/08`)
| Atributo | Tipo | Oblig. |
|---|---|---|
| `localidadId` | texto (`APOSTOLES`) | Sí |
| `nombre`, `provincia` | texto | Sí |
| `latitud`, `longitud` | número | Sí |
| `climaResumen` | texto | Sí |
| `riesgoHeladas`, `riesgoCalorExtremo` | `BAJO`/`MEDIO`/`ALTO` | Sí |
| `ejeLargoRecomendado` | `EO`/`NS` | Sí |
| `justificacionEje` | texto | Sí |
| `fuente` | texto | Sí |
| `estadoValidacion` | `VALIDADO`/`VALOR_DEMO_NO_VALIDADO` | Sí |

**EvaluacionFactibilidad** (derivada; se recalcula)
`estadoGlobal`, `observaciones[]`, `capacidadPorModalidad{ NFT:{eje, maxModulos, maxModulosEjeAlterno}, DWC:{…} }`, `aviso`.

**Layout** (derivado; no se persiste)
`ejeInvernadero`, `anchoInvernaderoM`, `largoInvernaderoM`, `areaInvernaderoM2`, `modulosPorFila[2]`.

## 7. Entradas y salidas
- **Entradas**: `Proyecto` (01), formulario de condiciones (pantalla 2), servicio de criterios (mock).
- **Salidas**: `EvaluacionFactibilidad` y `capacidadPorModalidad` → 02; `Layout` → 04, 05, 07; observaciones → 07 (resumen).

## 8. Plan de tareas
| ID | Tarea | Descripción | Archivos/componentes afectados |
|---|---|---|---|
| T-03-01 | Servicio de criterios (mock) | Interfaz `ServicioCriteriosUbicacion` + implementación semilla con sobre de respuesta común | servicios, datos |
| T-03-02 | Modelo y validación de condiciones | `CondicionesTerreno` + RN-012 | modelo, validación |
| T-03-03 | Función de capacidad y layout | `calcularCapacidad()` y `calcularLayout()` puras (RF-010) | lógica de terreno |
| T-03-04 | Función de evaluación | `evaluarFactibilidad()` con FAC-01…FAC-07 (RF-011) | lógica de factibilidad |
| T-03-05 | Pantalla 2 | Formulario, criterio de la localidad con fuente, capacidad por modalidad, semáforo y observaciones | pantalla ubicación |
| T-03-06 | Revalidación con configuración | Volver a evaluar al conocerse `nModulos` (FAC-01) | lógica de factibilidad, pantalla propuesta |

## 9. Criterios de aceptación
Los de RF-008 a RF-011. Adicional: el escenario demo produce `VERDE` y `maxModulos(NFT) = 8`.

## 10. Estrategia de verificación
- Normal: escenario demo (verde); variante `NS` (6 módulos + INFO).
- Inválidos: pendiente −1 y 101; enumeraciones no válidas; localidad inexistente.
- Límites: pendiente 2 % (sin observación) y 2,01 % (advertencia); pendiente 5 % (advertencia) y 5,01 % (crítica); terreno justo en el mínimo para 2 módulos (`largoDisp = 5,4`) y 0,1 m menos (capacidad 0).
- Superficie insuficiente (5 × 5 m) y datos faltantes (condiciones sin cargar → paso 2 `PENDIENTE`, no se puede avanzar).
- Servicio de criterios caído: usa semilla y muestra advertencia.

## Dependencias
- **Depende de**: `01`, `08`. (En la revalidación T-03-06 también usa `02`.)
- **Es utilizado por**: `02` (capacidad), `04`, `05`, `06`, `07`.
