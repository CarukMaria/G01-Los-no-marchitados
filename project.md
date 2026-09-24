# project.md — HidroPlan (nombre provisional)

## 3.1 Nombre provisional
**HidroPlan** — asistente de planificación inicial, factibilidad y estimación económica para emprendimientos hidropónicos.

## 3.2 Problema
Quien quiere iniciar o ampliar una producción hidropónica debe decidir a la vez cuestiones **técnicas, espaciales y económicas**: qué superficie necesita, qué sistema usar, cuántos módulos entran, qué infraestructura y equipamiento comprar, cómo orientar y distribuir la instalación, cuánto invertir, qué componentes hay que reponer, cuánto producirá, cuánto podría vender y en cuánto tiempo recuperaría la inversión. Hoy esa información está dispersa, y conocer solo el costo de construir un invernadero no alcanza para decidir.

## 3.3 Objetivo
Permitir que una persona, a partir de su terreno, su presupuesto o su objetivo productivo, obtenga una **propuesta inicial integral y comprensible** que relacione espacio, producción, infraestructura, inversión y costos futuros, con un croquis conceptual de la distribución. Es una primera aproximación **antes** de invertir.

## 3.4 Usuarios principales
- **Productor inicial**: no tiene infraestructura y quiere saber qué necesita para comenzar.
- **Productor con infraestructura parcial / que planifica una nueva inversión**: ya tiene parte de los componentes (o ya sabe qué quiere producir) y necesita saber qué agregar o comparar configuraciones.

No se definen otros actores para el prototipo (no hay proveedores ni profesionales operando el sistema).

## 3.5 Desafíos abordados
- **Desafío #1 (principal) — ¿Cómo iniciar?**: planificación inicial, factibilidad preliminar del espacio y estimación de inversión y retorno.
- **Aspectos incorporados del Desafío #3**: visión de costos a lo largo del tiempo (TCO): mantenimiento, calibración y reposición de componentes con vida útil limitada.
- **Aspectos incorporados del Desafío #6**: visualización del diseño (croquis conceptual y punto de integración para realidad aumentada) y uso de criterios técnicos de referencia (INTA) para orientación y emplazamiento.

> Los alcances exactos de #3 y #6 dentro del prototipo son los descritos en este documento y en los specs; no se extienden más allá.

## 3.6 Alcance del prototipo (lo que funciona)
Flujo completo, ejecutable **sin servicios externos**, con datos precargados:

1. Crear/editar/duplicar un proyecto (situación inicial, presupuesto, ubicación, superficie, infraestructura existente).
2. Cargar condiciones del terreno y obtener una **evaluación preliminar de factibilidad** (semáforo) con criterios de referencia precargados.
3. Elegir cultivo, modalidad y dimensionar por **presupuesto** (flujo principal) o por **objetivo productivo** (variante simplificada) con el mismo motor.
4. Obtener la lista de infraestructura y equipamiento requeridos, descontando lo existente.
5. Calcular la **inversión inicial** y compararla con el presupuesto.
6. Estimar **producción**, **ingresos** y **tiempo aproximado de recuperación** bajo supuestos explícitos.
7. Ver el **TCO** simplificado (mantenimiento, calibración, reposición) y la matriz de mantenimiento.
8. Ver la **propuesta visual**: resumen, croquis superior, vista frontal y acciones demostrativas (AR / consultar especialista).

## 3.7 Alcance del sistema completo (referencia, NO implementar)
Integración con fuentes oficiales (INTA, clima, mapas, topografía, riesgo hídrico), catálogo de proveedores con precios actualizables y publicación patrocinada, servicios de consultoría profesional, medición con cámara y AR avanzada, y en el largo plazo monitoreo y automatización.

