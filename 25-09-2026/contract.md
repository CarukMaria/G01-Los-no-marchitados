# contract.md — Contrato técnico transversal de Brota

Este documento define reglas que aplican a **todo** el prototipo. Cada spec de `specs/` puede agregar reglas específicas de su módulo, pero no puede contradecir este contrato.

## 1. Arquitectura

- Arquitectura modular: cada recorrido (evaluación, productos, guía) debe poder desarrollarse y probarse de forma independiente, compartiendo solo lo estrictamente necesario (proyecto guardado, catálogo de datos).
- Separación clara entre **datos**, **lógica** y **presentación**. Ningún dato de negocio (precios, cultivos, componentes, checklists) debe quedar incrustado en componentes de UI.
- Favorecer componentes reutilizables (tarjeta de producto, tarjeta de cultivo/sistema con explicación, checklist, resumen de proyecto) antes que duplicar markup.
- No implementar funcionalidades no especificadas en `project.md` o en `specs/`. Ante una duda de alcance, la especificación funcional decide, no la conveniencia técnica.

## 2. Datos

Deben mantenerse como datos configurables, separados de la lógica de presentación:

- cultivos;
- sistemas hidropónicos;
- componentes;
- productos;
- proveedores;
- precios (propios y de referencia externa);
- proyectos (guardados/demo);
- checklists;
- parámetros productivos y técnicos.

El prototipo puede implementarse con datos hardcodeados, archivos seed (JSON/JS) o una base de datos simple (ej. SQLite/archivo local). Cualquiera sea la opción elegida, debe ser reemplazable después por una fuente dinámica sin reescribir la lógica de negocio.

## 3. Fuente de verdad

```
project.md
    ↓
contract.md
    ↓
specs/
    ↓
implementación
```

Ante un conflicto o ambigüedad, las especificaciones funcionales (`specs/`) priman sobre supuestos de la IA de programación o del equipo. Si un requisito no está cubierto por ningún spec, no se implementa sin antes documentarlo (ver "Decisiones pendientes" en cada spec o en un archivo aparte).

## 4. Estimaciones

Toda salida que dependa de supuestos debe identificarse explícitamente como estimación en la interfaz y en la documentación. Nunca deben presentarse como certeza:

- producción;
- ingresos;
- factibilidad del terreno;
- rentabilidad;
- tiempo de amortización/recuperación;
- precios externos ("de referencia").

Redacción sugerida: *"Estimación orientativa según los datos ingresados y los supuestos utilizados. No reemplaza asesoramiento profesional."*

## 5. Integraciones externas

Deben ser reemplazables por mocks y **nunca** indispensables para ejecutar el prototipo:

- Google Maps / servicios cartográficos;
- fuentes de precios externos;
- APIs de proveedores;
- futuro agente de IA;
- futura realidad aumentada.

## 6. Prototipado de integraciones no implementadas

Cuando una integración todavía no está implementada, debe dejarse una interfaz o estructura preparada (función, componente, punto de extensión) claramente marcada como demo/preparada. No debe simularse como si la integración real hubiera ocurrido (por ejemplo, no mostrar un mapa real ni datos "en vivo" que en realidad son estáticos sin aclararlo).

## 7. Idioma y tono

Toda la interfaz y la documentación de cara al usuario se redactan en español, con lenguaje simple orientado a un productor sin conocimientos técnicos previos.

## 8. Identificadores

- Requisitos funcionales: `RF-XXX`.
- Reglas de negocio: `RN-XXX`.
- Criterios de aceptación: `CA-XX` (por spec, reiniciando numeración o con prefijo del módulo, a decidir por el equipo — ver Decisiones pendientes).

## 9. Alcance general

Ningún spec debe implementar: ABM de productos/proveedores, pagos/checkout real, scraping, IoT, monitoreo en tiempo real, automatización, agente de IA real, AR avanzada, cálculo estructural o análisis profesional de suelo. Ver `project.md` sección 8 para el listado completo fuera de alcance.
