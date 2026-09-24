# SPEC 08 — Datos iniciales y mocks

Soporte transversal. Define los datos mínimos para ejecutar el flujo completo **sin servicios externos**, y la forma de los mocks para poder reemplazarlos luego por integraciones reales.

> **Todos los valores de este documento son datos de demostración** (`AVISO_DATOS_DEMO`). No son cotizaciones, mediciones ni recomendaciones técnicas validadas. Los criterios de ubicación están marcados `VALOR_DEMO_NO_VALIDADO` y deben ser revisados por el equipo (ver DECISIONES PENDIENTES en `project.md`).

## 1. Objetivo
Proveer catálogos semilla (parámetros, cultivos, componentes, localidades), interfaces de servicios simulados y escenarios de demostración con resultados esperados, de modo que ninguna pantalla dependa de información no disponible.

## 3. Requisitos funcionales

### RF-025 — Catálogos semilla y repositorios de datos
- **Descripción**: cargar y exponer los datos de referencia mediante repositorios reemplazables.
- **Entradas**: archivos/tablas semilla con el contenido de las secciones 6.1 a 6.4.
- **Procesamiento**: `obtenerParametros()`, `obtenerCultivos()`, `obtenerComponentes()`, `obtenerCriteriosUbicacion()`; validar consistencia al cargar (IDs únicos, referencias existentes, rangos, reglas de cantidad conocidas, módulos con `atributosModulo`).
- **Salida**: datos validados o error `DATO_INCONSISTENTE` indicando el ID y campo.
- **Reglas relacionadas**: RN-041, RN-042.
- **Prioridad**: Alta.
- **Criterios de aceptación**:
  - CA-01: la aplicación arranca y completa el flujo demo usando solo estos datos, sin red.
  - CA-02: cambiar un precio en la semilla modifica la inversión sin tocar código de cálculo.
  - CA-03: un componente con `precioUnitario` ausente o negativo hace fallar la validación con `DATO_INCONSISTENTE`.
  - CA-04: cada catálogo se consume solo a través de su repositorio.

### RF-026 — Servicios externos simulados
- **Descripción**: interfaces de servicio con implementación mock.
- **Entradas**: llamadas a `ServicioCriteriosUbicacion`, `ServicioGeografico`, `ServicioPrecios`, `ServicioAR`.
- **Procesamiento**: cada método devuelve el **sobre común** (sección 7) con `origen = "MOCK"`; puede simular latencia y un modo de fallo configurable para pruebas.
- **Salida**: respuestas con la misma estructura que tendría una API real.
- **Reglas relacionadas**: RN-042, RN-043.
- **Prioridad**: Alta.
- **Criterios de aceptación**:
  - CA-01: reemplazar un mock por otra implementación no requiere cambios en los specs 02–07.
  - CA-02: con el modo de fallo activo, el flujo continúa con datos semilla y muestra una advertencia.
  - CA-03: toda respuesta mock incluye `origen`, `fechaActualizacion`, `datos` y `advertencias`.

### RF-027 — Escenarios de demostración
- **Descripción**: cargar un proyecto de ejemplo completo con resultados esperados.
- **Entradas**: acción "Cargar proyecto de demostración".
- **Procesamiento**: crear un proyecto con los datos de la sección 6.6 (`DEMO-APOSTOLES` por defecto).
- **Salida**: proyecto con pasos 1–2 cargados y listo para recorrer el flujo.
- **Reglas relacionadas**: RN-044.
- **Prioridad**: Alta.
- **Criterios de aceptación**:
  - CA-01: al recorrer el flujo con `DEMO-APOSTOLES` los resultados coinciden con la tabla de resultados esperados (tolerancia: ±1 en importes por redondeo, ±1 mes en recuperación).
  - CA-02: los escenarios de borde (6.6) producen los estados indicados.

## 4. Reglas de negocio
- **RN-041**: todo dato de este documento debe poder reemplazarse sin modificar la lógica de cálculo.
- **RN-042**: los datos semilla incluyen siempre `fuente` y fecha de referencia cuando corresponda; en este prototipo la fuente es `DEMO`.
- **RN-043**: ningún mock puede ser requisito para completar el flujo: ante falla se usan los datos semilla.
- **RN-044**: los resultados esperados de los escenarios son **valores de referencia para verificación**, no promesas de resultado real.

