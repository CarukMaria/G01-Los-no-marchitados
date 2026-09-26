# SPEC 05 — Estimación económica

## 1. Objetivo
Calcular la **inversión inicial estimada** de la configuración (materiales, mano de obra de instalación e imprevistos), descontando la infraestructura existente, y compararla contra el presupuesto disponible. Es el motor de costos usado por todos los modos de planificación.

## 2. Historias de usuario / casos de uso
- **HU-06** — Como productor, quiero ver cuánto costaría instalar mi configuración, línea por línea, y si entra en mi presupuesto o qué debería ajustar.

## 3. Requisitos funcionales

### RF-015 — Cálculo de la inversión inicial
- **Descripción**: calcular costos por línea y total.
- **Entradas**: lista de `ConfiguracionComponente` (spec 04) con `cantidadAComprar`; `precioUnitario` de cada `Componente`; `manoDeObraInstalacionPct` e `imprevistosPct` de `ParametrosGlobales`.
- **Procesamiento**:
  ```
  costoLinea             = cantidadAComprar × precioUnitario
  costoMateriales        = Σ costoLinea                                  (todas las líneas)
  costoInfraestructura   = Σ costoLinea de líneas con grupo = INFRAESTRUCTURA
  costoEquipamiento      = Σ costoLinea de líneas con grupo = EQUIPAMIENTO
  manoDeObraInstalacion  = costoMateriales × manoDeObraInstalacionPct
  imprevistos            = (costoMateriales + manoDeObraInstalacion) × imprevistosPct
  inversionInicial       = costoMateriales + manoDeObraInstalacion + imprevistos
  ```
  Función pública: `calcularInversion(nModulos, contexto)` → ejecuta `calcularLayout` (03) → `generarComponentes` (04) → fórmulas anteriores. Sin `cantidadExtra` cuando se usa para dimensionar (RN-024).
- **Salida**: `EstimacionEconomica` con líneas, subtotales, `inversionInicial` y `fechaReferenciaPrecios`.
- **Reglas relacionadas**: RN-023, RN-024, RN-025.
- **Prioridad**: Alta.
- **Criterios de aceptación**:
  - CA-01: escenario demo (NFT, 8 módulos, sin existentes) → `costoMateriales = 10.524.840`, `manoDeObraInstalacion = 1.578.726`, `imprevistos ≈ 1.210.357`, `inversionInicial ≈ 13.313.923` (tolerancia ±1 por redondeo).
  - CA-02: con `TANQUE_500` existente ×3, el costo de esa línea es 0 y `inversionInicial` disminuye en `450.000 × 1,15 × 1,10`.
  - CA-03: el costo total coincide con la suma de las cantidades y precios mostrados (verificable a mano).
  - CA-04: `inversionInicial(n)` es no decreciente en `n`.
  - CA-05: no se realiza ninguna llamada externa; los precios provienen del catálogo semilla.

### RF-016 — Comparación con el presupuesto
- **Descripción**: indicar si la configuración entra en el presupuesto y qué ajustar.
- **Entradas**: `inversionInicial`, `presupuestoDisponible` (opcional en modo objetivo), `nMaxTerreno`, `requiereInfraAdicional` (04), `estadoDimensionamiento` (02).
- **Procesamiento**:
  ```
  saldoPresupuesto   = presupuestoDisponible − inversionInicial
  usoPresupuestoPct  = inversionInicial / presupuestoDisponible
  estadoPresupuesto  = SIN_PRESUPUESTO           si no hay presupuesto
                       DENTRO_DE_PRESUPUESTO     si inversionInicial ≤ presupuestoDisponible
                       SUPERA_PRESUPUESTO        en otro caso
  Si SUPERA_PRESUPUESTO:
     nMaxPorPresupuesto = max { n ∈ [1 .. nMaxTerreno] : inversionInicial(n) ≤ presupuestoDisponible }, o 0 si no existe
     sugerencia = REDUCIR_ESCALA (a nMaxPorPresupuesto módulos)   si nMaxPorPresupuesto ≥ 1
                  AUMENTAR_PRESUPUESTO (mínimo = inversionInicial(1)) si nMaxPorPresupuesto = 0
  ```
- **Salida**: `estadoPresupuesto`, `saldoPresupuesto`, `usoPresupuestoPct`, `sugerencia`, `nMaxPorPresupuesto`, y las marcas `requiereInfraAdicional` y `estadoDimensionamiento` para mostrarlas junto al resultado.
- **Reglas relacionadas**: RN-026.
- **Prioridad**: Alta.
- **Criterios de aceptación**:
  - CA-01: demo con presupuesto 14.000.000 → `DENTRO_DE_PRESUPUESTO`, saldo ≈ 686.077.
  - CA-02: objetivo 150 kg/mes (6 módulos, inversión ≈ 10.207.336) con presupuesto 8.000.000 → `SUPERA_PRESUPUESTO`, `nMaxPorPresupuesto = 4`, sugerencia `REDUCIR_ESCALA`.
  - CA-03: presupuesto 3.000.000 → `SUPERA_PRESUPUESTO` con `AUMENTAR_PRESUPUESTO` y mínimo ≈ 3.665.262.
  - CA-04: sin presupuesto → `SIN_PRESUPUESTO`, sin error.
  - CA-05: la pantalla muestra con claridad los cuatro casos: dentro del presupuesto, supera el presupuesto, requiere reducir escala, requiere infraestructura adicional.

