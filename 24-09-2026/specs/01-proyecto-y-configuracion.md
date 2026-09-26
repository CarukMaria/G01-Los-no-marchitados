# SPEC 01 — Proyecto y configuración

## 1. Objetivo
Permitir crear, editar, guardar, recuperar y duplicar un proyecto hidropónico con los datos base (nombre, situación inicial, presupuesto, ubicación y superficie, infraestructura existente y objetivo general) que alimentan a los módulos posteriores.

## 2. Historias de usuario / casos de uso
- **HU-01** — Como productor, quiero crear y configurar mi proyecto indicando mi situación (desde cero, con infraestructura parcial o nueva inversión), mi presupuesto, dónde está mi terreno y qué tamaño tiene, para poder continuar con la planificación.

## 3. Requisitos funcionales

### RF-001 — Crear proyecto
- **Descripción**: alta de un proyecto con los datos base.
- **Entradas**: `nombre`, `situacionInicial`, `presupuestoDisponible` (opcional en este paso), `localidadId`, `largoM`, `anchoM`, `objetivoGeneral` (opcional), `fechaInicioEstimada` (opcional).
- **Procesamiento**: validar (RN-001 a RN-003); calcular `superficieM2 = largoM × anchoM`; asignar `id` y fechas; guardar; dejar el flujo en paso 1 completo.
- **Salida**: `Proyecto` persistido y disponible para los pasos siguientes; o lista de errores `{codigo, mensaje, campo}`.
- **Reglas relacionadas**: RN-001, RN-002, RN-003.
- **Prioridad**: Alta.
- **Criterios de aceptación**:
  - CA-01: con los datos mínimos válidos se crea el proyecto y se avanza al paso 2.
  - CA-02: los campos obligatorios vacíos o inválidos impiden guardar y muestran error junto al campo.
  - CA-03: `superficieM2` se muestra calculada y no es editable.
  - CA-04: `localidadId` solo admite localidades del catálogo (`specs/08`).
  - CA-05: los datos guardados son los que usan luego los cálculos (no se vuelven a pedir).

### RF-002 — Situación inicial e infraestructura existente
- **Descripción**: registrar qué tiene el usuario según su situación.
- **Entradas**: `situacionInicial` ∈ {`A_DESDE_CERO`, `B_INFRA_PARCIAL`, `C_NUEVA_INVERSION`}; si es B, lista `infraestructuraExistente` = [{`componenteId`, `cantidad`}] elegida del catálogo de componentes.
- **Procesamiento**: A → lista vacía. B → validar RN-004. C → lista vacía por defecto y se habilita `duplicarProyecto` (RF-003) para comparar configuraciones; el modo de planificación sugerido en la pantalla 3 es *objetivo* (editable).
- **Salida**: `Proyecto.infraestructuraExistente` actualizado. Los módulos 04 y 05 lo usan para calcular `cantidadAComprar`.
- **Reglas relacionadas**: RN-004.
- **Prioridad**: Alta.
- **Criterios de aceptación**:
  - CA-01: con situación A no se pide infraestructura existente.
  - CA-02: con situación B se puede agregar/quitar componentes existentes con su cantidad (entero ≥ 1).
  - CA-03: con situación B y lista vacía se bloquea el avance con el error `CAMPO_OBLIGATORIO`.
  - CA-04: cambiar de B a A vacía la lista tras confirmación.
  - CA-05: con situación C solo aparece la acción "Duplicar proyecto" como diferencia respecto de A.

### RF-003 — Editar, listar, recuperar y duplicar proyecto
- **Descripción**: mantener proyectos guardados y controlar la vigencia de resultados.
- **Entradas**: `proyectoId`; cambios sobre campos del proyecto; acción duplicar.
- **Procesamiento**: editar sobreescribe campos y actualiza `fechaModificacion`; si cambia un dato que afecta pasos posteriores (localidad, dimensiones, situación, infraestructura existente, presupuesto), marcar los pasos 2–6 como `DESACTUALIZADO` (RN-005). Duplicar crea un nuevo `Proyecto` con `id` nuevo, nombre `"<nombre> (copia)"` y copia de todas las entradas (no de resultados, que se recalculan).
- **Salida**: proyecto actualizado o duplicado; lista de proyectos con nombre, localidad, fecha de modificación.
- **Reglas relacionadas**: RN-001 a RN-005.
- **Prioridad**: Alta (crear/recuperar/editar), Media (duplicar).
- **Criterios de aceptación**:
  - CA-01: al reabrir un proyecto se recuperan exactamente los datos ingresados.
  - CA-02: editar `largoM` marca los pasos posteriores como desactualizados y exige recalcular.
  - CA-03: duplicar genera un proyecto independiente; editar la copia no altera el original.
  - CA-04: la lista muestra todos los proyectos guardados ordenados por `fechaModificacion` descendente.

