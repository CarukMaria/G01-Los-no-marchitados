# SPEC 06 — Producción, retorno y TCO

**Módulo prioritario.** Produce tres resultados: (1) producción estimada, (2) ingresos y recuperación aproximada de la inversión, (3) costo total de propiedad (TCO) con mantenimiento, calibración y reposición. Todos son **estimaciones** (`AVISO_ESTIMACION`).

## 1. Objetivo
Transformar la configuración y la inversión en una visión económica comprensible: cuánto se produciría, cuánto se podría ingresar, cuándo se recuperaría aproximadamente la inversión y cuánto cuesta sostener la instalación a lo largo del tiempo.

## 2. Historias de usuario / casos de uso
- **HU-07** — Como productor, quiero ver cuánto podría producir y ganar, y en cuánto tiempo recuperaría lo invertido bajo qué supuestos.
- **HU-08** — Como productor, quiero ver los costos de mantenimiento, calibración y reposición en el tiempo para no considerar solo el costo inicial.

## 3. Requisitos funcionales

### RF-017 — Producción estimada
- **Descripción**: estimar producción, frecuencia de cosecha, rendimiento y capacidad.
- **Entradas**: `ConfiguracionProductiva` (`nModulos`), `Cultivo`, módulo, `ParametrosGlobales`, resultados de RF-005 (spec 02).
- **Procesamiento**:
  ```
  plazasTotales          = nModulos × plazasPorModulo
  kgAnio                 = plazasTotales × ciclosPorAnio × pesoCosechaKg × (1 − mortalidadPct)
  kgMes                  = kgAnio / 12
  kgSemana               = kgAnio / 52
  superficieProductivaM2 = nModulos × huellaModuloM2
  rendimientoKgM2Anio    = kgAnio / superficieProductivaM2
  frecuenciaCosecha      = "semanal (siembras escalonadas)"   (texto fijo del prototipo)
  ```
- **Salida**: `plazasTotales` (capacidad productiva), `kgAnio`, `kgMes`, `kgSemana`, `rendimientoKgM2Anio`, `frecuenciaCosecha`.
- **Reglas relacionadas**: RN-029, RN-034.
- **Prioridad**: Alta.
- **Criterios de aceptación**:
  - CA-01: demo (lechuga, NFT, 8 módulos) → `plazasTotales = 2.304`, `kgAnio ≈ 2.623,1`, `kgMes ≈ 218,6`, `kgSemana ≈ 50,4`, `rendimientoKgM2Anio ≈ 91,1`.
  - CA-02: doblar `nModulos` duplica `kgMes` (linealidad).
  - CA-03: se muestra como estimación con los parámetros del cultivo usados.

### RF-018 — Ingresos y costos operativos mensuales
- **Descripción**: estimar ingreso bruto mensual, costos operativos y flujo neto en régimen.
- **Entradas**: `kgMes`, `plazasTotales`, `nModulos`, `Cultivo` (`precioReferenciaKg`, `costoInsumosKg`), `ParametrosGlobales`.
- **Procesamiento**:
  ```
  kgVendiblesMes            = kgMes × porcentajeVentaEfectiva
  ingresoBrutoMensual       = kgVendiblesMes × precioReferenciaKg
  costoInsumosMensual       = kgMes × costoInsumosKg
  horasSemana               = horasBaseSemana + horasPor100PlazasSemana × plazasTotales / 100
  costoManoObraMensual      = horasSemana × valorHoraManoObra × semanasPorMes
  costoFijoMensual          = costoFijoMensualBase + costoFijoMensualPorModulo × nModulos
  costosOperativosMensuales = costoInsumosMensual + costoManoObraMensual + costoFijoMensual
  flujoNetoOperativoMensual = ingresoBrutoMensual − costosOperativosMensuales
  ```
