# 02 — Planificación productiva

## 1. Objetivo

Permitir que el usuario seleccione, dentro de su proyecto, el **cultivo**, el **sistema hidropónico**, la **escala** y la **cantidad de módulos/racks**, con explicaciones pensadas para un usuario sin experiencia previa.

## 2. Historias de usuario / casos de uso

- Como usuario principiante, quiero ver los cultivos disponibles con una explicación breve, para elegir uno sin conocer términos técnicos de antemano.
- Como usuario principiante, quiero ver los sistemas hidropónicos disponibles con una explicación breve de cómo funcionan, para elegir el que más se ajuste a mi situación.
- Como usuario, quiero definir la escala de mi proyecto (cantidad de módulos/racks o superficie productiva), para que Brota calcule infraestructura, inversión y producción en base a eso.
- Como usuario, quiero que Brota me avise si el cultivo y el sistema elegidos no son compatibles, para no configurar algo inviable.

## 3. Requisitos funcionales

**RF-005 — Seleccionar cultivo**
- Entradas: proyecto activo, cultivo elegido de una lista precargada (spec 10).
- Procesamiento: guarda el cultivo en la configuración productiva del proyecto; muestra su descripción breve.
- Salida: cultivo asociado al proyecto.
- Prioridad: alta.

**RF-006 — Seleccionar sistema hidropónico**
- Entradas: proyecto activo, sistema elegido (NFT, DWC, goteo, u otros de spec 10).
- Procesamiento: guarda el sistema; valida compatibilidad con el cultivo elegido (RN-005).
- Salida: sistema asociado al proyecto; alerta si hay incompatibilidad.
- Prioridad: alta.

**RF-007 — Definir escala**
- Entradas: cantidad de módulos/racks o superficie productiva deseada.
- Procesamiento: si el usuario definió un objetivo de producción en spec 01, sugiere una escala aproximada (puede ser una regla simple, no un optimizador); si definió presupuesto, la escala queda sujeta a lo que arroje la estimación económica (spec 05).
- Salida: escala guardada (`cantidadModulos`, `superficieProductiva`).
- Prioridad: alta.

## 4. Reglas de negocio

**RN-005** — Cada cultivo tiene una lista de sistemas hidropónicos compatibles (dato mock, spec 10). Si el usuario elige una combinación no compatible, Brota debe mostrar una advertencia clara (no bloquear necesariamente, salvo que el equipo decida bloquear — ver Decisiones pendientes).

**RN-006** — La escala mínima es 1 módulo/rack. No se permite continuar sin al menos un valor de escala definido.

**RN-007** — Un cultivo y un sistema siempre deben mostrarse con su explicación breve (RN heredada de la restricción de usabilidad general del proyecto); nunca solo con el nombre o sigla.

## 5. Restricciones técnicas específicas

Los datos de cultivos y sistemas son mock/hardcodeados (spec 10); no requieren fuente externa.

## 6. Modelo de datos

**Cultivo**
- `id`, `nombre`, `descripcionBreve`, `cicloDias` (aprox.), `rendimientoEstimado`, `sistemasCompatibles` (lista de ids de `SistemaHidroponico`).

**SistemaHidroponico**
- `id`, `nombre`, `descripcionBreve`, `componentesRequeridos` (lista de ids de `Componente`, ver spec 04), `cultivosCompatibles`.

**ConfiguracionProductiva**
- `id`, `proyectoId` (relación con `Proyecto`), `cultivoId`, `sistemaId`, `cantidadModulos`, `superficieProductiva`, `escalaCalculada` (derivado).

## 7. Entradas y salidas

Entrada: selección de cultivo, sistema y escala.
Salida: `ConfiguracionProductiva` asociada al proyecto, insumo directo de spec 04 (infraestructura) y spec 06 (producción).

## 8. Plan de tareas

| ID | Tarea | Archivos/componentes afectados |
|----|-------|---------------------------------|
| T02-01 | Pantalla de selección de cultivo con tarjetas explicativas | UI selección cultivo |
| T02-02 | Pantalla de selección de sistema hidropónico con tarjetas explicativas | UI selección sistema |
| T02-03 | Validación de compatibilidad cultivo/sistema | lógica de negocio |
| T02-04 | Formulario de escala (módulos/superficie) | UI escala |
| T02-05 | Persistencia de `ConfiguracionProductiva` | capa de datos |

## 9. Criterios de aceptación

```
CA-06
El usuario puede elegir un cultivo entre varios, cada uno con una descripción breve.

CA-07
El usuario puede elegir un sistema hidropónico entre varios, cada uno con una descripción breve.

CA-08
Si elige una combinación cultivo/sistema no compatible, Brota lo advierte.

CA-09
El usuario puede definir la escala de su proyecto (módulos o superficie) y ese valor queda guardado.
```

## 10. Estrategia de verificación

Probar: selección de cultivo y sistema compatibles, selección de combinación incompatible (verificar advertencia), escala en 0 o vacía (debe bloquear avance), escala válida guardada correctamente.

## 11. Dependencias

```
Depende de:
01 Proyecto y configuración

Es utilizado por:
04 Infraestructura y equipamiento
05 Estimación económica
06 Producción, retorno y TCO
08 Guía y checklist
```
