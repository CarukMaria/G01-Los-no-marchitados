# contract.md — Contrato técnico común

Reglas transversales a **todos** los módulos y specs. No describe funcionalidades de un módulo en particular.

## 4.1 Principios de desarrollo
- **Arquitectura modular**: un módulo por spec; los módulos se comunican por funciones/interfaces con entradas y salidas definidas en los specs, no por acceso directo a los datos internos del otro.
- **Separación en tres capas**: (1) *datos de referencia* (catálogos, parámetros), (2) *lógica de cálculo* (funciones puras, sin acceso a UI ni a almacenamiento), (3) *presentación* (pantallas). La lógica de cálculo no formatea texto ni importa componentes de UI.
- **Funciones de cálculo puras y determinísticas**: mismas entradas → mismas salidas. Sin fechas del sistema, azar ni red dentro del cálculo (la fecha actual se inyecta como entrada).
- **Preparado para ampliación**: acceso a datos externos siempre a través de las interfaces de servicio definidas en `specs/08`.
- **Evitar duplicación**: cada fórmula, regla y texto de aviso se define en un único lugar (spec dueño) y se reutiliza.
- **No introducir funcionalidades no especificadas.**

## 4.2 Fuente de verdad
- `project.md`: propósito y alcance general.
- `specs/`: comportamiento funcional (RF, RN, modelo de datos, criterios de aceptación).
- `contract.md`: restricciones técnicas comunes.
- Ante una **contradicción** entre documentos, o un dato/regla que falte, **detenerse y señalarla** antes de implementar. No resolverla por cuenta propia.
- Jerarquía ante duda de alcance: `project.md` (qué queda fuera) prevalece sobre cualquier spec.

## 4.3 Datos configurables
- Cultivos, componentes, precios, vidas útiles, costos, parámetros productivos y criterios de ubicación viven en **archivos/tablas de datos semilla**, cargados a través de repositorios (`obtenerCultivos()`, `obtenerComponentes()`, `obtenerParametros()`, `obtenerCriteriosUbicacion()`).
- Ninguna constante de negocio (precio, rendimiento, porcentaje, dimensión) puede estar escrita dentro de la lógica de cálculo ni de la presentación.
- Los datos semilla del prototipo están definidos en `specs/08`. Deben poder reemplazarse por otra fuente sin modificar la lógica de cálculo.

## 4.4 Estimaciones y avisos obligatorios
Toda producción, ingreso, costo, rentabilidad, amortización o factibilidad se presenta **como estimación**. Nunca como certeza ni garantía.

Textos de aviso (definidos una sola vez como constantes de contenido y reutilizados; el texto exacto puede ajustarse sin cambiar su identificador):

| Constante | Dónde debe mostrarse | Texto base |
|---|---|---|
| `AVISO_ESTIMACION` | Toda pantalla/sección con producción, ingresos, inversión, recuperación o TCO | "Valores estimados y orientativos, calculados con los supuestos indicados. No constituyen garantía de resultados ni de rentabilidad." |
| `AVISO_FACTIBILIDAD` | Resultado de factibilidad y propuesta | "Evaluación preliminar basada en los datos ingresados y en criterios de referencia. No reemplaza estudios profesionales de suelo, ingeniería o construcción." |
| `AVISO_CROQUIS` | Croquis y vista frontal | "Representación conceptual generada a partir de la configuración. No es un plano arquitectónico ni constructivo." |
| `AVISO_DATOS_DEMO` | Toda pantalla mientras `origenDatos = MOCK` o `SEMILLA` | "Precios y parámetros de demostración; no son cotizaciones ni datos validados." |

## 4.5 Integraciones externas
- Toda fuente externa (geografía, clima, criterios INTA, precios, AR) se consume mediante una **interfaz de servicio** con respuesta envuelta en el formato común de `specs/08` (`origen`, `fechaActualizacion`, `datos`, `advertencias`).
- En el prototipo **todas** las implementaciones son mocks/semilla locales. El flujo principal **no debe depender** de red ni de servicios de terceros.
- Si un servicio falla o no está disponible, el flujo continúa con datos semilla y se muestra una advertencia; nunca se bloquea el flujo principal.

