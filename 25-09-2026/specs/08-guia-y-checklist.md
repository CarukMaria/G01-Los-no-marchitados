# 08 — Guía y checklist

## 1. Objetivo

Ofrecer una mini guía de hidroponía para principiantes y un checklist de pasos adaptado al proyecto guardado del usuario, más un espacio preparado (placeholder) para un futuro agente de consultas.

## 2. Historias de usuario / casos de uso

- Como usuario principiante, quiero acceder desde el inicio a una guía simple sobre hidroponía, sin necesidad de tener un proyecto creado.
- Como usuario, quiero seleccionar uno de mis proyectos guardados y ver una guía/checklist adaptada a su cultivo y sistema.
- Como usuario, quiero marcar los pasos del checklist como completados, para hacer seguimiento de mi avance.
- Como usuario, quiero ver un espacio para consultar dudas a Brota, aunque hoy sea solo un placeholder.

## 3. Requisitos funcionales

**RF-025 — Mostrar mini guía general**
- Entradas: ninguna.
- Procesamiend: devuelve contenidos fijos (spec 10) sobre fundamentos de hidroponía, tipos de sistemas, componentes, pH, nutrientes, mantenimiento básico.
- Salida: contenido de guía en pantalla, organizado por secciones cortas.
- Prioridad: alta.

**RF-026 — Seleccionar proyecto para guía adaptada**
- Entradas: proyecto guardado elegido por el usuario.
- Procesamiento: obtiene cultivo, sistema y escala del proyecto (spec 02) para adaptar el contenido mostrado.
- Salida: guía adaptada (resumen de qué aplica a ese proyecto en particular).
- Prioridad: media.

**RF-027 — Generar checklist adaptado**
- Entradas: sistema hidropónico y cultivo del proyecto seleccionado.
- Procesamiento: busca el checklist precargado correspondiente a esa combinación (o al sistema, si no hay uno específico por cultivo) en spec 10.
- Salida: lista de `PasoChecklist` asociada al proyecto.
- Prioridad: alta.

**RF-028 — Marcar pasos completados**
- Entradas: paso del checklist, estado (completado/pendiente).
- Procesamiento: actualiza el estado del paso para ese proyecto.
- Salida: checklist actualizado con progreso visible (ej. "4 de 10 completados").
- Prioridad: media.

**RF-029 — Mostrar placeholder de consulta**
- Entradas: ninguna.
- Procesamiento: muestra un bloque fijo invitando a "Consultar a Brota", sin lógica de IA real detrás.
- Salida: bloque visual con mensaje de "próximamente" y temas que podrá cubrir.
- Prioridad: baja.

## 4. Reglas de negocio

**RN-027** — Los checklists son datos hardcodeados por sistema hidropónico (y opcionalmente por cultivo); no se genera dinámicamente un procedimiento nuevo.

**RN-028** — El progreso del checklist (pasos completados) se guarda por proyecto, no de forma global.

**RN-029** — La guía general (sin proyecto seleccionado) siempre debe estar disponible, incluso si el usuario no tiene ningún proyecto creado.

**RN-030** — El placeholder de consulta nunca debe simular una respuesta real generada por IA; debe dejar explícito que es una funcionalidad futura.

## 5. Restricciones técnicas específicas

Ninguna integración externa. El futuro agente se documenta como punto de extensión, no se implementa lógica de IA real salvo decisión explícita del equipo (ver Decisiones pendientes).

## 6. Modelo de datos

**Guia**
- `id`, `titulo`, `contenido` (texto/secciones), `aplicaA` (opcional: sistema o cultivo específico, si es contenido adaptado).

**PasoChecklist**
- `id`, `proyectoId`, `sistemaId` (referencia al checklist base), `descripcion`, `orden`, `completado` (bool).

## 7. Entradas y salidas

Entrada: proyecto seleccionado (opcional), contenidos de guía y checklists mock (spec 10).
Salida: guía general o adaptada, checklist con progreso por proyecto.

## 8. Plan de tareas

| ID | Tarea | Archivos/componentes afectados |
|----|-------|---------------------------------|
| T08-01 | Pantalla de mini guía general (accesible desde el inicio) | UI guía |
| T08-02 | Selector de proyecto para guía/checklist adaptado | UI guía |
| T08-03 | Lógica de obtención de checklist según sistema/cultivo del proyecto | lógica de negocio |
| T08-04 | Marcado de pasos completados y cálculo de progreso | UI + lógica checklist |
| T08-05 | Bloque placeholder de consulta a Brota | UI consultas |

## 9. Criterios de aceptación

```
CA-30
El usuario puede acceder a la guía general sin tener un proyecto creado.

CA-31
El usuario puede seleccionar un proyecto guardado y ver un checklist adaptado a su sistema hidropónico.

CA-32
El usuario puede marcar pasos del checklist como completados y ver su progreso.

CA-33
Existe un bloque de "Consultar a Brota" claramente identificado como funcionalidad futura/placeholder.
```

## 10. Estrategia de verificación

Probar: guía sin proyecto seleccionado, guía con proyecto seleccionado (checklist correcto según sistema), marcado de pasos y persistencia del progreso, proyecto sin configuración completa (mensaje adecuado en vez de error).

## 11. Dependencias

```
Depende de:
01 Proyecto y configuración
02 Planificación productiva
10 Datos iniciales y mocks

Es utilizado por:
Ninguno directamente (es un recorrido final, "Recorrido C")
```
