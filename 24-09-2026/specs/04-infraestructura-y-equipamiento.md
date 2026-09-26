# SPEC 04 — Infraestructura y equipamiento

## 1. Objetivo
A partir de la configuración productiva y del layout, determinar **qué componentes de infraestructura y equipamiento se necesitan, en qué cantidad y cuáles hay que comprar** descontando lo existente. Es el puente entre la planificación productiva y la estimación económica.

## 2. Historias de usuario / casos de uso
- **HU-05** — Como productor, quiero ver la lista de infraestructura y equipamiento que necesita mi configuración (y qué ya tengo), para saber qué debo adquirir.

## 3. Requisitos funcionales

### RF-012 — Generación de componentes requeridos
- **Descripción**: calcular la cantidad requerida de cada componente aplicable a la modalidad.
- **Entradas**: `ConfiguracionProductiva` (`modalidad`, `nModulos`), `Layout` (spec 03, `areaInvernaderoM2`), catálogo de `Componente`, módulo de la modalidad (`litrosReservaPorModulo`).
- **Procesamiento**: para cada componente cuyo `aplicaModalidades` incluya la modalidad, aplicar su `reglaCantidad` (RN-017):
  ```
  FIJA                    : cantidad = parametroRegla
  POR_MODULO              : cantidad = nModulos × parametroRegla
  POR_M2_INVERNADERO      : cantidad = areaInvernaderoM2 × parametroRegla        (sin redondear, unidad m²)
  CADA_N_MODULOS          : cantidad = max(1, ceil(nModulos / parametroRegla))
  CADA_N_M2_INVERNADERO   : cantidad = ceil(areaInvernaderoM2 / parametroRegla)
  POR_CAPACIDAD_TANQUE    : cantidad = ceil(nModulos × litrosReservaPorModulo / parametroRegla)   (parametroRegla = capacidad del tanque en L)
  cantidadRequerida = cantidad + cantidadExtra        (cantidadExtra: RF-014, por defecto 0)
  ```
- **Salida**: lista de `ConfiguracionComponente` agrupada por `grupo` (INFRAESTRUCTURA / EQUIPAMIENTO) y `categoria`, con `cantidadRequerida`.
- **Reglas relacionadas**: RN-017, RN-018.
- **Prioridad**: Alta.
- **Criterios de aceptación**:
  - CA-01: escenario demo (NFT, 8 módulos, área 69,12 m²) → `ESTR_INV = 69,12`, `CUBIERTA = 138,24`, `VENTILADOR = 4`, `MOD_NFT = 8`, `TANQUE_500 = 3`, `KIT_CANIA = 8`, `FILTRO = 2`, `BOMBA_RECIRC = 2`, `AIREADOR = 2`, `SONDA_PH = 1`, `MEDIDOR_EC = 1`, `SENSOR_TEMP = 1`, `KIT_ELEC = 1`.
  - CA-02: con modalidad DWC no aparecen `MOD_NFT` ni `BOMBA_RECIRC`; aparece `MOD_DWC`.
  - CA-03: si cambia `nModulos`, todas las cantidades se recalculan sin intervención manual.
  - CA-04: no aparece ningún componente sin regla de cantidad; un componente con regla desconocida produce `DATO_INCONSISTENTE`.

### RF-013 — Infraestructura existente y cantidad a adquirir
- **Descripción**: descontar lo que el usuario ya posee (situación B; en A y C la lista existente suele ser vacía).
- **Entradas**: `Proyecto.infraestructuraExistente`, resultado de RF-012.
- **Procesamiento**:
  ```
  cantidadExistente = cantidad declarada en el proyecto (0 si no figura)
  cantidadAComprar  = max(0, cantidadRequerida − cantidadExistente)
  cobertura         = SIN_EXISTENTE  si cantidadExistente = 0
                      PARCIAL        si 0 < cantidadExistente < cantidadRequerida
                      COMPLETA       si cantidadExistente ≥ cantidadRequerida
  ```
  Componentes existentes que no aplican a la configuración se listan como información ("no utilizado en esta configuración") y no generan costo.
  `requiereInfraAdicional = existe alguna línea con cobertura PARCIAL`.
- **Salida**: `ConfiguracionComponente` completo por línea + bandera `requiereInfraAdicional`.
- **Reglas relacionadas**: RN-019.
- **Prioridad**: Alta.
- **Criterios de aceptación**:
  - CA-01: existente `TANQUE_500 = 2` con requerido 3 → `cantidadAComprar = 1`, cobertura `PARCIAL`, `requiereInfraAdicional = true`.
  - CA-02: existente mayor al requerido → `cantidadAComprar = 0`, cobertura `COMPLETA`, sin cantidades negativas.
  - CA-03: en situación A todas las líneas tienen `cantidadExistente = 0`.
  - CA-04: un componente existente que no aplica se muestra como informativo y no suma al costo.

### RF-014 — Ajuste manual de equipamiento
- **Descripción**: permitir agregar unidades extra a componentes ajustables.
- **Entradas**: `componenteId`, `cantidadExtra` (entero ≥ 0).
- **Procesamiento**: solo si `Componente.ajustable = true`; recalcular RF-012/RF-013 para esa línea.
- **Salida**: línea actualizada; pasos 5–6 quedan desactualizados hasta recalcular.
- **Reglas relacionadas**: RN-020.
- **Prioridad**: Media.
- **Criterios de aceptación**:
  - CA-01: `BOMBA_RECIRC` con `cantidadExtra = 1` → `cantidadRequerida` aumenta en 1.
  - CA-02: no se puede editar la cantidad de `ESTR_INV`, `CUBIERTA` ni de los módulos (no ajustables).
  - CA-03: `cantidadExtra` negativa o no entera se rechaza.

