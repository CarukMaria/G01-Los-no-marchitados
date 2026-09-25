# 10 — Datos iniciales y mocks

## 1. Objetivo

Definir el conjunto mínimo de datos precargados necesarios para que los tres recorridos de Brota (evaluación, productos/carrito, guía/checklist) puedan ejecutarse de principio a fin sin depender de Internet ni de integraciones reales.

## 3. Requisitos funcionales

**RF-034 — Proveer set de datos semilla**
- Entradas: ninguna (dato estático del sistema).
- Procesamiento: carga, al iniciar el prototipo, todos los conjuntos de datos descriptos en este spec.
- Salida: datos disponibles para todos los módulos.
- Prioridad: alta (bloqueante para el resto del prototipo).

## 4. Reglas de negocio

**RN-034** — Ningún dato de este spec debe presentarse en la interfaz como información en tiempo real; donde corresponda, debe indicarse "dato de referencia" o "dato de demostración".

**RN-035** — Todos los datos aquí definidos deben poder reemplazarse después por una fuente dinámica sin cambiar la lógica de negocio de los demás módulos (contract.md, sección 2).

## 6. Modelo de datos (contenido semilla)

### 6.1 Cultivos (mínimo 6, ejemplo)

| id | nombre | descripción breve | ciclo aprox. | sistemas compatibles |
|----|--------|--------------------|---------------|------------------------|
| lechuga | Lechuga | Ciclo relativamente corto, adecuado para NFT y DWC. | 30–45 días | nft, dwc |
| rucula | Rúcula | Ciclo corto, buena adaptación a sistemas de baja profundidad. | 25–35 días | nft, dwc |
| espinaca | Espinaca | Ciclo corto a medio, tolera bien DWC. | 35–45 días | dwc, nft |
| acelga | Acelga | Ciclo medio, requiere algo más de espacio entre plantas. | 40–55 días | nft, goteo |
| albahaca | Albahaca | Aromática de ciclo medio, buena para NFT y goteo. | 30–40 días | nft, goteo |
| frutilla | Frutilla | Ciclo más largo, se adapta bien a sistemas de goteo. | 60–90 días (primer fruto) | goteo, nft |
| tomate | Tomate | Ciclo largo, requiere mayor infraestructura de soporte. | 70–90 días (primer fruto) | goteo |

Cada cultivo incluye además, como dato mock: `rendimientoEstimado` (ej. kg por módulo por ciclo) y `precioReferencia` (para spec 06).

### 6.2 Sistemas hidropónicos (mínimo 3)

| id | nombre | descripción breve | componentes requeridos (base) |
|----|--------|--------------------|-------------------------------|
| nft | NFT | Técnica en la que una fina película de solución nutritiva circula continuamente por canales donde están las raíces. | tanque, bomba, canales/rack_nft, cañerías, filtro, medidor_ph |
| dwc | DWC | Las raíces permanecen sumergidas en solución nutritiva, con oxigenación mediante aireación. | tanque, bomba_aireacion, contenedor_dwc, medidor_ph |
| goteo | Goteo | La solución nutritiva se suministra mediante líneas de goteo directamente a cada planta. | tanque, bomba, lineas_goteo, filtro, medidor_ph |

### 6.3 Componentes (mínimo 10)

| id | nombre | categoría | unidad | costo (mock) | vida útil | mantenimiento |
|----|--------|-----------|--------|----------------|-----------|-----------------|
| estructura_invernadero | Estructura de invernadero | estructura | m² | $XX | 10 años | revisión anual |
| cubierta | Cubierta/film | estructura | m² | $XX | 3 años | reemplazo trienal |
| tanque | Tanque de reserva | hidráulico | unidad | $XX | 8 años | limpieza semestral |
| bomba | Bomba de agua | equipamiento | unidad | $XX | 2 años | revisión semestral |
| bomba_aireacion | Bomba de aireación | equipamiento | unidad | $XX | 2 años | revisión semestral |
| canales_nft | Canales NFT | producción | metro | $XX | 5 años | limpieza mensual |
| contenedor_dwc | Contenedor DWC | producción | unidad | $XX | 5 años | limpieza mensual |
| lineas_goteo | Líneas de goteo | hidráulico | metro | $XX | 3 años | revisión trimestral |
| filtro | Filtro | hidráulico | unidad | $XX | 6 meses | reemplazo semestral |
| medidor_ph | Medidor de pH | equipamiento | unidad | $XX | 12 meses | calibración mensual |

`factorEscala` (regla mock de RN-012, spec 04): componentes de producción (canales, contenedores, racks) escalan 1:1 con `cantidadModulos`; tanque, bomba y medidor de pH pueden mantenerse en 1 unidad hasta cierto umbral de módulos (definido como dato mock por componente).

### 6.4 Productos (varios por componente)

Cada producto: `id`, `nombre`, `categoria`, `descripcion`, `proveedorId`, `precio`, `unidad`, `disponibilidadSimulada`, `componenteId` (relación con 6.3), `precioReferenciaExterno` (`{monto, fuente: "referencia externa (demo)", fechaMock}`). Se recomienda al menos 2 productos por componente, de proveedores distintos, para poder demostrar comparación.

