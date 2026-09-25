# 05 — Estimación económica

## 1. Objetivo

Calcular la inversión inicial estimada del proyecto a partir de los componentes necesarios (spec 04), y compararla con el presupuesto disponible o con el objetivo de producción definidos en spec 01.

## 2. Historias de usuario / casos de uso

- Como usuario, quiero ver cuánto costaría aproximadamente iniciar mi proyecto, sumando estructura, equipamiento, instalación y mano de obra.
- Como usuario que definió un presupuesto, quiero saber si mi configuración entra dentro de ese presupuesto o si lo supera.
- Como usuario que definió un objetivo de producción, quiero saber cuánto necesitaría invertir aproximadamente para alcanzarlo.

## 3. Requisitos funcionales

**RF-014 — Calcular inversión inicial**
- Entradas: lista de `ConfiguracionComponente` con `origen = "requerido"` (spec 04), costo de instalación (dato mock, % o monto fijo) y costo de mano de obra (dato mock).
- Procesamiento (fórmulas, ver sección 4).
- Salida: `EstimacionEconomica` con el desglose de inversión inicial.
- Prioridad: alta.

**RF-015 — Comparar con presupuesto**
- Entradas: `presupuestoDisponible` del proyecto (spec 01), `inversionInicial` calculada.
- Procesamiento: compara ambos valores.
- Salida: estado ("dentro de presupuesto" / "supera el presupuesto") + diferencia. Si supera, sugiere reducir escala (referencia a spec 02).
- Prioridad: alta.

**RF-016 — Estimar inversión requerida para un objetivo de producción**
- Entradas: `objetivoProduccion` del proyecto, datos de rendimiento por módulo del cultivo (spec 10).
- Procesamiento: estima la escala necesaria (cantidad de módulos aproximada) para alcanzar el objetivo, y a partir de esa escala reutiliza el cálculo de RF-014.
- Salida: inversión estimada requerida + escala sugerida.
- Prioridad: media (puede implementarse de forma simplificada, según sección "Modalidades" del prompt maestro).

## 4. Reglas de negocio

**RN-015** — Fórmulas de cálculo:

```
costoComponente = cantidadNecesaria × costoUnitario

inversionInicial =
    Σ costoComponente (de todos los ConfiguracionComponente con origen = "requerido")
    + costoInstalacion
    + costoManoDeObra
```

**RN-016** — `costoInstalacion` y `costoManoDeObra` son valores de referencia mock (pueden expresarse como % de la suma de componentes o como monto fijo por escala; a definir en spec 10).

**RN-017** — Los precios de referencia de Internet (spec 07) **no** forman parte de este cálculo; solo se usan como contexto informativo en el catálogo de productos.

**RN-018** — Todo resultado de este módulo debe presentarse como estimación (ver contract.md, sección 4).

## 5. Restricciones técnicas específicas

Ninguna. Cálculo puramente aritmético sobre datos ya cargados en el proyecto.

## 6. Modelo de datos

**EstimacionEconomica**
- `id`, `proyectoId`, `inversionInicial`, `desgloseComponentes` (lista costoComponente por componente), `costoInstalacion`, `costoManoDeObra`, `presupuestoDisponible` (referencia), `estadoPresupuesto` (enum: "dentro" / "supera" / "sin_definir"), `diferenciaPresupuesto`.

## 7. Entradas y salidas

Entrada: `ConfiguracionComponente` (spec 04) + datos del proyecto (spec 01).
Salida: `EstimacionEconomica`, insumo de spec 06 (retorno) y spec 09 (propuesta visual).

## 8. Plan de tareas

| ID | Tarea | Archivos/componentes afectados |
|----|-------|---------------------------------|
| T05-01 | Implementar fórmula de inversión inicial | lógica de negocio |
| T05-02 | Comparación contra presupuesto disponible | lógica de negocio |
| T05-03 | Estimación simplificada de inversión requerida por objetivo de producción | lógica de negocio |
| T05-04 | Pantalla de resultado de inversión con desglose por componente | UI estimación económica |
| T05-05 | Persistencia de `EstimacionEconomica` | capa de datos |

## 9. Criterios de aceptación

```
CA-17
Brota calcula y muestra la inversión inicial estimada, desglosada por componente, instalación y mano de obra.

CA-18
Si el proyecto tiene presupuesto definido, Brota indica claramente si la configuración entra o supera ese presupuesto.

CA-19
Si el proyecto tiene un objetivo de producción definido, Brota muestra una inversión estimada aproximada para alcanzarlo.

CA-20
Todos los resultados económicos incluyen una aclaración de que son estimaciones.
```

## 10. Estrategia de verificación

Probar: proyecto con presupuesto suficiente, proyecto con presupuesto insuficiente, proyecto sin presupuesto ni objetivo (modo libre), proyecto con objetivo de producción definido.

## 11. Dependencias

```
Depende de:
01 Proyecto y configuración
04 Infraestructura y equipamiento

Es utilizado por:
06 Producción, retorno y TCO
09 Propuesta visual
```
