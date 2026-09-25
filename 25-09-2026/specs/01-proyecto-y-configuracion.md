# 01 — Proyecto y configuración

## 1. Objetivo

Permitir crear, editar y guardar un **Proyecto** de Brota, que actúa como contenedor de todas las decisiones del usuario (situación inicial, ubicación, presupuesto, infraestructura existente) y como dato base para los demás módulos (planificación productiva, infraestructura, estimación económica, catálogo de productos y guía/checklist).

## 2. Historias de usuario / casos de uso

- Como usuario, quiero crear un proyecto nuevo indicando su nombre y mi situación inicial, para empezar a planificar.
- Como usuario, quiero indicar si parto de cero, si tengo infraestructura parcial o si estoy planificando una nueva inversión, para que Brota adapte las preguntas siguientes.
- Como usuario, quiero indicar mi presupuesto disponible (opcional) o mi objetivo de producción (opcional), para que el motor de estimación tenga un punto de referencia.
- Como usuario, quiero guardar mi proyecto para retomarlo después y para que aparezca en el catálogo de productos y en la guía/checklist.
- Como usuario, quiero editar los datos de un proyecto ya creado.

## 3. Requisitos funcionales

**RF-001 — Crear proyecto**
- Entradas: nombre del proyecto, situación inicial, ubicación (texto libre o referencia a spec 03), superficie disponible aproximada, presupuesto disponible (opcional), objetivo de producción (opcional).
- Procesamiento: valida datos mínimos (nombre y situación inicial obligatorios) y crea la entidad `Proyecto`.
- Salida: proyecto creado con estado "en configuración".
- Prioridad: alta.
- Criterio de aceptación: ver CA-01, CA-02.

**RF-002 — Editar proyecto**
- Entradas: id de proyecto, campos a modificar.
- Procesamiento: actualiza los campos permitidos sin perder referencias a configuraciones ya definidas en otros módulos.
- Salida: proyecto actualizado.
- Prioridad: media.

**RF-003 — Listar/seleccionar proyectos guardados**
- Entradas: ninguna (o usuario actual, si el prototipo maneja usuarios).
- Procesamiento: devuelve los proyectos guardados (creados por el usuario + proyectos de demostración precargados, ver spec 10).
- Salida: lista de proyectos con nombre, situación y estado.
- Prioridad: alta (es la base para los recorridos B y C).

**RF-004 — Definir situación inicial**
- Entradas: una de las tres opciones: "desde cero", "infraestructura parcial", "nueva inversión".
- Procesamiento: si es "infraestructura parcial", habilita un formulario simple para listar qué ya posee (terreno, invernadero, tanque, racks, bombas, instalación eléctrica) como texto/checklist simple.
- Salida: situación guardada en el proyecto.
- Prioridad: alta.

## 4. Reglas de negocio

**RN-001** — Un proyecto siempre tiene una situación inicial entre las tres definidas; no puede quedar vacía.

**RN-002** — El presupuesto y el objetivo de producción son ambos opcionales, pero si ninguno se completa, el motor de estimación económica (spec 05) debe poder ejecutarse igual usando una configuración "libre" (el usuario recorre la planificación sin límite fijado, y al final ve la inversión resultante).

**RN-003** — Si se declara infraestructura existente, esos ítems se excluyen del cálculo de inversión inicial en el módulo de infraestructura (spec 04) y estimación económica (spec 05).

**RN-004** — Un proyecto guardado queda disponible para los módulos de productos (spec 07) y guía/checklist (spec 08) apenas tiene, como mínimo, cultivo y sistema hidropónico definidos (ver spec 02); antes de eso puede mostrarse como "configuración incompleta" en esos módulos.

## 5. Restricciones técnicas específicas

Ninguna integración externa es necesaria para este módulo. La ubicación puede ingresarse como texto libre en este spec; el detalle geográfico (coordenadas, orientación) se define en spec 03.

## 6. Modelo de datos

**Proyecto**
- `id`: identificador único. Obligatorio.
- `nombre`: texto. Obligatorio.
- `situacionInicial`: enum ["desde_cero", "infraestructura_parcial", "nueva_inversion"]. Obligatorio.
- `infraestructuraExistente`: lista de texto/ítems (solo si situación = infraestructura_parcial). Opcional.
- `ubicacionTexto`: texto libre (referencia rápida; detalle en `Ubicacion`, spec 03). Opcional.
- `superficieDisponible`: número (m²). Opcional.
- `presupuestoDisponible`: número. Opcional.
- `objetivoProduccion`: texto/número (ej. "kg/mes"). Opcional.
- `estado`: enum ["en_configuracion", "completo"]. Calculado.
- `fechaCreacion`, `fechaActualizacion`: fecha.

Relación: un `Proyecto` es referenciado por `ConfiguracionProductiva`, `Ubicacion`, `EstimacionEconomica`, `EstimacionProduccion`, `ProyeccionTCO`, `Carrito` y `PasoChecklist` (ver specs correspondientes).

## 7. Entradas y salidas

Entrada: formulario de creación/edición de proyecto.
Salida: objeto `Proyecto` persistido (o en memoria/mock), reutilizado por el resto de los módulos.

## 8. Plan de tareas

| ID | Tarea | Archivos/componentes afectados |
|----|-------|---------------------------------|
| T01-01 | Definir entidad `Proyecto` y almacenamiento (mock/local) | modelo de datos, capa de datos |
| T01-02 | Formulario de creación de proyecto (nombre, situación inicial) | UI creación |
| T01-03 | Formulario de infraestructura existente (condicional) | UI creación |
| T01-04 | Listado de proyectos guardados (propios + demo) | UI listado |
| T01-05 | Edición de proyecto existente | UI edición |
| T01-06 | Cálculo de `estado` del proyecto según completitud | lógica de negocio |

## 9. Criterios de aceptación

```
CA-01
El usuario puede crear un proyecto con nombre y situación inicial como datos mínimos.

CA-02
El proyecto creado queda guardado y aparece en el listado de proyectos.

CA-03
Si la situación inicial es "infraestructura parcial", el usuario puede indicar qué infraestructura ya posee.

CA-04
El proyecto guardado puede seleccionarse posteriormente desde el catálogo de productos y desde la guía/checklist.

CA-05
Editar un proyecto no borra las configuraciones ya cargadas en otros módulos (cultivo, sistema, infraestructura).
```

## 10. Estrategia de verificación

Probar manualmente: creación con datos mínimos, creación con infraestructura parcial, edición de un proyecto existente, intento de guardar sin nombre (debe bloquear), selección del proyecto desde otros módulos.

## 11. Dependencias

```
Depende de:
Ninguna (módulo base).

Es utilizado por:
02 Planificación productiva
03 Ubicación y factibilidad
04 Infraestructura y equipamiento
05 Estimación económica
06 Producción, retorno y TCO
07 Productos y carrito
08 Guía y checklist
09 Propuesta visual
```