- **Salida**: los valores anteriores más el desglose de costos.
- **Reglas relacionadas**: RN-030, RN-034.
- **Prioridad**: Alta.
- **Criterios de aceptación**:
  - CA-01: demo → `ingresoBrutoMensual ≈ 1.278.763`, `costoInsumosMensual ≈ 262.310`, `costoManoObraMensual ≈ 218.544`, `costoFijoMensual = 180.000`, `flujoNetoOperativoMensual ≈ 617.909`.
  - CA-02: si el precio de referencia baja lo suficiente, el flujo neto puede ser negativo y se muestra sin error.
  - CA-03: todos los supuestos (precio, % de venta, valor hora, costos fijos) se listan en pantalla.

### RF-019 — Recuperación aproximada de la inversión
- **Descripción**: simular mes a mes el flujo acumulado y hallar el primer mes en que se recupera la inversión.
- **Entradas**: `inversionInicial` (05), `flujoNetoOperativoMensual` y `costoFijoMensual` (RF-018), eventos de TCO por mes (RF-020), `cicloDias`, `horizonteMeses`.
- **Procesamiento**:
  ```
  mesesArranque = ceil(cicloDias / 30)
  acumulado(0)  = − inversionInicial
  para m = 1 .. horizonteMeses:
      flujoOperativo(m) = − costoFijoMensual                 si m ≤ mesesArranque   (sin cosecha aún)
                        =  flujoNetoOperativoMensual         si m > mesesArranque
      flujoMes(m)       = flujoOperativo(m) − costoEventosTCO(m)     (reposición + mantenimiento + calibración del mes m)
      acumulado(m)      = acumulado(m−1) + flujoMes(m)
  recuperacionMeses = primer m con acumulado(m) ≥ 0, o null si no ocurre dentro del horizonte
  recuperacionAnios = recuperacionMeses / 12   (1 decimal)
  ```
  Texto de resultado (obligatorio, con supuestos a la vista):
  - Con recuperación: *"Con los supuestos utilizados, la inversión inicial se recuperaría aproximadamente en {meses} meses ({años} años)."*
  - Sin recuperación: *"Con los supuestos utilizados, la inversión inicial no se recuperaría dentro de los {horizonte} meses analizados."*
- **Salida**: `recuperacionMeses`, `recuperacionAnios`, `flujoAcumuladoMensual[1..horizonte]`, texto de resultado.
- **Reglas relacionadas**: RN-031, RN-032, RN-034.
- **Prioridad**: Alta.
- **Criterios de aceptación**:
  - CA-01: demo (8 módulos, presupuesto 14.000.000) → `recuperacionMeses = 26` (≈ 2,2 años); tolerancia ±1 mes por redondeo.
  - CA-02: lechuga NFT con 4 módulos → 35 meses; con 6 módulos → 28 meses.
  - CA-03: lechuga DWC con cualquier `n` del demo → `null` y se muestra el texto "no se recuperaría…" sin error.
  - CA-04: el texto siempre incluye "Con los supuestos utilizados" y el aviso de no garantía.
  - CA-05: se puede mostrar el gráfico/lista del flujo acumulado mensual con el punto de recuperación marcado.