## 4. Reglas de negocio
- **RN-023**: el costo total refleja siempre cantidades y precios del catálogo (RN-017 del spec 04 y precios semilla); no existen costos "planos" escritos en la lógica.
- **RN-024**: el dimensionamiento por presupuesto (spec 02, RF-006) calcula con `cantidadExtra = 0`; el resultado final (RF-015 sobre la configuración vigente) incluye los extras del usuario, por lo que puede superar el presupuesto y se informa en RF-016.
- **RN-025**: `manoDeObraInstalacion` cubre instalación y montaje (no se separan "instalación" y "mano de obra"); se calcula como porcentaje del costo de materiales que se compran.
- **RN-026**: toda comparación con el presupuesto se expresa con `estadoPresupuesto`; el sistema nunca oculta que una configuración supera el presupuesto.
- **RN-027**: los importes se muestran con `AVISO_ESTIMACION` y `AVISO_DATOS_DEMO`, con la fecha de referencia de los precios.
- **RN-028**: no se estiman costos de terreno, permisos, transporte ni tributos (fuera de alcance; se declara en la pantalla).

## 5. Restricciones técnicas específicas
- Las fórmulas viven en un único módulo de cálculo puro; la presentación solo formatea.
- **Clasificación de datos**:

| Dato | Tipo |
|---|---|
| `presupuestoDisponible`, `nModulos` (si se ajusta), `infraestructuraExistente`, `cantidadExtra` | **Ingresado** |
| `areaInvernaderoM2`, `cantidadRequerida`, `cantidadAComprar`, `costoLinea`, `costoMateriales`, `manoDeObraInstalacion`, `imprevistos`, `inversionInicial`, `saldoPresupuesto` | **Derivado** |
| `precioUnitario`, `reglaCantidad`, `manoDeObraInstalacionPct`, `imprevistosPct`, `fechaReferenciaPrecios` | **Hardcodeado / semilla** (`specs/08`, reemplazable) |
| Origen de precios (`fuentePrecio = DEMO`), futura actualización de proveedores | **Simulado** (mock; extensión futura) |

## 6. Modelo de datos

**EstimacionEconomica** (derivada; se recalcula)
| Atributo | Tipo | Oblig. |
|---|---|---|
| `proyectoId` | texto | Sí |
| `lineas` | lista de `{componenteId, cantidadAComprar, precioUnitario, costoLinea}` | Sí |
| `costoInfraestructura`, `costoEquipamiento`, `costoMateriales` | número | Sí |
| `manoDeObraInstalacion`, `imprevistos`, `inversionInicial` | número | Sí |
| `fechaReferenciaPrecios` | fecha | Sí |
| `estadoPresupuesto` | enum RF-016 | Sí |
| `saldoPresupuesto`, `usoPresupuestoPct` | número | Cond. (con presupuesto) |
| `sugerencia`, `nMaxPorPresupuesto` | enum, entero | Cond. |

## 7. Entradas y salidas
- **Entradas**: salida del spec 04, `ParametrosGlobales`, `Proyecto.presupuestoDisponible`.
- **Salidas**: `EstimacionEconomica` → 02 (`calcularInversion`), 06 (`inversionInicial`), 07 (resumen).

## 8. Plan de tareas
| ID | Tarea | Descripción | Archivos/componentes afectados |
|---|---|---|---|
| T-05-01 | Función de costos | `calcularCostos(lineas, parametros)` con las fórmulas de RF-015 | lógica económica |
| T-05-02 | Función `calcularInversion(n)` | Orquesta layout → componentes → costos para cualquier `n` | lógica económica |
| T-05-03 | Comparación con presupuesto | RF-016 con búsqueda de `nMaxPorPresupuesto` | lógica económica |
| T-05-04 | Pantalla 5 (parte inversión) | Tabla por línea, subtotales, total, estado del presupuesto, avisos | pantalla resultados |

## 9. Criterios de aceptación
Los de RF-015 y RF-016. Adicional: la suma de las líneas del escenario demo coincide con `10.524.840` (ver tabla en `specs/08`).

## 10. Estrategia de verificación
- Normal: escenario demo (valores de RF-015 CA-01).
- Existentes: tanque existente completo; existente parcial; existente que no aplica.
- Presupuesto: dentro, exacto (`inversión = presupuesto` → dentro), supera con reducción posible, presupuesto insuficiente, sin presupuesto.
- Límites: `n = 1`; `n = nMaxTerreno`; monotonía de `inversionInicial(n)` para n = 1…8.
- Datos faltantes: componente sin precio → `DATO_INCONSISTENTE`; porcentajes fuera de [0, 1].
- Comprobación manual: recalcular el total del demo con calculadora usando la tabla de `specs/08`.

## Dependencias
- **Depende de**: `01`, `02`, `03`, `04`, `08`.
- **Es utilizado por**: `02` (RF-006 usa `calcularInversion`), `06`, `07`.
