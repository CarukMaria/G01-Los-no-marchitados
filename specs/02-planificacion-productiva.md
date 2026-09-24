# SPEC 02 — Planificación productiva

## 1. Objetivo
Definir qué se va a producir y cómo: cultivo, modalidad, y cantidad de módulos, ya sea a partir del **presupuesto** disponible (flujo funcional principal) o de un **objetivo de producción** (variante simplificada). Ambas variantes usan el mismo motor de configuración y estimación.

## 2. Historias de usuario / casos de uso
- **HU-02** — Como productor, quiero indicar cuánto puedo invertir (o cuánto quiero producir) para que el sistema me proponga una escala posible.
- **HU-03** — Como productor, quiero elegir cultivo y modalidad y ver cuántos módulos, plazas y qué superficie productiva implica mi configuración.

## 3. Requisitos funcionales

### RF-004 — Selección de cultivo y modalidad
- **Descripción**: elegir cultivo y modalidad de producción compatibles.
- **Entradas**: `cultivoId`, `modalidad` ∈ {`NFT`, `DWC`}.
- **Procesamiento**: listar solo cultivos del catálogo; al elegir cultivo, ofrecer únicamente sus `modalidadesCompatibles` (RN-007); guardar en `ConfiguracionProductiva`.
- **Salida**: `cultivoId` y `modalidad` válidos; muestra los parámetros del cultivo (ciclo, peso de cosecha, densidad, precio de referencia) como supuestos.
- **Reglas relacionadas**: RN-007.
- **Prioridad**: Alta.
- **Criterios de aceptación**:
  - CA-01: solo se pueden elegir modalidades compatibles con el cultivo.
  - CA-02: cambiar de cultivo a uno incompatible con la modalidad actual obliga a elegir otra modalidad.
  - CA-03: la selección se guarda y marca los pasos 4–6 como desactualizados si cambió.
  - CA-04: se muestran los parámetros del cultivo usados en los cálculos.

### RF-005 — Parámetros productivos por módulo
- **Descripción**: calcular la capacidad de un módulo para el cultivo y modalidad elegidos.
- **Entradas**: `Cultivo`, módulo del catálogo de la modalidad (`atributosModulo`), `ParametrosGlobales` (`diasOperativosAnio`, `diasLimpiezaEntreCiclos`).
- **Procesamiento**:
  ```
  plazasPorModulo   = floor(plazasBase × factorDensidad)
  ciclosPorAnio     = diasOperativosAnio / (cicloDias + diasLimpiezaEntreCiclos)
  kgAnioPorModulo   = plazasPorModulo × ciclosPorAnio × pesoCosechaKg × (1 − mortalidadPct)
  kgMesPorModulo    = kgAnioPorModulo / 12
  huellaModuloM2    = largoM × anchoM            (del módulo)
  ```
- **Salida**: `plazasPorModulo`, `ciclosPorAnio`, `kgMesPorModulo`, `huellaModuloM2`.
- **Reglas relacionadas**: RN-008.
- **Prioridad**: Alta.
- **Criterios de aceptación**:
  - CA-01: lechuga + NFT → `plazasPorModulo = 288`, `ciclosPorAnio = 6.875`, `kgMesPorModulo ≈ 27,32`.
  - CA-02: rúcula + NFT → `plazasPorModulo = 432`.
  - CA-03: los resultados son función exclusiva de datos del catálogo y parámetros (ningún número escrito en el código).
  - CA-04: se rechaza con `DATO_INCONSISTENTE` un cultivo con `cicloDias ≤ 0` o `mortalidadPct` fuera de [0, 1).

### RF-006 — Dimensionamiento por presupuesto (flujo principal)
- **Descripción**: determinar la mayor cantidad de módulos posible dentro del presupuesto y del terreno.
- **Entradas**: `presupuestoDisponible` (obligatorio en este modo), `nMaxTerreno` = `capacidadPorModalidad[modalidad]` (spec 03), función `calcularInversion(nModulos)` (spec 05).
- **Procesamiento**:
  ```
  n* = max { n ∈ [1 .. nMaxTerreno] : inversionInicial(n) ≤ presupuestoDisponible }
  limitadoPor = TERRENO       si n* = nMaxTerreno
              = PRESUPUESTO   en otro caso
  ```
  Si `nMaxTerreno = 0` → `estadoDimensionamiento = SUPERFICIE_INSUFICIENTE`. Si `inversionInicial(1) > presupuestoDisponible` → `PRESUPUESTO_INSUFICIENTE` y `nModulos = 1` como referencia. Caso contrario `OK`.
  El usuario puede luego ajustar `nModulos` manualmente entre 1 y `nMaxTerreno` (`ajusteManual = true`).