## 5. Restricciones técnicas específicas
- Formato de datos serializable (JSON o equivalente), versionado junto al repositorio.
- Los importes de la semilla están en la moneda de `parametros.moneda` (propuesta `ARS`) sin símbolo ni separadores.

## 6. Modelo de datos (datos semilla)

### 6.1 Parámetros globales (`ParametrosGlobales`)
| Parámetro | Valor | Unidad | Uso |
|---|---|---|---|
| `moneda` | `ARS` | — | presentación |
| `fechaReferenciaPrecios` | `2026-09-01` | fecha | avisos |
| `retiroPerimetralM` | 1,5 | m | 03 |
| `filasPorInvernadero` | 2 | — | 03 |
| `pasilloLateralM` | 0,6 | m | 03, 07 |
| `pasilloCentralM` | 1,2 | m | 03, 07 |
| `zonaTecnicaM` | 2,4 | m | 03, 07 |
| `alturaLateralM` | 2,5 | m | 07 |
| `alturaCumbreraM` | 3,5 | m | 07 |
| `diasOperativosAnio` | 330 | días | 02, 06 |
| `diasLimpiezaEntreCiclos` | 3 | días | 02, 06 |
| `manoDeObraInstalacionPct` | 0,15 | fracción | 05 |
| `imprevistosPct` | 0,10 | fracción | 05 |
| `porcentajeVentaEfectiva` | 0,90 | fracción | 06 |
| `horasBaseSemana` | 3,0 | h/semana | 06 |
| `horasPor100PlazasSemana` | 0,6 | h/semana | 06 |
| `valorHoraManoObra` | 3.000 | moneda/h | 06 |
| `semanasPorMes` | 4,33 | — | 06 |
| `costoFijoMensualBase` | 60.000 | moneda/mes | 06 (energía, agua, embalaje, otros) |
| `costoFijoMensualPorModulo` | 15.000 | moneda/mes | 06 |
| `horizonteMeses` | 60 | meses | 06 |

### 6.2 Cultivos (`Cultivo`)
| id | nombre | modalidades | cicloDias | pesoCosechaKg | mortalidadPct | factorDensidad | precioReferenciaKg | costoInsumosKg |
|---|---|---|---:|---:|---:|---:|---:|---:|
| `LECHUGA` | Lechuga | NFT, DWC | 45 | 0,18 | 0,08 | 1,0 | 6.500 | 1.200 |
| `ALBAHACA` | Albahaca | NFT | 50 | 0,12 | 0,10 | 0,8 | 14.000 | 1.800 |
| `RUCULA` | Rúcula | NFT, DWC | 35 | 0,06 | 0,10 | 1,5 | 10.000 | 1.500 |

Campos informativos (no usados en cálculos, a validar): pH 5,8–6,5 / temperatura 15–24 °C (lechuga); pH 5,5–6,5 / 18–28 °C (albahaca); pH 6,0–7,0 / 12–22 °C (rúcula).

Producción de referencia por módulo (RF-005): lechuga NFT 288 plazas → 27,32 kg/mes; lechuga DWC 72 → 6,83; albahaca NFT 230 → 12,89; rúcula NFT 432 → 16,88; rúcula DWC 108 → 4,22.

### 6.3 Componentes (`Componente`)
Campos comunes: `fuentePrecio = DEMO`, `fechaActualizacionPrecio = 2026-09-01`.