### RF-020 — TCO y matriz de mantenimiento
- **Descripción**: proyectar reposiciones, mantenimientos y calibraciones y armar la matriz de mantenimiento.
- **Entradas**: lista `ConfiguracionComponente` (04, con `cantidadRequerida`), catálogo (`vidaUtilMeses`, `mantenimientoFrecuenciaMeses`, `mantenimientoCostoPorEvento`, `mantenimientoTipo`, `precioUnitario`), `inversionInicial`, `horizonteMeses`, `fechaInicioEstimada`.
- **Procesamiento**: para cada línea con `cantidadRequerida > 0`:
  ```
  REPOSICION    en cada mes m (1..horizonte) con m mod vidaUtilMeses = 0:
                costo = cantidadRequerida × precioUnitario
  MANTENIMIENTO / CALIBRACION (según mantenimientoTipo)
                en cada mes m con m mod mantenimientoFrecuenciaMeses = 0:
                costo = cantidadRequerida × mantenimientoCostoPorEvento
  costoEventosTCO(m)  = Σ costos de eventos del mes m
  tcoTotal            = inversionInicial + Σ reposiciones + Σ mantenimientos + Σ calibraciones   (dentro del horizonte)
  totalesPorAnio[k]   = Σ costoEventosTCO de los meses 12(k−1)+1 .. 12k
  matrizMantenimiento = por componente: {componenteId, vidaUtilMeses, frecuenciaMantenimientoMeses,
                        costoPorEvento (línea), primerEventoMeses = min(vidaUtilMeses, frecuenciaMantenimientoMeses),
                        proximaFecha = fechaInicioEstimada + primerEventoMeses meses, o «—» si primerEventoMeses > horizonte}
  ```
- **Salida**: `ProyeccionTCO` (eventos, totales por año y por tipo, `tcoTotal`, matriz).
- **Reglas relacionadas**: RN-033, RN-035, RN-019 (spec 04).
- **Prioridad**: Alta.
- **Criterios de aceptación**:
  - CA-01: demo a 60 meses → reposiciones `2.894.440`, mantenimientos `632.800`, calibraciones `260.000`, `tcoTotal ≈ 17.101.163`.
  - CA-02: totales por año del demo → año 1 `323.560`, año 2 `573.560`, año 3 `1.183.000`, año 4 `683.560`, año 5 `1.023.560`.
  - CA-03: `SONDA_PH` (vida 12 meses) aparece repuesta en los meses 12, 24, 36, 48 y 60; `FILTRO` (vida 6) en cada semestre.
  - CA-04: componentes con vida mayor al horizonte (p. ej. `ESTR_INV`, 120 meses) no generan reposición dentro del horizonte pero sí su mantenimiento anual.
  - CA-05: la matriz muestra componente, vida útil, frecuencia de mantenimiento, costo y próxima fecha.
  - CA-06: el TCO se muestra en tabla y como acumulado a lo largo del tiempo, con `AVISO_ESTIMACION`.

## 4. Reglas de negocio
- **RN-029**: la producción es una estimación basada en parámetros del cultivo; se asume siembra escalonada (cosecha semanal aproximada) y `diasOperativosAnio` del parámetro global.
- **RN-030**: los ingresos consideran solo la fracción efectivamente vendida (`porcentajeVentaEfectiva`) al `precioReferenciaKg`; el resultado económico depende de supuestos explícitos.
- **RN-031**: durante `mesesArranque` no hay ingresos y solo se consideran costos fijos (supuesto simplificador declarado).
- **RN-032**: la recuperación de la inversión es una proyección bajo supuestos; nunca se presenta como garantía y siempre se acompaña de `AVISO_ESTIMACION`.
- **RN-033**: el TCO cubre inversión inicial, reposición, mantenimiento y calibración; **no incluye** costos operativos (insumos, mano de obra, costos fijos), que se muestran aparte. La reposición se valúa al costo de la línea (sin mano de obra ni imprevistos).
- **RN-034**: el panel de supuestos (parámetros usados: ciclo, peso, mortalidad, precio, % de venta, costos, horizonte) debe ser visible junto a los resultados.
- **RN-035**: los componentes existentes (situación B) participan del TCO de mantenimiento y reposición (vida útil completa, RN-019 del spec 04), pero su costo inicial no se incluye en `inversionInicial`.

## 5. Restricciones técnicas específicas
- Funciones puras: `estimarProduccion()`, `estimarIngresos()`, `proyectarTCO()`, `calcularRecuperacion()`. La fecha de inicio se recibe como entrada.
- El TCO se calcula **antes** que la recuperación (esta depende de `costoEventosTCO(m)`).
- Suma de meses a una fecha: mantener el día; si no existe (p. ej. 31), usar el último día del mes.