- **Salida**: `nModulos`, `limitadoPor`, `estadoDimensionamiento`, y (si corresponde) el aviso de qué presupuesto mínimo se necesita (`inversionInicial(1)`).
- **Reglas relacionadas**: RN-006, RN-009, RN-010.
- **Prioridad**: Alta.
- **Criterios de aceptación**:
  - CA-01: escenario `DEMO-APOSTOLES` (presupuesto 14.000.000) → `nModulos = 8`, `limitadoPor = TERRENO`.
  - CA-02: presupuesto 8.000.000 con el mismo terreno → `nModulos = 4`, `limitadoPor = PRESUPUESTO`.
  - CA-03: presupuesto 3.000.000 → `PRESUPUESTO_INSUFICIENTE` con `nModulos = 1` y mensaje con el monto mínimo.
  - CA-04: sin presupuesto ingresado no se puede usar este modo (error `CAMPO_OBLIGATORIO`).
  - CA-05: el ajuste manual a un valor fuera de [1, `nMaxTerreno`] es rechazado.

### RF-007 — Dimensionamiento por objetivo (variante simplificada)
- **Descripción**: estimar los módulos necesarios para alcanzar una producción mensual objetivo.
- **Entradas**: `objetivoKgMes` (> 0), `kgMesPorModulo` (RF-005), `nMaxTerreno`.
- **Procesamiento**:
  ```
  nRequerido = ceil(objetivoKgMes / kgMesPorModulo)
  si nRequerido ≤ nMaxTerreno: nModulos = nRequerido; estado = OK
  si no: nModulos = nMaxTerreno; estado = SUPERFICIE_INSUFICIENTE
  ```
  Con `SUPERFICIE_INSUFICIENTE` se informa `nRequerido`, el área de invernadero que exigiría (usando `calcularLayout` de 03 sin límite de terreno) y `nMaxTerreno`. Si además hay `presupuestoDisponible`, la comparación la resuelve el spec 05 (RF-016).
- **Salida**: `nRequerido`, `nModulos`, `estadoDimensionamiento`.
- **Reglas relacionadas**: RN-006, RN-009, RN-010.
- **Prioridad**: Media (variante simplificada sobre el mismo motor).
- **Criterios de aceptación**:
  - CA-01: lechuga NFT con objetivo 150 kg/mes → `nRequerido = 6`, `estado = OK`.
  - CA-02: objetivo 400 kg/mes en el terreno demo → `nRequerido = 15`, `nModulos = 8`, `SUPERFICIE_INSUFICIENTE`.
  - CA-03: objetivo `0` o negativo es rechazado.
  - CA-04: el resto del flujo (spec 04 en adelante) funciona igual que en el modo presupuesto.

## 4. Reglas de negocio
- **RN-006**: `nModulos` es entero ≥ 1 y ≤ `nMaxTerreno` (spec 03). Nunca se genera una configuración que exceda el límite físico del terreno.
- **RN-007**: la modalidad debe pertenecer a `modalidadesCompatibles` del cultivo y debe existir un módulo de catálogo para esa modalidad.
- **RN-008**: los parámetros productivos provienen exclusivamente de datos configurables (`Cultivo`, módulo, `ParametrosGlobales`).
- **RN-009**: el modo por defecto es `PRESUPUESTO` (en situación C se sugiere `OBJETIVO`, editable). Ambos modos usan el mismo motor: la inversión para cualquier `n` se obtiene siempre con `calcularInversion(n)` (spec 05).
- **RN-010**: `estadoDimensionamiento` ∈ {`OK`, `PRESUPUESTO_INSUFICIENTE`, `SUPERFICIE_INSUFICIENTE`}; cuando no es `OK` la UI debe mostrar el motivo y no presentar la propuesta como viable.

## 5. Restricciones técnicas específicas
- `calcularInversion(n)` debe ser una función pura invocable múltiples veces (una por cada `n` probado).
- Este spec no calcula costos ni layout: los solicita a los specs 05 y 03 a través de sus funciones públicas.

## 6. Modelo de datos