## 4.6 Seguridad
- **Validación de entradas** en el punto de ingreso y también dentro de la lógica de cálculo: tipo, rango, obligatoriedad, longitud máxima (textos ≤ 80 caracteres para nombres, ≤ 280 para descripciones). Números: finitos, no `NaN`, no negativos salvo indicación.
- **Manejo de errores**: los errores de validación y de cálculo se devuelven como objeto `{ codigo, mensaje, campo? }` (ver 4.7); la presentación los muestra junto al campo o en un aviso. No se muestran trazas técnicas al usuario. Ningún error debe dejar el proyecto guardado en estado inconsistente.
- **Sin secretos en el código ni en el repositorio.** Si en el futuro se necesitan credenciales/URLs de servicios, se leen de **variables de entorno** (con un archivo de ejemplo sin valores reales). El prototipo no requiere ninguna.
- Los textos ingresados por el usuario se tratan como datos (no se interpretan como código/HTML al mostrarse).
- El prototipo no tiene autenticación ni datos personales sensibles; el proyecto se identifica localmente.

## 4.7 Convenciones
- **Idioma**: interfaz en español (Argentina). Nombres de dominio (entidades, campos, funciones de negocio) **en español**, tal como aparecen en los specs, sin tildes ni eñes en identificadores (`anio`, `cania` sólo dentro de IDs de catálogo).
- **Nombres**: campos y variables `camelCase` (`costoUnitario`); entidades `PascalCase` (`ConfiguracionProductiva`); enumeraciones y IDs de catálogo `MAYUSCULAS_CON_GUION_BAJO` (`MOD_NFT`, `DENTRO_DE_PRESUPUESTO`); archivos de documentación `kebab-case`.
- **Identificadores**: proyecto `PRY-<texto único>`; IDs de catálogo estables y no reutilizables. Una referencia a un ID inexistente es un error de datos (`DATO_INCONSISTENTE`), no se ignora.
- **Formato de datos**: estructuras serializables como JSON; fechas ISO 8601 (`AAAA-MM-DD`); decimales con punto en datos; formato `es-AR` (miles con punto, decimal con coma) **solo** en presentación.
- **Unidades**: longitudes en **metros (m)**, superficies en **m²**, volúmenes en **litros (L)**, masa en **kilogramos (kg)**, tiempo en **días/meses/años** (mes = 30 días para ciclos, mes calendario para proyecciones), porcentajes en el rango decimal `0–1` dentro de datos y cálculos (`0.15`) y como `%` solo en presentación (excepto `pendientePct`, que se ingresa en % como 0–100).
- **Moneda**: un único código de moneda en los parámetros (`moneda`, propuesta `ARS`); montos como números sin redondear en cálculo; **redondeo a entero solo al presentar**. No mezclar monedas.
- **Errores**: `codigo` en `MAYUSCULAS_CON_GUION_BAJO` (ver códigos comunes: `CAMPO_OBLIGATORIO`, `VALOR_FUERA_DE_RANGO`, `FORMATO_INVALIDO`, `DATO_INCONSISTENTE`, `ESTADO_INVALIDO`).
- **Requisitos e IDs de documentación**: `HU-nn` historias, `RF-nnn` requisitos funcionales, `RN-nnn` reglas de negocio, `CA-nn` criterios (dentro de cada RF), `T-nn-mm` tareas (nn = número de spec). Los IDs son estables: no se renumeran.
- **No se imponen tecnologías** (lenguaje, framework, base de datos, librería gráfica). Esas decisiones están pendientes (ver `project.md`).

## 4.8 Restricción de alcance
Una IA de programación **no debe implementar** funcionalidades futuras solo porque aparezcan mencionadas en `project.md` o en notas de extensión de un spec. Solo se implementa lo que tenga RF/RN/tarea en `specs/`. Los "puntos de extensión" se implementan únicamente como interfaz + mock, sin lógica adicional.

## 4.9 Persistencia y estado del proyecto
- El proyecto y sus resultados se guardan mediante una **interfaz de repositorio** (`guardarProyecto`, `obtenerProyecto`, `listarProyectos`, `duplicarProyecto`); el mecanismo concreto es una decisión pendiente. Debe funcionar sin conexión.
- Los resultados calculados (inversión, producción, TCO, croquis) **se recalculan** a partir de las entradas cuando cambian; no se editan a mano. Si una entrada aguas arriba cambia, los pasos posteriores deben marcarse como *desactualizados* hasta recalcularse.
