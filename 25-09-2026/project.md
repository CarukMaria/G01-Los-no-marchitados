# project.md — Brota

## 1. Nombre

**Brota**

Prototipo de planificación y acompañamiento para emprendimientos hidropónicos.

## 2. Problema

Una persona que quiere iniciar o ampliar un emprendimiento hidropónico debe tomar, al mismo tiempo, decisiones técnicas, espaciales y económicas sin tener un punto de partida claro. En general no sabe:

- qué infraestructura necesita;
- qué sistema hidropónico conviene usar;
- cuánto espacio requiere;
- cuánto podría costarle empezar;
- qué componentes debe adquirir;
- qué mantenimiento tendrá esa infraestructura a lo largo del tiempo;
- qué producción podría esperar razonablemente;
- cómo empezar el proceso, paso a paso.

Hoy esta información está dispersa (proveedores, INTA, experiencia de terceros, prueba y error), lo que dificulta especialmente a un productor inicial evaluar si un proyecto es viable antes de invertir.

## 3. Objetivo

Brota busca acompañar al usuario en:

- una evaluación inicial de su proyecto (espacio, ubicación, cultivo, sistema);
- la planificación de la infraestructura necesaria;
- una estimación económica orientativa (inversión, producción, ingresos, recuperación, TCO);
- la identificación de productos/componentes necesarios;
- el aprendizaje de conceptos básicos de hidroponía;
- un seguimiento inicial mediante checklist adaptado a su proyecto.

Brota **no** reemplaza el asesoramiento profesional ni garantiza resultados económicos. Todo resultado se presenta como una estimación orientativa basada en los datos ingresados y en información de referencia.

## 4. Usuarios principales

- Productor inicial que nunca armó un sistema hidropónico.
- Persona que evalúa iniciar un emprendimiento hidropónico.
- Productor que ya tiene infraestructura parcial y quiere ampliarla, reorganizarla o evaluar una nueva inversión.

## 5. Desafíos abordados

- **Desafío #1 — ¿Cómo iniciar?**: desafío principal. Brota da una propuesta inicial de producción a partir de ubicación, espacio, cultivo, sistema, presupuesto/objetivo.
- **Desafío #3**: incorporado parcialmente a través de la sección de productos/carrito y la identificación de componentes necesarios por proyecto.
- **Desafío #6**: incorporado parcialmente a través de la guía de hidroponía y el checklist adaptado, que acompañan al productor en la implementación.

## 6. Alcance del prototipo

El prototipo demuestra **tres recorridos principales**:

1. **Evaluación y planificación** de un proyecto hidropónico (factibilidad, infraestructura, inversión, producción, retorno, TCO, propuesta visual).
2. **Productos y carrito**: catálogo mock de productos/proveedores, relación con el proyecto guardado, carrito sin checkout real.
3. **Guía y checklist**: mini guía de hidroponía para principiantes + checklist adaptado al proyecto guardado.

Además contempla, de forma preparada pero no obligatoria:

- datos precargados (cultivos, sistemas, componentes, productos, proveedores, ubicaciones);
- proyectos guardados de demostración;
- placeholder de un futuro agente/asistente de consultas;
- integración geográfica preparada (coordenadas, mapa) pero no ejecutada contra un servicio real;
- generación de una propuesta visual (croquis conceptual).

## 7. Alcance del sistema completo (fuera del MVP, mencionado como visión)

- ABM de productos y de proveedores;
- APIs reales de precios y de mapas;
- análisis avanzado del terreno (satelital, topográfico);
- conexión con profesionales/consultoría paga;
- agente de IA real;
- realidad aumentada completa;
- IoT, monitoreo y automatización;
- trazabilidad y comercialización avanzada.

Estas funcionalidades **no** son requisitos del MVP; se documentan solo como contexto de hacia dónde podría crecer el sistema.

## 8. Fuera del prototipo (explícito)

- ABM de proveedores.
- ABM de productos.
- Compras reales, pagos, checkout.
- Scraping de precios.
- Dependencia de APIs externas obligatorias (Maps, precios, clima).
- IoT, automatización, monitoreo en tiempo real.
- Análisis profesional de suelo o cálculo estructural/ingeniería constructiva.
- Agente de IA real (solo placeholder).
- AR completa (solo demostración simplificada u opcional).
- Cálculo profesional/certificado de rentabilidad.

## 9. Recorridos demostrables

### Recorrido A — Evaluación y planificación de proyecto
Crear proyecto → situación inicial → ubicación/espacio → orientación y condiciones → cultivo → sistema hidropónico → configuración de producción e infraestructura → inversión → producción e ingresos → recuperación aproximada → TCO/mantenimiento → propuesta visual.

### Recorrido B — Productos y carrito
Explorar catálogo mock → ver productos destacados para el proyecto guardado → agregar/quitar del carrito → ver subtotal/total.

### Recorrido C — Guía y checklist
Consultar mini guía de hidroponía → seleccionar proyecto guardado → ver checklist adaptado → marcar pasos completados → placeholder de consulta a Brota.

## 10. Pantalla de inicio

Brota no obliga a crear un proyecto para empezar. La pantalla inicial ofrece los tres recorridos:

```
BROTA
Planificá, aprendé y preparé tu proyecto hidropónico.

[ Evaluar mi proyecto ]      Analizá espacio, cultivo, infraestructura e inversión.
[ Explorar productos ]        Encontrá productos y componentes para tu configuración.
[ Guía de hidroponía ]        Aprendé los fundamentos y seguí una checklist para tu proyecto.
```