## 6. Modelo de datos

**EstimacionProduccionRetorno** (derivada)
| Atributo | Tipo | Oblig. |
|---|---|---|
| `proyectoId` | texto | Sí |
| `plazasTotales`, `kgAnio`, `kgMes`, `kgSemana`, `rendimientoKgM2Anio`, `superficieProductivaM2` | número | Sí |
| `frecuenciaCosecha` | texto | Sí |
| `ingresoBrutoMensual`, `costoInsumosMensual`, `costoManoObraMensual`, `costoFijoMensual`, `costosOperativosMensuales`, `flujoNetoOperativoMensual` | número | Sí |
| `mesesArranque` | entero | Sí |
| `recuperacionMeses` | entero o null | Sí |
| `recuperacionAnios` | número o null | Sí |
| `flujoAcumuladoMensual` | lista de número (1..horizonte) | Sí |
| `supuestos` | mapa de parámetros usados | Sí |

**ProyeccionTCO** (derivada; incluye los eventos de mantenimiento)
| Atributo | Tipo | Oblig. |
|---|---|---|
| `horizonteMeses` | entero | Sí |
| `eventos` | lista `{mes, componenteId, tipo (REPOSICION/MANTENIMIENTO/CALIBRACION), costo}` | Sí |
| `totalesPorAnio` | lista de número | Sí |
| `totalesPorTipo` | `{reposicion, mantenimiento, calibracion}` | Sí |
| `tcoTotal` | número | Sí |
| `matrizMantenimiento` | lista `{componenteId, vidaUtilMeses, frecuenciaMantenimientoMeses, costoPorEvento, proximaFecha}` | Sí |

## 7. Entradas y salidas
- **Entradas**: salidas de 02, 04, 05 y `ParametrosGlobales`.
- **Salidas**: resultados para la pantalla 5 y el resumen del spec 07.

## 8. Plan de tareas
| ID | Tarea | Descripción | Archivos/componentes afectados |
|---|---|---|---|
| T-06-01 | Producción | `estimarProduccion()` (RF-017) | lógica de producción |
| T-06-02 | Ingresos y costos | `estimarIngresos()` (RF-018) | lógica económica |
| T-06-03 | Proyección de TCO | `proyectarTCO()` + matriz de mantenimiento + suma de meses a fecha (RF-020) | lógica de TCO |
| T-06-04 | Recuperación | `calcularRecuperacion()` con simulación mensual y texto de resultado (RF-019) | lógica económica |
| T-06-05 | Pantalla 5 (producción y retorno) | Tarjetas de producción, ingresos, recuperación, panel de supuestos y avisos | pantalla resultados |
| T-06-06 | Pantalla 5 (TCO) | Tabla por año/tipo, matriz de mantenimiento y acumulado | pantalla resultados |

## 9. Criterios de aceptación
Los de RF-017 a RF-020. Adicional: la pantalla 5 del escenario demo muestra 26 meses (≈ 2,2 años) y `tcoTotal ≈ 17.101.163`.

## 10. Estrategia de verificación
- Normal: demo (valores en los CA); 4 y 6 módulos.
- Sin recuperación: DWC.
- Límites: recuperación exactamente en el mes 60 (debe reportarse) y en el mes 61 (→ `null`); `nModulos = 1`; cultivo con `cicloDias = 30` (`mesesArranque = 1`); precio de referencia `0`.
- Inválidos: `horizonteMeses ≤ 0`, `vidaUtilMeses` no entero → `DATO_INCONSISTENTE`.
- Comprobación cruzada: `tcoTotal = inversionInicial + suma de totalesPorTipo`; suma de `totalesPorAnio = suma de totalesPorTipo`.
- Datos faltantes: catálogo sin `vidaUtilMeses` en un componente requerido.

## Dependencias
- **Depende de**: `02`, `04`, `05`, `08`.
- **Es utilizado por**: `07`.