## 3.8 Fuera del prototipo
IoT, control automático, monitoreo en tiempo real, detección avanzada de enfermedades, gestión completa de nutrientes, gestión de stock, trazabilidad, comercialización avanzada, análisis de mercado, automatización de invernaderos, integración completa de mapas, análisis profesional de suelo, cálculo estructural, ingeniería constructiva, AR avanzada, catálogo de proveedores real y contratación de consultoría (módulos M09–M14).

## 3.9 Flujo principal (demo ante el jurado)
Escenario guía: *"Tengo un terreno en Apóstoles, quiero producir lechuga y dispongo de aproximadamente $14.000.000."*

Pantalla 1 Proyecto → Pantalla 2 Ubicación y terreno (+ factibilidad) → Pantalla 3 Cultivo, modalidad y dimensionamiento → Pantalla 4 Infraestructura y equipamiento → Pantalla 5 Inversión, producción, retorno y TCO → Pantalla 6 Propuesta visual (croquis, vista frontal) → acciones "Ver en AR (demostrativo)" y "Consultar a un especialista (demostrativo)".

Cierre esperado: *"Con los supuestos utilizados, la inversión inicial se recuperaría aproximadamente en X años."*

> El orden de pantallas (1→6) es el orden de uso. El orden de implementación sigue la numeración de specs 01→07 (ver dependencias en cada spec).

## 3.10 Supuestos y limitaciones
- Todos los cálculos de producción, ingresos, rentabilidad y amortización son **estimaciones orientativas** basadas en los datos ingresados y en datos de referencia.
- Los precios, rendimientos y costos son **datos precargados de demostración**, no cotizaciones ni datos validados.
- Las APIs externas (geográficas, climáticas, INTA, precios, AR) están **simuladas**.
- La evaluación de factibilidad **no es** una certificación profesional, estudio de suelo ni ingeniería.
- El período de amortización es una **proyección basada en supuestos**, no una garantía de rentabilidad.
- El croquis es **conceptual**: no sirve para obra ni reemplaza planos profesionales.
- El sistema no reemplaza el asesoramiento profesional.

## Mapa de documentos
| Archivo | Contenido |
|---|---|
| `contract.md` | Reglas técnicas transversales |
| `specs/01-…` a `07-…` | Comportamiento funcional del flujo, en orden de implementación |
| `specs/08-datos-iniciales-y-mocks.md` | Datos semilla y mocks (soporte transversal) |

---

## DECISIONES PENDIENTES
Solo decisiones que el equipo debe tomar **antes de implementar**. No se tomaron automáticamente; donde hay una propuesta de trabajo en los specs, se indica.

1. **Stack tecnológico y persistencia** (frontend, backend o no, base de datos o almacenamiento local, forma de renderizar el croquis). Los specs son agnósticos; se necesita elegir después de revisar `contract.md`. *Sin esto no se puede comenzar la implementación.*
2. **Validación de los criterios INTA por localidad** (eje largo recomendado, justificación y fuente citable). En `08` los valores están marcados `VALOR_DEMO_NO_VALIDADO`; el equipo con formación forestal/agropecuaria debe confirmarlos o reemplazarlos con referencias reales.
3. **Validación de los datos de demostración agronómicos y económicos** (rendimiento por planta, ciclos, mortalidad, plazas por módulo, precios de venta, costos de componentes, valor hora de mano de obra, costos fijos) y **fecha/moneda de referencia** (propuesta: ARS, `2026-09-01`). Afecta directamente el resultado de recuperación mostrado al jurado.
4. **Cultivos, modalidades y localidades incluidos en el prototipo** (propuesta: lechuga, albahaca, rúcula; NFT y DWC; Apóstoles, Posadas, Mendoza). Confirmar o ajustar.
5. **Modalidad principal de planificación** (propuesta: *presupuesto → producción* como flujo principal y *objetivo → inversión* como variante simplificada sobre el mismo motor). Confirmar.
6. **Nivel de AR en la demo** (propuesta: botón demostrativo con mensaje/experiencia mínima y punto de integración). Decidir si se implementa algo más que eso.