### 6.5 Proveedores (mínimo 3, mock)

Ejemplo: `id`, `nombre` (ej. "Proveedor Agro Misiones", "Hidro Insumos NEA", "Vivero Técnico Apóstoles"), `ubicacion`, `categoriasQueOfrece`.

### 6.6 Proyectos de demostración (mínimo 2–3)

Ejemplo:
- **"Huerta familiar"**: situación "desde cero", cultivo lechuga, sistema NFT, 3 módulos, ubicación Apóstoles, presupuesto definido.
- **"Ampliación con infraestructura parcial"**: situación "infraestructura parcial" (ya tiene terreno e invernadero), cultivo frutilla, sistema goteo, objetivo de producción definido.
- **"Nueva inversión — tomate"**: situación "nueva inversión", cultivo tomate, sistema goteo, escala mayor, para mostrar TCO e inversión más altos.

Cada proyecto demo debe tener su configuración completa en todos los módulos (01 a 06) para poder recorrer también specs 07, 08 y 09 sin pasos adicionales.

### 6.7 Ubicación (datos mock, con foco en Apóstoles)

`id`, `nombreReferencia` (ej. "Apóstoles, Misiones"), `latitud`/`longitud` (mock), `orientacionSugerida`, `condicionesClimaticasReferencia` (texto breve, criterios INTA simplificados), `riesgoInundacionReferencia`.

### 6.8 Mapa (mock)

Estructura preparada para "seleccionar ubicación en mapa": al confirmarse, devuelve coordenadas mock predefinidas (ej. las de Apóstoles) en vez de conectar con un servicio real.

### 6.9 Precios externos (mock)

Para cada producto con `precioReferenciaExterno`: monto simulado, `fuente: "precio externo de demostración"`, `fechaMock` fija (no se actualiza dinámicamente).

### 6.10 Checklist (por sistema, mínimo)

Ejemplo checklist base para NFT (reutilizable/adaptable a DWC y goteo con variaciones menores):

```
☐ Preparar el espacio
☐ Verificar orientación del invernadero
☐ Instalar tanque
☐ Instalar bomba
☐ Preparar cañerías
☐ Montar módulos/canales
☐ Verificar circulación del agua
☐ Preparar solución nutritiva
☐ Medir pH
☐ Realizar prueba de circulación
☐ Preparar cultivo
```

Se recomienda una variante por sistema (NFT, DWC, goteo), ajustando 2–3 pasos específicos (ej. DWC: "verificar aireación" en vez de "verificar circulación").

### 6.11 Agente (placeholder)

Dato mínimo: texto fijo de invitación a consultar + lista de temas futuros ("infraestructura", "cultivos", "sistemas hidropónicos", "mantenimiento", "configuración de tu proyecto"). Sin lógica ni modelo de IA real asociado.

## 7. Entradas y salidas

Entrada: ninguna (datos estáticos del sistema).
Salida: todos los conjuntos de datos anteriores, consumidos por specs 01 a 09.

## 8. Plan de tareas

| ID | Tarea | Archivos/componentes afectados |
|----|-------|---------------------------------|
| T10-01 | Definir archivo(s) semilla de cultivos y sistemas hidropónicos | datos/seed |
| T10-02 | Definir archivo(s) semilla de componentes | datos/seed |
| T10-03 | Definir archivo(s) semilla de productos y proveedores | datos/seed |
| T10-04 | Definir proyectos de demostración completos | datos/seed |
| T10-05 | Definir ubicaciones mock (con foco en Apóstoles) | datos/seed |
| T10-06 | Definir checklists por sistema hidropónico | datos/seed |
| T10-07 | Definir contenido de la mini guía general | datos/seed |
| T10-08 | Definir texto placeholder del agente | datos/seed |

## 9. Criterios de aceptación

```
CA-38
Existen al menos 6 cultivos, cada uno con descripción breve y sistemas compatibles.

CA-39
Existen al menos 3 sistemas hidropónicos, cada uno con descripción breve para principiantes.

CA-40
Existen al menos 2 o 3 proyectos de demostración completamente configurados, utilizables de inmediato en los tres recorridos.

CA-41
El prototipo puede ejecutarse íntegramente sin conexión a Internet, usando únicamente estos datos.
```

## 10. Estrategia de verificación

Verificar manualmente que cada proyecto de demostración permite completar sin errores: evaluación completa (specs 01–06, 09), catálogo con indicadores correctos (spec 07) y guía/checklist adaptado (spec 08).

## 11. Dependencias

```
Depende de:
Ninguna (módulo de datos base)

Es utilizado por:
01, 02, 03, 04, 05, 06, 07, 08, 09 (transversal a todo el prototipo)
```

## Decisiones pendientes (de esta spec)

- Definir si los proyectos demo son editables por el usuario o de solo lectura ("proyecto de ejemplo").
- Definir horizonte temporal fijo para la proyección de TCO (5 o 10 años).
- Definir si se necesitan más de 2–3 proyectos demo para la presentación ante el jurado.