## 4. Reglas de negocio
- **RN-017**: las cantidades se calculan **solo** con la `reglaCantidad` del catálogo; el catálogo es la única fuente de reglas de dimensionamiento de componentes.
- **RN-018**: `cantidadRequerida` de los módulos es siempre igual a `nModulos` (un módulo de la modalidad elegida por cada módulo de la configuración).
- **RN-019**: los componentes existentes se consideran en buen estado y con vida útil completa (supuesto simplificador declarado en la propuesta).
- **RN-020**: se puede agregar equipamiento, pero nunca reducir por debajo de lo que exige la regla.
- **RN-021**: no se modelan en este módulo IoT, automatización, monitoreo en tiempo real, control de nutrientes ni detección de enfermedades. Los sensores del catálogo son instrumentos de medición manual/básica.
- **RN-022**: `frecuenciaReposicionMeses` equivale a `vidaUtilMeses` (un solo dato, sin duplicar).

## 5. Restricciones técnicas específicas
- La función `generarComponentes(configuracion, layout, componentes, infraestructuraExistente)` debe ser pura y ordenar la salida de forma estable (por `grupo`, `categoria`, `id`).

## 6. Modelo de datos

**Componente** (catálogo, `specs/08`)
| Atributo | Tipo | Oblig. |
|---|---|---|
| `id` | texto (`BOMBA_RECIRC`) | Sí |
| `nombre` | texto | Sí |
| `categoria` | `ESTRUCTURA`/`PRODUCCION`/`HIDRAULICA`/`EQUIPAMIENTO`/`ELECTRICA` | Sí |
| `grupo` | `INFRAESTRUCTURA`/`EQUIPAMIENTO` | Sí |
| `unidad` | texto (`unidad`, `m2`, …) | Sí |
| `precioUnitario` | número ≥ 0 | Sí |
| `fuentePrecio`, `fechaActualizacionPrecio` | texto, fecha | Sí |
| `reglaCantidad`, `parametroRegla` | enum RF-012, número > 0 | Sí |
| `aplicaModalidades` | lista `NFT`/`DWC` | Sí |
| `ajustable` | booleano | Sí |
| `vidaUtilMeses` | entero > 0 | Sí |
| `mantenimientoFrecuenciaMeses` | entero > 0 | No |
| `mantenimientoCostoPorEvento` | número ≥ 0 (por unidad) | Cond. (si hay frecuencia) |
| `mantenimientoTipo` | `MANTENIMIENTO`/`CALIBRACION` | Cond. |
| `atributosModulo` | `{modalidad, largoM, anchoM, niveles, plazasBase, litrosReservaPorModulo, alturaM}` | Solo módulos |
| `atributosTanque` | `{capacidadL, diametroM, alturaM}` | Solo tanque |

**ConfiguracionComponente** (por proyecto, derivada)
| Atributo | Tipo | Oblig. | Relación |
|---|---|---|---|
| `proyectoId` | texto | Sí | → `Proyecto` |
| `componenteId` | texto | Sí | → `Componente` |
| `cantidadExtra` | entero ≥ 0 | Sí (0) | |
| `cantidadRequerida` | número | Sí | derivado |
| `cantidadExistente` | número | Sí | del proyecto |
| `cantidadAComprar` | número | Sí | derivado |
| `cobertura` | `SIN_EXISTENTE`/`PARCIAL`/`COMPLETA` | Sí | derivado |

## 7. Entradas y salidas
- **Entradas**: `ConfiguracionProductiva` (02), `Layout` (03), `Proyecto.infraestructuraExistente` (01), catálogo (08).
- **Salidas**: lista de `ConfiguracionComponente` + `requiereInfraAdicional` → 05 (costos), 06 (TCO), 07 (resumen y elementos del croquis: tanques, bombas, filtros).

## 8. Plan de tareas
| ID | Tarea | Descripción | Archivos/componentes afectados |
|---|---|---|---|
| T-04-01 | Repositorio de componentes | `obtenerComponentes()` + validación de consistencia (reglas conocidas, módulos con `atributosModulo`) | datos, repositorio |
| T-04-02 | Función de cantidades | `generarComponentes()` con las seis reglas de RF-012 | lógica de infraestructura |
| T-04-03 | Descuento de existentes | RF-013: `cantidadAComprar`, `cobertura`, `requiereInfraAdicional` | lógica de infraestructura |
| T-04-04 | Ajuste manual | RF-014 con validación | lógica y pantalla |
| T-04-05 | Pantalla 4 | Tabla agrupada por grupo/categoría con requerida/existente/a comprar y editor de extras | pantalla infraestructura |

## 9. Criterios de aceptación
Los de RF-012 a RF-014. Adicional: con el escenario demo la lista coincide exactamente con RF-012 CA-01.

## 10. Estrategia de verificación
- Normal: escenario demo NFT-8; variante DWC.
- Existentes: parcial, completa, componente que no aplica, situación A.
- Límites: `nModulos = 1` (mínimo 1 filtro/bomba/aireador); `nModulos = 6` → filtro 1 y `n = 7` → filtro 2; área que justo cumple múltiplo de 20 m².
- Inválidos: `cantidadExtra` negativa/decimal; componente no ajustable; regla desconocida en catálogo.
- Datos faltantes: modalidad sin módulo en catálogo → `DATO_INCONSISTENTE`.

## Dependencias
- **Depende de**: `01`, `02`, `03`, `08`.
- **Es utilizado por**: `05`, `06`, `07`.
