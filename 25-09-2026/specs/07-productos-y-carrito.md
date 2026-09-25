# 07 — Productos y carrito

## 1. Objetivo

Ofrecer un catálogo de productos mock (representando proveedores registrados en Brota), permitir agregarlos a un carrito simple, y destacar los productos relacionados con las necesidades del proyecto guardado del usuario.

## 2. Historias de usuario / casos de uso

- Como usuario, quiero explorar productos desde el inicio de Brota, sin necesidad de tener un proyecto configurado.
- Como usuario, quiero ver el detalle de cada producto (proveedor, precio, unidad).
- Como usuario, quiero ver un precio de referencia de Internet junto al precio del proveedor registrado, para comparar.
- Como usuario con un proyecto guardado, quiero que el catálogo me muestre qué productos necesito, cuáles ya tengo contemplados y cuáles me faltan.
- Como usuario, quiero agregar productos a un carrito, modificar cantidades y ver el subtotal/total, sin necesidad de pagar ni comprar realmente.

## 3. Requisitos funcionales

**RF-020 — Listar catálogo de productos**
- Entradas: ninguna obligatoria (filtros opcionales: categoría, proyecto).
- Procesamiento: devuelve productos mock (spec 10) con su información básica.
- Salida: listado de `Producto`.
- Prioridad: alta.

**RF-021 — Ver detalle de producto**
- Entradas: id de producto.
- Procesamiento: obtiene nombre, categoría, descripción, proveedor, precio, unidad, disponibilidad simulada, compatibilidad/uso, y precio de referencia externo (si existe).
- Salida: detalle del producto.
- Prioridad: media.

**RF-022 — Destacar productos necesarios para un proyecto**
- Entradas: proyecto seleccionado por el usuario, lista de `ConfiguracionComponente` de ese proyecto (spec 04).
- Procesamiento: relaciona cada `Producto` con su `componenteId` correspondiente; compara contra `ConfiguracionComponente` del proyecto y asigna un indicador.
- Salida: cada producto del catálogo muestra, si hay proyecto seleccionado, uno de los indicadores: "necesario para tu proyecto", "recomendado", "ya contemplado en tu proyecto", o ninguno si no aplica.
- Prioridad: alta (requisito destacado en el prompt maestro).

**RF-023 — Gestionar carrito**
- Entradas: producto, cantidad.
- Procesamiento: agrega, quita o modifica cantidad de ítems en `Carrito`; recalcula subtotal por ítem y total.
- Salida: `Carrito` actualizado.
- Prioridad: alta.

**RF-024 — Relacionar carrito con proyecto**
- Entradas: proyecto activo (opcional).
- Procesamiento: si hay proyecto seleccionado, el carrito puede asociarse a ese proyecto (referencia informativa, no obligatoria).
- Salida: `Carrito.proyectoId` (opcional).
- Prioridad: baja.

## 4. Reglas de negocio

**RN-022** — El catálogo y los proveedores son exclusivamente datos precargados/mock. No existe alta, baja ni edición de productos o proveedores en el prototipo.

**RN-023** — El indicador "necesario para tu proyecto" se basa en `ConfiguracionComponente.origen = "requerido"` del proyecto seleccionado; "ya contemplado" corresponde a `origen = "ya_contemplado"`.

**RN-024** — El precio de referencia externo se muestra siempre identificado como "precio de referencia de demostración" o similar, con una fecha mock de actualización, y nunca se presenta como un precio en tiempo real ni se obtiene por scraping.

**RN-025** — El carrito no requiere checkout, pago ni validación de stock real; la "disponibilidad" es un dato simulado meramente informativo.

**RN-026** — La cantidad de cada ítem del carrito debe ser un número entero mayor a 0; si llega a 0, el ítem se quita del carrito.

## 5. Restricciones técnicas específicas

No requiere scraping ni conexión a APIs de proveedores o de precios. Todo el catálogo, precios y relación producto-componente son datos mock (spec 10).

## 6. Modelo de datos

**Producto**
- `id`, `nombre`, `categoria`, `descripcion`, `proveedorId`, `precio`, `unidad`, `disponibilidadSimulada` (bool), `componenteId` (relación con `Componente`, spec 04), `precioReferenciaExterno` (opcional: `{monto, fuente, fechaMock}`).

**Proveedor**
- `id`, `nombre`, `ubicacion` (opcional), `categoriasQueOfrece`.

**Carrito**
- `id`, `proyectoId` (opcional), `items`: lista de `ItemCarrito`.

**ItemCarrito**
- `id`, `productoId`, `cantidad`, `subtotal` (derivado: `cantidad × precio`).

## 7. Entradas y salidas

Entrada: catálogo mock (spec 10), `ConfiguracionComponente` del proyecto seleccionado (spec 04, opcional).
Salida: catálogo con indicadores por proyecto, `Carrito` con subtotal y total.

## 8. Plan de tareas

| ID | Tarea | Archivos/componentes afectados |
|----|-------|---------------------------------|
| T07-01 | Pantalla de catálogo de productos (accesible desde el inicio) | UI catálogo |
| T07-02 | Detalle de producto con precio propio y de referencia externa | UI detalle |
| T07-03 | Selector de proyecto dentro del catálogo | UI catálogo |
| T07-04 | Lógica de indicadores "necesario"/"recomendado"/"ya contemplado" | lógica de negocio |
| T07-05 | Carrito: agregar, quitar, modificar cantidad, subtotal/total | UI + lógica carrito |
| T07-06 | Persistencia de `Carrito` | capa de datos |

## 9. Criterios de aceptación

```
CA-25
El usuario puede acceder al catálogo de productos desde la pantalla de inicio, sin necesidad de tener un proyecto configurado.

CA-26
Cada producto muestra su proveedor, precio propio y, cuando existe, un precio de referencia externo claramente diferenciado.

CA-27
Si el usuario selecciona un proyecto guardado con configuración de infraestructura, el catálogo destaca los productos necesarios, recomendados o ya contemplados para ese proyecto.

CA-28
El usuario puede agregar productos al carrito, modificar cantidades y ver el subtotal y el total actualizados.

CA-29
El carrito no ofrece ni simula un checkout o pago real.
```

## 10. Estrategia de verificación

Probar: catálogo sin proyecto seleccionado (sin indicadores), catálogo con proyecto seleccionado (indicadores correctos), carrito vacío, agregar/quitar productos, cantidad inválida (0 o negativa).

## 11. Dependencias

```
Depende de:
01 Proyecto y configuración
04 Infraestructura y equipamiento (para los indicadores por proyecto)
10 Datos iniciales y mocks

Es utilizado por:
Ninguno directamente (es un recorrido final, "Recorrido B")
```