| id | nombre | categoría | grupo | unidad | precioUnitario | reglaCantidad | parámetro | aplica | ajustable | vidaUtilMeses | mantFrecMeses | mantCostoEvento | tipo mant. |
|---|---|---|---|---|---:|---|---:|---|:-:|---:|---:|---:|---|
| `ESTR_INV` | Estructura metálica del invernadero | ESTRUCTURA | INFRAESTRUCTURA | m2 | 45.000 | POR_M2_INVERNADERO | 1 | NFT, DWC | no | 120 | 12 | 500 | MANTENIMIENTO |
| `CUBIERTA` | Cubierta de polietileno (techo y laterales) | ESTRUCTURA | INFRAESTRUCTURA | m2 | 6.000 | POR_M2_INVERNADERO | 2 | NFT, DWC | no | 36 | — | — | — |
| `VENTILADOR` | Ventilador/extractor | EQUIPAMIENTO | EQUIPAMIENTO | unidad | 65.000 | CADA_N_M2_INVERNADERO | 20 | NFT, DWC | sí | 60 | — | — | — |
| `MOD_NFT` | Módulo NFT de 3 niveles | PRODUCCION | INFRAESTRUCTURA | unidad | 600.000 | POR_MODULO | 1 | NFT | no | 96 | 6 | 5.000 | MANTENIMIENTO |
| `MOD_DWC` | Módulo DWC (piscina flotante) | PRODUCCION | INFRAESTRUCTURA | unidad | 380.000 | POR_MODULO | 1 | DWC | no | 96 | 6 | 4.000 | MANTENIMIENTO |
| `TANQUE_500` | Tanque de reserva 500 L | HIDRAULICA | INFRAESTRUCTURA | unidad | 150.000 | POR_CAPACIDAD_TANQUE | 500 | NFT, DWC | no | 120 | — | — | — |
| `KIT_CANIA` | Kit de cañerías y conexiones por módulo | HIDRAULICA | INFRAESTRUCTURA | unidad | 55.000 | POR_MODULO | 1 | NFT, DWC | no | 60 | — | — | — |
| `FILTRO` | Filtro de agua | HIDRAULICA | INFRAESTRUCTURA | unidad | 25.000 | CADA_N_MODULOS | 6 | NFT, DWC | sí | 6 | — | — | — |
| `BOMBA_RECIRC` | Bomba de recirculación | EQUIPAMIENTO | EQUIPAMIENTO | unidad | 85.000 | CADA_N_MODULOS | 6 | NFT | sí | 24 | 6 | 3.000 | MANTENIMIENTO |
| `AIREADOR` | Bomba de aireación/oxigenación | EQUIPAMIENTO | EQUIPAMIENTO | unidad | 40.000 | CADA_N_MODULOS | 4 | NFT, DWC | sí | 24 | — | — | — |
| `SONDA_PH` | Sonda de pH | EQUIPAMIENTO | EQUIPAMIENTO | unidad | 45.000 | FIJA | 1 | NFT, DWC | sí | 12 | 1 | 3.000 | CALIBRACION |
| `MEDIDOR_EC` | Medidor de conductividad (EC) | EQUIPAMIENTO | EQUIPAMIENTO | unidad | 110.000 | FIJA | 1 | NFT, DWC | sí | 48 | 3 | 4.000 | CALIBRACION |
| `SENSOR_TEMP` | Sensor de temperatura | EQUIPAMIENTO | EQUIPAMIENTO | unidad | 30.000 | FIJA | 1 | NFT, DWC | sí | 36 | — | — | — |
| `KIT_ELEC` | Kit de instalación eléctrica básica | ELECTRICA | EQUIPAMIENTO | unidad | 150.000 | FIJA | 1 | NFT, DWC | no | 120 | — | — | — |

`atributosModulo`:
| id | modalidad | largoM | anchoM | niveles | plazasBase | litrosReservaPorModulo | alturaM |
|---|---|---:|---:|---:|---:|---:|---:|
| `MOD_NFT` | NFT | 3,0 | 1,2 | 3 | 288 | 150 | 1,9 |
| `MOD_DWC` | DWC | 2,4 | 1,2 | 1 | 72 | 250 | 0,9 |

`atributosTanque` de `TANQUE_500`: `capacidadL = 500`, `diametroM = 1,0`, `alturaM = 1,2`.

### 6.4 Criterios de ubicación (`CriterioUbicacion`)
Todos con `fuente = "Referencia técnica (INTA) — a verificar por el equipo"` y `estadoValidacion = VALOR_DEMO_NO_VALIDADO`.

| localidadId | nombre | provincia | latitud | longitud | riesgoHeladas | riesgoCalorExtremo | ejeLargoRecomendado | climaResumen | justificacionEje (demo) |
|---|---|---|---:|---:|---|---|---|---|---|
| `APOSTOLES` | Apóstoles | Misiones | −27,91 | −55,76 | MEDIO | ALTO | `EO` | Subtropical húmedo; veranos calurosos, inviernos suaves con heladas ocasionales | Orientar el eje largo E–O favorece la captación de radiación en invierno (criterio de demostración) |
| `POSADAS` | Posadas | Misiones | −27,37 | −55,90 | BAJO | ALTO | `EO` | Subtropical húmedo | Ídem Apóstoles (criterio de demostración) |
| `MENDOZA` | Mendoza | Mendoza | −32,89 | −68,84 | MEDIO | MEDIO | `NS` | Templado árido, alta radiación, amplitud térmica | Criterio de demostración distinto para evidenciar que la orientación depende de la localidad |

