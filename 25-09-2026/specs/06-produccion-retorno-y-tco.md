# 06 — Producción, retorno y TCO

## 1. Objetivo

Estimar la producción esperada del proyecto, el ingreso potencial y el tiempo aproximado de recuperación de la inversión, además de representar el costo total de propiedad (TCO) a lo largo del tiempo (mantenimiento, calibración, reposición).

## 2. Historias de usuario / casos de uso

- Como usuario, quiero saber cuánto podría producir aproximadamente por mes, según mi cultivo y escala.
- Como usuario, quiero saber cuánto podría ganar aproximadamente por mes, y en cuánto tiempo recuperaría mi inversión inicial.
- Como usuario, quiero ver qué costos de mantenimiento y reposición tendrá mi infraestructura a lo largo del tiempo, para no considerar solo el costo inicial.

## 3. Requisitos funcionales

**RF-017 — Estimar producción**
- Entradas: cultivo, escala (`ConfiguracionProductiva`, spec 02), datos de rendimiento del cultivo (spec 10).
- Procesamiento: `produccionEstimada = rendimientoPorModulo(cultivo) × cantidadModulos × frecuenciaCosecha`.
- Salida: producción estimada por período (ej. kg/mes) y frecuencia de cosecha aproximada.
- Prioridad: alta.

**RF-018 — Estimar ingresos y recuperación**
- Entradas: producción estimada, precio de referencia del cultivo (mock, spec 10), costos recurrentes estimados (mantenimiento mensual aproximado, spec 04), inversión inicial (spec 05).
- Procesamiento (fórmulas, ver sección 4).
- Salida: ingreso bruto estimado, costos estimados, tiempo aproximado de recuperación.
- Prioridad: alta.

**RF-019 — Generar proyección de TCO**
- Entradas: lista de `ConfiguracionComponente` con su `vidaUtil`, `frecuenciaMantenimiento` y `costoMantenimiento` (spec 04).
- Procesamiento: arma una matriz temporal simple (componente, vida útil, próxima reposición, costo).
- Salida: `ProyeccionTCO` con eventos de mantenimiento/reposición proyectados.
- Prioridad: media.

## 4. Reglas de negocio

**RN-019** — Fórmulas:

```
ingresoBrutoEstimado = produccionEstimada × precioReferencia

recuperacionEstimada (en meses) =
    inversionInicial / (ingresoBrutoEstimado - costosEstimadosMensuales)
```

Si el denominador es menor o igual a 0 (los costos igualan o superan el ingreso), Brota no debe calcular un número de meses negativo: debe mostrar un mensaje indicando que, con los supuestos actuales, el proyecto no recupera la inversión, y sugerir revisar escala o cultivo.

**RN-020** — Los eventos de `ProyeccionTCO` se calculan proyectando, para cada componente, las reposiciones esperadas según su `vidaUtil` dentro de un horizonte fijo de referencia (ej. 5 o 10 años, a definir por el equipo — ver Decisiones pendientes).

**RN-021** — Todo resultado de este módulo se presenta con la leyenda de estimación (contract.md, sección 4), incluyendo explícitamente la frase de tipo: *"Con los supuestos utilizados, la inversión inicial se recuperaría aproximadamente en X meses/años."*

## 5. Restricciones técnicas específicas

Ninguna integración externa. Precios de referencia y rendimientos son datos mock (spec 10).

## 6. Modelo de datos

**EstimacionProduccion**
- `id`, `proyectoId`, `produccionEstimada`, `unidad`, `frecuenciaCosecha`.

Extensión de **EstimacionEconomica** (o entidad relacionada) para retorno:
- `ingresoBrutoEstimado`, `costosEstimadosMensuales`, `recuperacionEstimadaMeses` (o `null`/flag si no se recupera dentro del horizonte).

**ProyeccionTCO**
- `id`, `proyectoId`, `eventos`: lista de `{componenteId, fechaEstimada/ano, costo, tipo: "reposición"/"calibración"/"mantenimiento"}`.

**Mantenimiento**
- `id`, `componenteId`, `frecuencia`, `costo`, `tipo`.

## 7. Entradas y salidas

Entrada: `ConfiguracionProductiva`, `ConfiguracionComponente`, `EstimacionEconomica`.
Salida: `EstimacionProduccion`, retorno estimado, `ProyeccionTCO` — todo insumo de spec 09 (propuesta visual).

## 8. Plan de tareas

| ID | Tarea | Archivos/componentes afectados |
|----|-------|---------------------------------|
| T06-01 | Cálculo de producción estimada | lógica de negocio |
| T06-02 | Cálculo de ingreso, costos y recuperación | lógica de negocio |
| T06-03 | Generación de matriz/proyección de TCO | lógica de negocio |
| T06-04 | Pantalla de resultados de producción y retorno | UI retorno |
| T06-05 | Pantalla/tabla de TCO y mantenimiento | UI TCO |

## 9. Criterios de aceptación

```
CA-21
Brota muestra una producción estimada (cantidad y frecuencia) a partir del cultivo y la escala.

CA-22
Brota muestra ingreso bruto estimado, costos estimados y tiempo aproximado de recuperación de la inversión.

CA-23
Si el proyecto no recupera la inversión bajo los supuestos actuales, Brota lo indica claramente en vez de mostrar un tiempo negativo o absurdo.

CA-24
Brota muestra una proyección de TCO con al menos los componentes de mayor relevancia (bomba, filtro, sonda de pH).
```

## 10. Estrategia de verificación

Probar: escala pequeña vs. grande (impacto en producción e ingresos), costos que superan ingresos (verificar mensaje sin recuperación), componentes con distinta vida útil (verificar que la proyección de TCO los distribuye correctamente en el tiempo.

## 11. Dependencias

```
Depende de:
02 Planificación productiva
04 Infraestructura y equipamiento
05 Estimación económica

Es utilizado por:
09 Propuesta visual
```
