# 09 — Propuesta visual

## 1. Objetivo

Generar una representación conceptual (resumen + croquis) de la configuración final del proyecto, integrando los resultados de los módulos anteriores en una única pantalla de propuesta.

## 2. Historias de usuario / casos de uso

- Como usuario, quiero ver un resumen final de mi proyecto (cultivo, sistema, superficie, inversión, producción, recuperación, TCO) en una sola pantalla.
- Como usuario, quiero ver una vista superior conceptual de cómo se distribuiría mi instalación.
- Como usuario, quiero ver una vista lateral/frontal conceptual de la estructura.
- Como usuario, quiero ver un botón de "ver en AR" aunque hoy sea solo una demostración o placeholder.

## 3. Requisitos funcionales

**RF-030 — Generar resumen de propuesta**
- Entradas: `Proyecto`, `ConfiguracionProductiva`, `Ubicacion`, `EstimacionEconomica`, `EstimacionProduccion`, `ProyeccionTCO`.
- Procesamiento: consolida los datos de todos los módulos en una sola estructura de resumen.
- Salida: resumen de propuesta listo para mostrar.
- Prioridad: alta.

**RF-031 — Generar croquis conceptual (vista superior)**
- Entradas: superficie, orientación (spec 03), cantidad de módulos y tanques (spec 02/04).
- Procesamiento: dibuja una distribución simplificada (grilla o esquema básico) ubicando límites del invernadero, accesos, racks/módulos, tanques y circulación aproximada.
- Salida: representación gráfica simple (ej. SVG o diagrama) — no un plano de obra.
- Prioridad: media.

**RF-032 — Generar croquis conceptual (vista lateral/frontal)**
- Entradas: altura aproximada de referencia (dato mock por tipo de estructura), racks, tanque.
- Procesamiento: dibuja una representación simplificada de la estructura y sus componentes principales.
- Salida: representación gráfica simple.
- Prioridad: baja.

**RF-033 — Mostrar acceso a AR (placeholder)**
- Entradas: ninguna real.
- Procesamiento: muestra un botón "Ver en AR" que abre una demostración controlada o un mensaje de funcionalidad futura.
- Salida: interacción visual, sin dependencia funcional del resto de la propuesta.
- Prioridad: baja.

## 4. Reglas de negocio

**RN-031** — El croquis es conceptual: no debe presentarse como plano arquitectónico, cálculo estructural ni documentación apta para obra (heredado de project.md/prompt maestro).

**RN-032** — Si algún módulo previo no está completo (por ejemplo, no se definió ubicación), la propuesta debe mostrar esa sección como "pendiente de completar" en vez de fallar o mostrar datos inventados.

**RN-033** — El botón de AR nunca debe ser un requisito para completar o entender la propuesta; el sistema debe funcionar íntegramente sin usarlo.

## 5. Restricciones técnicas específicas

El croquis puede implementarse con formas geométricas simples (rectángulos/íconos) representando racks, tanques y accesos; no requiere motor 3D. La demo de AR, si se implementa, es una demostración controlada y no depende de sensores reales para el resto del sistema.

## 6. Modelo de datos

No se define una entidad nueva de persistencia obligatoria: la propuesta se compone en tiempo de lectura a partir de las entidades ya definidas en specs 01 a 06. Si el equipo decide persistir la propuesta generada, puede agregarse una entidad `PropuestaVisual` con `proyectoId` y una referencia/snapshot de los datos consolidados (ver Decisiones pendientes).

## 7. Entradas y salidas

Entrada: datos consolidados de specs 01, 02, 03, 04, 05 y 06.
Salida: pantalla de propuesta con resumen, croquis (superior y lateral) y acceso opcional a AR.

## 8. Plan de tareas

| ID | Tarea | Archivos/componentes afectados |
|----|-------|---------------------------------|
| T09-01 | Consolidación de datos de todos los módulos en un resumen único | lógica de negocio |
| T09-02 | Pantalla de resumen de propuesta | UI propuesta |
| T09-03 | Generación de croquis vista superior (simplificado) | UI/gráficos |
| T09-04 | Generación de croquis vista lateral/frontal (simplificado) | UI/gráficos |
| T09-05 | Botón/placeholder de "Ver en AR" | UI propuesta |

## 9. Criterios de aceptación

```
CA-34
La propuesta muestra en una sola pantalla: cultivo, sistema, superficie, escala, inversión, producción, recuperación estimada y TCO.

CA-35
La propuesta incluye una vista superior conceptual con racks/módulos, tanques y accesos aproximados.

CA-36
Si falta información de un módulo anterior, la propuesta lo indica como pendiente en vez de romperse o inventar datos.

CA-37
El botón "Ver en AR" es opcional y el resto de la propuesta funciona sin necesidad de usarlo.
```

## 10. Estrategia de verificación

Probar: proyecto con todos los módulos completos (propuesta completa), proyecto con módulos incompletos (secciones marcadas como pendientes), distintas escalas (el croquis refleja la cantidad de módulos).

## 11. Dependencias

```
Depende de:
01 Proyecto y configuración
02 Planificación productiva
03 Ubicación y factibilidad
04 Infraestructura y equipamiento
05 Estimación económica
06 Producción, retorno y TCO

Es utilizado por:
Ninguno (módulo final del Recorrido A)
```