**Cultivo** (catálogo, `specs/08`)
| Atributo | Tipo | Oblig. |
|---|---|---|
| `id` | texto (`LECHUGA`) | Sí |
| `nombre` | texto | Sí |
| `modalidadesCompatibles` | lista de `NFT`/`DWC` | Sí |
| `cicloDias` | entero > 0 (incluye almácigo) | Sí |
| `pesoCosechaKg` | número > 0 (por planta) | Sí |
| `mortalidadPct` | número [0,1) | Sí |
| `factorDensidad` | número > 0 | Sí |
| `precioReferenciaKg` | número ≥ 0 | Sí |
| `costoInsumosKg` | número ≥ 0 | Sí |
| `phMin`, `phMax`, `tempMinC`, `tempMaxC` | número | No (informativos) |

**ConfiguracionProductiva** (del proyecto)
| Atributo | Tipo | Oblig. | Relación |
|---|---|---|---|
| `proyectoId` | texto | Sí | → `Proyecto` |
| `cultivoId` | texto | Sí | → `Cultivo` |
| `modalidad` | `NFT`/`DWC` | Sí | |
| `modoPlanificacion` | `PRESUPUESTO`/`OBJETIVO` | Sí | |
| `objetivoKgMes` | número > 0 | Cond. (OBJETIVO) | |
| `nRequerido` | entero | Cond. (OBJETIVO) | derivado |
| `nModulos` | entero ≥ 1 | Sí | derivado o ajustado |
| `ajusteManual` | booleano | Sí | |
| `limitadoPor` | `TERRENO`/`PRESUPUESTO` | Cond. | derivado |
| `estadoDimensionamiento` | enum RN-010 | Sí | derivado |
| `plazasTotales` | entero | Sí | `nModulos × plazasPorModulo` |
| `superficieProductivaM2` | número | Sí | `nModulos × huellaModuloM2` |

## 7. Entradas y salidas
- **Entradas**: pantalla 3 (cultivo, modalidad, modo, presupuesto/objetivo), `Proyecto`, catálogos.
- **Salidas**: `ConfiguracionProductiva` → consumida por 03 (revalida factibilidad con `nModulos`), 04 (cantidades), 05, 06, 07.

## 8. Plan de tareas
| ID | Tarea | Descripción | Archivos/componentes afectados |
|---|---|---|---|
| T-02-01 | Repositorio de cultivos | `obtenerCultivos()` sobre datos semilla + validación de consistencia (RF-005 CA-04) | datos, repositorio |
| T-02-02 | Función de parámetros productivos | `calcularParametrosModulo(cultivo, modulo, parametros)` (RF-005) | lógica de producción |
| T-02-03 | Selección cultivo/modalidad | Pantalla 3 parte A con compatibilidad (RF-004) | pantalla producción |
| T-02-04 | Modelo `ConfiguracionProductiva` | Persistencia y marcado de pasos desactualizados | modelo, repositorio |
| T-02-05 | Dimensionar por presupuesto | `dimensionarPorPresupuesto()` (RF-006). *Requiere 03 y 05 implementados* | lógica de dimensionamiento |
| T-02-06 | Dimensionar por objetivo | `dimensionarPorObjetivo()` (RF-007). *Requiere 03* | lógica de dimensionamiento |
| T-02-07 | Pantalla 3 parte B | Selector de modo, campos, resultado, ajuste manual y avisos de estado | pantalla producción |

## 9. Criterios de aceptación
Los de RF-004 a RF-007. Adicional: con el escenario demo se llega a `nModulos = 8` con lechuga/NFT y presupuesto 14.000.000.

## 10. Estrategia de verificación
- Normal: demo presupuesto (8 módulos), demo objetivo (6 módulos).
- Inválidos: cultivo inexistente; modalidad incompatible (albahaca + DWC); objetivo 0/negativo/texto; presupuesto vacío en modo presupuesto.
- Límites: presupuesto exactamente igual a `inversionInicial(n)` (debe admitir `n`); `nModulos = nMaxTerreno`; `nModulos = nMaxTerreno + 1` (rechazado).
- Presupuesto insuficiente y superficie insuficiente (RF-006 CA-03, RF-007 CA-02).
- Datos faltantes: cultivo con campos obligatorios ausentes → `DATO_INCONSISTENTE`.

## Dependencias
- **Depende de**: `01`, `08`. **Uso diferido (interfaces)** de `03` (`capacidadPorModalidad`, `calcularLayout`) y `05` (`calcularInversion`) solo para RF-006/RF-007: implementar T-02-05 y T-02-06 después de esos specs.
- **Es utilizado por**: `03` (revalidación con `nModulos`), `04`, `05`, `06`, `07`.