> Estos ejes **no son una afirmación técnica**: sirven para demostrar la lógica (RN-013). El equipo debe reemplazarlos por criterios INTA verificados.

### 6.5 Estructura de los mocks de servicios
Todos devuelven este **sobre común**:
```json
{
  "origen": "MOCK",
  "fechaActualizacion": "2026-09-01",
  "datos": { },
  "advertencias": []
}
```

| Servicio | Método | Parámetros | `datos` |
|---|---|---|---|
| `ServicioCriteriosUbicacion` | `obtenerCriterios(localidadId)` | `localidadId` | un `CriterioUbicacion` (6.4) |
| `ServicioGeografico` | `consultarLocalidad(localidadId)` | `localidadId` | `{latitud, longitud, elevacionM, zonaClimatica}` (elevación y zona: valores fijos de demostración; sin uso en cálculos) |
| `ServicioPrecios` | `obtenerCatalogo()` | — | lista de `{componenteId, precioUnitario, fuente, fechaActualizacion}` (idéntica a 6.3) |
| `ServicioFactibilidad` | `evaluar(entrada)` | `Proyecto`, `CondicionesTerreno`, criterio, config (opcional) | `EvaluacionFactibilidad` (implementación **local por reglas** del spec 03; misma firma si se reemplaza) |
| `ServicioAR` | `estaDisponible()` / `iniciar(modeloCroquis)` | `ModeloCroquis` | `{disponible: false, mensaje: "La realidad aumentada es una demostración; esta vista no está habilitada en este dispositivo."}` |

Ejemplo `ServicioCriteriosUbicacion.obtenerCriterios("APOSTOLES")`:
```json
{
  "origen": "MOCK",
  "fechaActualizacion": "2026-09-01",
  "datos": {
    "localidadId": "APOSTOLES", "nombre": "Apóstoles", "provincia": "Misiones",
    "latitud": -27.91, "longitud": -55.76,
    "riesgoHeladas": "MEDIO", "riesgoCalorExtremo": "ALTO",
    "ejeLargoRecomendado": "EO",
    "estadoValidacion": "VALOR_DEMO_NO_VALIDADO"
  },
  "advertencias": ["Criterio de demostración pendiente de validación"]
}
```
Modo de fallo de prueba: cualquier método puede configurarse para devolver `{ "origen": "ERROR", "datos": null, "advertencias": ["Servicio no disponible"] }`; el consumidor debe usar la semilla (RN-043).

### 6.6 Escenarios de demostración

**DEMO-APOSTOLES (escenario principal)**
| Entrada | Valor |
|---|---|
| nombre | Invernadero Apóstoles (demo) |
| situación | `A_DESDE_CERO` |
| presupuestoDisponible | 14.000.000 |
| localidad / terreno | `APOSTOLES` / 20 m × 15 m |
| condiciones | `ejeLargoTerreno = EO`, pendiente 1 %, inundación `BAJO`, agua `SI`, electricidad `RED` |
| producción | `LECHUGA`, `NFT`, modo `PRESUPUESTO` |
| `fechaInicioEstimada` | 2026-10-01 |

Resultados esperados:
| Resultado | Valor de referencia |
|---|---|
| Factibilidad | `VERDE`; `capacidad NFT = 8`; eje `EO` |
| `nModulos` | 8 (`limitadoPor = TERRENO`) |
| Layout | 14,4 m × 4,8 m = 69,12 m²; `modulosPorFila = [4, 4]` |
| Materiales | 10.524.840 (infraestructura 9.679.840; equipamiento 845.000) |
| Líneas | `ESTR_INV` 3.110.400 · `CUBIERTA` 829.440 · `VENTILADOR` ×4 260.000 · `MOD_NFT` ×8 4.800.000 · `TANQUE_500` ×3 450.000 · `KIT_CANIA` ×8 440.000 · `FILTRO` ×2 50.000 · `BOMBA_RECIRC` ×2 170.000 · `AIREADOR` ×2 80.000 · `SONDA_PH` 45.000 · `MEDIDOR_EC` 110.000 · `SENSOR_TEMP` 30.000 · `KIT_ELEC` 150.000 |
| Mano de obra de instalación / imprevistos | 1.578.726 / ≈ 1.210.357 |
| Inversión inicial | ≈ 13.313.923 (`DENTRO_DE_PRESUPUESTO`, saldo ≈ 686.077) |
| Producción | 2.304 plazas; ≈ 2.623,1 kg/año; ≈ 218,6 kg/mes; ≈ 50,4 kg/semana; ≈ 91,1 kg/m²·año |
| Ingreso bruto mensual | ≈ 1.278.763 |
| Costos operativos mensuales | insumos ≈ 262.310 + mano de obra ≈ 218.544 + fijos 180.000 = ≈ 660.854 |
| Flujo neto operativo mensual | ≈ 617.909 |
| Recuperación | ≈ 26 meses (≈ 2,2 años); `mesesArranque = 2` |
| TCO a 60 meses | reposiciones 2.894.440 + mantenimientos 632.800 + calibraciones 260.000 + inversión ≈ 13.313.923 = ≈ 17.101.163 |
| Primera fecha en la matriz | `SONDA_PH`: 2026-11-01 (calibración mensual) |

