# 04 — Infraestructura y equipamiento

## 1. Objetivo

Determinar, a partir del cultivo, sistema hidropónico y escala elegidos (spec 02), qué componentes de infraestructura y equipamiento necesita el proyecto, descontando lo que el usuario ya declaró tener (spec 01).

## 2. Historias de usuario / casos de uso

- Como usuario, quiero ver qué infraestructura necesito para mi configuración (estructura, racks, tanques, cañerías, filtros), para entender el alcance de mi proyecto.
- Como usuario, quiero ver qué equipamiento necesito (bombas, aireación, medidor de pH, sensores básicos), para saber qué comprar.
- Como usuario que ya tiene infraestructura parcial, quiero que Brota descuente lo que ya tengo del listado de necesidades.

## 3. Requisitos funcionales

**RF-012 — Calcular lista de componentes necesarios**
- Entradas: `ConfiguracionProductiva` (cultivo, sistema, escala) y `infraestructuraExistente` del proyecto (spec 01).
- Procesamiento: toma `componentesRequeridos` del sistema hidropónico elegido (dato mock, spec 10), multiplica cantidades según la escala (ej. cantidad de módulos) y resta lo ya declarado como existente.
- Salida: lista de `ConfiguracionComponente` (componente + cantidad necesaria) para el proyecto.
- Prioridad: alta.

**RF-013 — Mostrar detalle de cada componente**
- Entradas: componente seleccionado de la lista.
- Procesamiento: muestra nombre, categoría, unidad, vida útil y mantenimiento asociado (dato mock).
- Salida: detalle visible en UI.
- Prioridad: media.

## 4. Reglas de negocio

**RN-012** — La cantidad necesaria de cada componente se calcula como `cantidadBase(componente) × factorEscala(cantidadModulos)`, con `factorEscala` definido por componente (algunos escalan 1:1 con módulos —racks—, otros no —ej. un solo medidor de pH puede alcanzar para todo el proyecto—). El detalle de estos factores se define como dato mock en spec 10.

**RN-013** — Si el usuario declaró un componente como infraestructura existente (spec 01) con cantidad suficiente, ese componente se marca como "ya contemplado" y no se suma a la inversión inicial (spec 05).

**RN-014** — No se incluyen en este módulo componentes de IoT, automatización o monitoreo en tiempo real (fuera de alcance del prototipo).

## 5. Restricciones técnicas específicas

Todos los componentes y sus reglas de asociación a sistemas hidropónicos son datos mock (spec 10); no requieren cálculo estructural real.

## 6. Modelo de datos

**Componente**
- `id`, `nombre`, `categoria` (estructura/hidráulico/producción/equipamiento), `unidad`, `costoUnitario`, `vidaUtil` (meses), `frecuenciaMantenimiento`, `costoMantenimiento`.

**ConfiguracionComponente**
- `id`, `proyectoId`, `componenteId`, `cantidadNecesaria`, `origen` (enum: "requerido" / "ya_contemplado"), `costoTotal` (derivado, ver spec 05).

## 7. Entradas y salidas

Entrada: `ConfiguracionProductiva` + infraestructura existente del proyecto.
Salida: lista de `ConfiguracionComponente`, insumo directo de spec 05 (estimación económica), spec 06 (TCO) y spec 07 (productos necesarios por proyecto).

## 8. Plan de tareas

| ID | Tarea | Archivos/componentes afectados |
|----|-------|---------------------------------|
| T04-01 | Lógica de cálculo de componentes necesarios según sistema y escala | lógica de negocio |
| T04-02 | Descuento de infraestructura existente | lógica de negocio |
| T04-03 | Pantalla de listado de infraestructura y equipamiento necesarios | UI infraestructura |
| T04-04 | Detalle de componente (vida útil, mantenimiento) | UI infraestructura |
| T04-05 | Persistencia de `ConfiguracionComponente` | capa de datos |

## 9. Criterios de aceptación

```
CA-14
Al definir cultivo, sistema y escala, Brota muestra automáticamente una lista de infraestructura y equipamiento necesarios.

CA-15
Si el usuario declaró infraestructura existente equivalente, esos componentes aparecen marcados como "ya contemplados" y no se duplican en el listado de necesidades.

CA-16
Cada componente muestra su vida útil y mantenimiento asociado.
```

## 10. Estrategia de verificación

Probar: proyecto desde cero (todos los componentes son necesarios), proyecto con infraestructura parcial (algunos componentes descontados), cambio de escala (la cantidad de componentes se recalcula).

## 11. Dependencias

```
Depende de:
01 Proyecto y configuración
02 Planificación productiva

Es utilizado por:
05 Estimación económica
06 Producción, retorno y TCO
07 Productos y carrito
```