## 4. Reglas de negocio
- **RN-001**: `nombre` obligatorio, 1–80 caracteres.
- **RN-002**: `largoM` y `anchoM` obligatorios, números > 0 y ≤ 1000; `superficieM2 = largoM × anchoM` (terreno rectangular simplificado). `presupuestoDisponible`, si se informa, es > 0; no es obligatorio al crear el proyecto (lo exige el spec 02 solo en el modo *presupuesto*).
- **RN-003**: `localidadId` obligatorio y existente en el catálogo de criterios de ubicación.
- **RN-004**: en situación `B_INFRA_PARCIAL`, `infraestructuraExistente` debe tener ≥ 1 ítem; cada `cantidad` es entero ≥ 1; no repetir `componenteId`.
- **RN-005**: cada paso tiene estado `PENDIENTE | COMPLETO | DESACTUALIZADO`. Un cambio en una entrada aguas arriba deja `DESACTUALIZADO` a los pasos que dependen de ella; no se muestran resultados de pasos desactualizados como vigentes.

## 5. Restricciones técnicas específicas
- El proyecto debe guardarse y recuperarse sin conexión (ver `contract.md` 4.9).
- La lista de componentes para infraestructura existente proviene del catálogo (`specs/08`), no se escribe en la pantalla.

## 6. Modelo de datos

**Proyecto**
| Atributo | Tipo | Oblig. | Notas |
|---|---|---|---|
| `id` | texto `PRY-…` | Sí | único |
| `nombre` | texto ≤ 80 | Sí | |
| `situacionInicial` | enum A/B/C | Sí | |
| `presupuestoDisponible` | número ARS | No* | *obligatorio para modo presupuesto |
| `ubicacion` | objeto `{localidadId, largoM, anchoM, superficieM2}` | Sí | `localidadId` → `CriterioUbicacion`; `superficieM2` derivada |
| `infraestructuraExistente` | lista `{componenteId, cantidad}` | Cond. (B) | `componenteId` → `Componente` |
| `objetivoGeneral` | texto ≤ 280 | No | descriptivo, no se usa en cálculos |
| `fechaInicioEstimada` | fecha | No | por defecto fecha de creación; se usa para "próxima fecha" de mantenimiento (spec 06) |
| `estadoPasos` | mapa paso→estado | Sí | RN-005 |
| `fechaCreacion`, `fechaModificacion` | fecha | Sí | |

Relaciones: 1 `Proyecto` → 0..1 `CondicionesTerreno` (03), 0..1 `ConfiguracionProductiva` (02), y resultados derivados (04–06).

## 7. Entradas y salidas
- **Entrada**: formulario de proyecto (pantalla 1) y editor de infraestructura existente.
- **Salida**: `Proyecto` persistido; listado de proyectos. Consumidores: 02 (presupuesto, situación), 03 (ubicación, dimensiones), 04–05 (infraestructura existente), 06 (`fechaInicioEstimada`).

## 8. Plan de tareas
| ID | Tarea | Descripción | Archivos/componentes afectados |
|---|---|---|---|
| T-01-01 | Modelo y validación | Definir `Proyecto` y `validarProyecto()` con RN-001 a RN-004 devolviendo errores `{codigo, mensaje, campo}` | modelo de datos, módulo de validación |
| T-01-02 | Repositorio de proyectos | Interfaz + implementación local: guardar, obtener, listar, duplicar | capa de persistencia |
| T-01-03 | Estado de pasos | Implementar `estadoPasos` y la regla de invalidación RN-005 | lógica de proyecto |
| T-01-04 | Pantalla 1 | Formulario (nombre, situación, presupuesto, localidad, largo, ancho, objetivo, fecha) con superficie calculada | pantalla proyecto |
| T-01-05 | Editor de infraestructura existente | Selector de componentes del catálogo + cantidad (solo situación B) | pantalla proyecto |
| T-01-06 | Lista de proyectos | Listar, abrir, duplicar | pantalla inicio |

## 9. Criterios de aceptación
Los de RF-001 a RF-003. Adicional: crear un proyecto con los datos del escenario `DEMO-APOSTOLES` (`specs/08`) debe dejar el paso 1 `COMPLETO`.

## 10. Estrategia de verificación
- Normal: crear el proyecto demo; reabrirlo; duplicarlo.
- Inválidos: nombre vacío; nombre de 81 caracteres; largo `0`, negativo, texto; localidad inexistente; presupuesto `0` o negativo.
- Límites: largo/ancho `1000` (válido) y `1000.01` (inválido).
- Datos faltantes: situación B sin componentes; situación B con cantidad `0`.
- Estado: editar el largo y verificar que los pasos 2–6 quedan desactualizados.

## Dependencias
- **Depende de**: `08` (catálogo de localidades y de componentes).
- **Es utilizado por**: `02`, `03`, `04`, `05`, `06`, `07`.