**Escenarios de borde**
| ID | Cambio respecto de `DEMO-APOSTOLES` | Resultado esperado |
|---|---|---|
| `DEMO-OBJETIVO` | modo `OBJETIVO`, 150 kg/mes, sin presupuesto | `nRequerido = 6`, layout 11,4 × 4,8 m (54,72 m²), inversión ≈ 10.207.336, recuperación ≈ 28 meses |
| `DEMO-4-MODULOS` | presupuesto 8.000.000 | `nModulos = 4`, `limitadoPor = PRESUPUESTO`, inversión ≈ 7.461.274, recuperación ≈ 35 meses |
| `DEMO-PRESUP-INSUF` | presupuesto 3.000.000 | `PRESUPUESTO_INSUFICIENTE`, `nModulos = 1` (referencia), mínimo ≈ 3.665.262 |
| `DEMO-OBJ-EXCEDE` | modo `OBJETIVO`, 400 kg/mes | `nRequerido = 15`, `nModulos = 8`, `SUPERFICIE_INSUFICIENTE` |
| `DEMO-TERRENO-NS` | `ejeLargoTerreno = NS` | capacidad NFT = 6 (con `INFO`: el eje alterno admitiría 8) |
| `DEMO-CHICO` | terreno 5 × 5 m | `ROJO` (FAC-01), avance bloqueado |
| `DEMO-DWC` | modalidad `DWC` | recuperación `null` (no se recupera en 60 meses) |
| `DEMO-B` | situación `B_INFRA_PARCIAL` con `TANQUE_500` ×2 y `BOMBA_RECIRC` ×1 existentes | `TANQUE_500`: comprar 1 (`PARCIAL`); `BOMBA_RECIRC`: comprar 1 (`PARCIAL`); `requiereInfraAdicional = true` |

## 7. Entradas y salidas
- **Entradas**: ninguna del usuario (salvo activar el escenario demo o el modo de fallo de pruebas).
- **Salidas**: catálogos y respuestas de servicios consumidos por los specs 01–07.

## 8. Plan de tareas
| ID | Tarea | Descripción | Archivos/componentes afectados |
|---|---|---|---|
| T-08-01 | Archivos semilla | Crear parámetros, cultivos, componentes (con `atributosModulo`/`atributosTanque`) y criterios según 6.1–6.4 | datos |
| T-08-02 | Repositorios y validación | `obtener*()` con validación de consistencia (RF-025) | repositorios |
| T-08-03 | Servicios mock | Interfaces y mocks con sobre común y modo de fallo (RF-026) | servicios |
| T-08-04 | Escenarios | Cargar `DEMO-APOSTOLES` y escenarios de borde como datos de prueba (RF-027) | datos, pruebas |
| T-08-05 | Prueba de humo del flujo | Ejecutar el flujo completo con `DEMO-APOSTOLES` y comparar con los resultados esperados | pruebas |

## 9. Criterios de aceptación
Los de RF-025 a RF-027. Adicional: la prueba de humo de T-08-05 pasa con las tolerancias definidas.

## 10. Estrategia de verificación
- Carga: validar cada catálogo (IDs duplicados, referencias inexistentes, precio negativo, regla desconocida).
- Reemplazo: cambiar un precio en la semilla y comprobar el cambio en la inversión.
- Servicios: respuesta normal y modo de fallo.
- Flujo: escenario principal y escenarios de borde con los valores de 6.6.

## Dependencias
- **Depende de**: ninguno (es la base de datos de referencia).
- **Es utilizado por**: `01` (localidades, componentes), `02` (cultivos, módulos, parámetros), `03` (criterios), `04` (componentes), `05`, `06` (parámetros), `07` (parámetros, AR).
