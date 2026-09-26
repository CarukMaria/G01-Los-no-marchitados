# SPEC 07 — Propuesta visual

## 1. Objetivo
Generar la **propuesta final** que integra los resultados de los módulos anteriores: un resumen ejecutivo, un croquis conceptual en vista superior, una vista frontal y acciones demostrativas (AR y consulta a especialista). El croquis se genera **exclusivamente** a partir de la configuración del proyecto.

## 2. Historias de usuario / casos de uso
- **HU-09** — Como productor, quiero ver una propuesta completa y un croquis de cómo quedaría distribuida mi instalación para entender y comunicar el proyecto.

## 3. Requisitos funcionales

### RF-021 — Resumen de la propuesta
- **Descripción**: pantalla/documento de resumen.
- **Entradas**: `Proyecto`, `EvaluacionFactibilidad`, `ConfiguracionProductiva`, `Layout`, `EstimacionEconomica`, `EstimacionProduccionRetorno`, `ProyeccionTCO`.
- **Procesamiento**: ensamblar el `ResumenPropuesta` (función `generarResumen`) sin recalcular ninguna fórmula: solo lee resultados.
- **Salida**: bloque con: nombre del proyecto y localidad; superficie del terreno; semáforo de factibilidad y observaciones principales; cultivo, modalidad, `nModulos`, `plazasTotales`; superficie del invernadero; `inversionInicial` y `estadoPresupuesto`; `kgMes` e `ingresoBrutoMensual`; texto de recuperación (RF-019); `tcoTotal` a 60 meses; panel de supuestos; los cuatro avisos de `contract.md` 4.4.
- **Reglas relacionadas**: RN-036, RN-039, RN-040.
- **Prioridad**: Alta.
- **Criterios de aceptación**:
  - CA-01: el resumen del escenario demo muestra 8 módulos, inversión ≈ 13.313.923, ≈ 218,6 kg/mes, recuperación ≈ 26 meses (2,2 años) y `tcoTotal ≈ 17.101.163`.
  - CA-02: cada valor del resumen coincide exactamente con el de su pantalla de origen (no se recalcula).
  - CA-03: aparecen `AVISO_ESTIMACION`, `AVISO_FACTIBILIDAD`, `AVISO_CROQUIS` y `AVISO_DATOS_DEMO`.
  - CA-04: si `estadoDimensionamiento ≠ OK` o `estadoPresupuesto = SUPERA_PRESUPUESTO` se muestra un aviso destacado con el motivo y la sugerencia.

### RF-022 — Croquis conceptual: vista superior
- **Descripción**: representación en planta de terreno, invernadero e infraestructura principal.
- **Entradas**: `Proyecto.ubicacion`, `CondicionesTerreno.ejeLargoTerreno`, `Layout`, `nModulos`, módulo y tanque del catálogo, cantidad de tanques (spec 04), `ParametrosGlobales`.
- **Procesamiento**: función pura `generarModeloCroquis(...)` → `ModeloCroquis.vistaSuperior` (lista de elementos rectangulares en **metros**) según RN-037; un renderizador transforma ese modelo en imagen (la tecnología es libre, ver `contract.md`).
- **Salida**: vista superior con: límites del terreno y retiro perimetral, contorno del invernadero, **flecha del Norte** y eje de implantación (E–O / N–S), acceso, zona técnica, tanques, bloque de equipos (bombas/filtros/tablero), módulos numerados, circulación (pasillo central y laterales), barra de escala y leyenda.
- **Reglas relacionadas**: RN-036, RN-037, RN-038.
- **Prioridad**: Alta.
- **Criterios de aceptación**:
  - CA-01: demo (NFT, 8 módulos, eje `EO`) → invernadero de 14,4 m (E–O) × 4,8 m (N–S), 4 módulos en cada fila, 3 tanques en zona técnica, acceso en el extremo oeste.
  - CA-02: con `n = 5` aparecen 3 módulos en la fila norte y 2 en la fila sur.
  - CA-03: el dibujo mantiene las proporciones reales (metros) y muestra la escala.
  - CA-04: cambiar `nModulos` o el eje de implantación cambia el croquis sin intervención manual.
  - CA-05: el croquis siempre incluye `AVISO_CROQUIS` y la indicación de Norte.
  - CA-06: ningún elemento queda fuera del terreno ni dentro del retiro perimetral.

### RF-023 — Croquis conceptual: vista frontal
- **Descripción**: corte transversal del invernadero mirando desde el acceso hacia el interior.
- **Entradas**: `Layout`, módulo (`alturaM`, `niveles`, `anchoM`), tanque (`alturaM`, `diametroM`), `ParametrosGlobales` (`alturaLateralM`, `alturaCumbreraM`, `pasilloLateralM`, `pasilloCentralM`).
- **Procesamiento**: `generarModeloCroquis(...)` → `ModeloCroquis.vistaFrontal` según RN-037.
- **Salida**: estructura (paredes laterales, cubierta a dos aguas), racks por fila con sus niveles, tanque representativo, línea de piso, cotas de ancho y alturas (lateral y cumbrera) y `AVISO_CROQUIS`.
- **Reglas relacionadas**: RN-036, RN-037.
- **Prioridad**: Alta.
- **Criterios de aceptación**:
  - CA-01: demo → ancho 4,8 m, alturas 2,5 m y 3,5 m, dos racks de 3 niveles y 1,9 m de altura, con pasillo central de 1,2 m.
  - CA-02: con `n = 1` se dibuja un solo rack.
  - CA-03: con modalidad DWC los módulos se dibujan de 1 nivel y 0,9 m de altura.
  - CA-04: si la altura del módulo supera `alturaLateralM − 0,3` se rechaza con `DATO_INCONSISTENTE` (no se dibuja).

### RF-024 — Acciones demostrativas (AR y consulta a especialista)
- **Descripción**: dos acciones secundarias de la propuesta, ambas opcionales.
- **Entradas**: `ModeloCroquis`; respuesta de `ServicioAR` (mock).
- **Procesamiento**: "Ver en AR (demostrativo)": consulta `ServicioAR.estaDisponible()`. Si es verdadero llama `ServicioAR.iniciar(modeloCroquis)`; si es falso o falla, muestra el mensaje del mock. "Consultar a un especialista (demostrativo)": muestra un mensaje explicando que en la versión completa se podrá solicitar asistencia profesional; no envía datos ni tiene lógica.
- **Salida**: mensaje o experiencia mínima de AR; ningún efecto sobre los cálculos.
- **Reglas relacionadas**: RN-038.
- **Prioridad**: Baja.
- **Criterios de aceptación**:
  - CA-01: con AR no disponible, el botón muestra el mensaje y el resto de la propuesta sigue funcionando.
  - CA-02: la entrada de AR es el mismo `ModeloCroquis` (dimensiones en metros, orientación, posiciones).
  - CA-03: ambos botones están rotulados como "demostrativo".
  - CA-04: ninguna de las dos acciones altera datos del proyecto ni resultados.

## 4. Reglas de negocio
- **RN-036**: el croquis es una representación conceptual derivada solo de la configuración; no incluye cálculos estructurales ni hidráulicos ni sirve para obra; no debe mostrar cotas constructivas más allá de las dimensiones generales.
- **RN-037**: reglas de composición (coordenadas en metros; origen en la esquina noroeste del terreno; `x` crece hacia el Este, `y` hacia el Sur). Se usan coordenadas locales del invernadero `(u, v)`: `u` a lo largo del eje de implantación, `v` transversal; `W = anchoInvernaderoM`, `Lg = largoInvernaderoM`.

  **Vista superior**
  ```
  Terreno:      Wt (E–O) = largoM si ejeLargoTerreno = EO, si no anchoM
                Ht (N–S) = anchoM si ejeLargoTerreno = EO, si no largoM
  Retiro:       rectángulo interior a retiroPerimetralM del borde
  Invernadero:  eje EO → (w,h) = (Lg, W);  eje NS → (w,h) = (W, Lg);  centrado: x0 = (Wt − w)/2, y0 = (Ht − h)/2
  Transformación local→global de un rectángulo (u0, v0, du, dv):
        eje EO:  (x0 + u0, y0 + v0, du, dv)          (u=0 en el extremo Oeste, v=0 en el borde Norte)
        eje NS:  (x0 + v0, y0 + h − u0 − du, dv, du) (u=0 en el extremo Sur, v=0 en el borde Oeste)
  Elementos (coordenadas locales):
        zona técnica:  (0, 0, zonaTecnicaM, W)
        acceso:        (0, W/2 − 0.6, 0.1, 1.2)                     [extremo u=0]
        tanque j:      (1.3, 0.2 + 1.2·j, diametroM, diametroM)     para j = 0..2 (dentro)
                       (−1.3, 0.2 + 1.2·(j−3), diametroM, diametroM) para j ≥ 3 (afuera, etiqueta «exterior»)
        equipos:       (1.3, 3.8, 1.0, 1.0)  "Bombas / filtros / tablero"
        fila r (r=0,1): vFila = [pasilloLateralM, pasilloLateralM + anchoModulo + pasilloCentralM]
        módulo (r, k):  (zonaTecnicaM + k·largoModulo, vFila[r], largoModulo, anchoModulo),  k = 0..modulosPorFila[r]−1
        circulación:    pasillo central (zonaTecnicaM, pasilloLateralM + anchoModulo, Lg − zonaTecnicaM, pasilloCentralM)
        numeración de módulos: fila 0 de u creciente, luego fila 1
  ```
  **Vista frontal** (`x` = v, `y` = altura desde el piso)
  ```
  Estructura:   paredes en x = 0 y x = W hasta alturaLateralM; cubierta a dos aguas hasta alturaCumbreraM en x = W/2
  Racks:        por fila con modulosPorFila[r] > 0: rectángulo (vFila[r], 0, anchoModulo, alturaModulo) con «niveles» divisiones
  Tanque:       rectángulo (0.2, 0, diametroM, alturaTanque) con etiqueta «Tanque (zona técnica)», en trazo punteado
  Cotas:        ancho W, alturaLateralM, alturaCumbreraM, alturaModulo
  ```
- **RN-038**: la AR es una capacidad opcional de visualización y validación espacial; su ausencia no impide el flujo principal ni afecta los cálculos.
- **RN-039**: la propuesta solo se genera con los pasos 1–5 en estado `COMPLETO`. Si alguno está `DESACTUALIZADO`, se ofrece recalcular. Si `estadoDimensionamiento = PRESUPUESTO_INSUFICIENTE`, el croquis muestra 1 módulo con la etiqueta «referencia»; si `SUPERFICIE_INSUFICIENTE`, muestra `nMaxTerreno` módulos.
- **RN-040**: el resumen no contiene texto de venta de terceros; no hay publicidad ni recomendaciones patrocinadas.

## 5. Restricciones técnicas específicas
- Separar **generación del modelo geométrico** (función pura, testeable sin UI) del **renderizado**.
- El `ModeloCroquis` debe ser serializable (JSON) para poder alimentar la AR y las pruebas.
- Esta funcionalidad no realiza ningún cálculo económico ni productivo.

## 6. Modelo de datos
Entidades **no persistidas** (vista):

**ResumenPropuesta**: campos enumerados en RF-021 (solo lectura de resultados existentes).

**ModeloCroquis**
| Atributo | Tipo | Oblig. |
|---|---|---|
| `unidad` | texto (`m`) | Sí |
| `vistaSuperior` | `{terreno, retiro, invernadero, elementos[], norte, ejeLabel}` | Sí |
| `vistaFrontal` | `{ancho, alturaLateral, alturaCumbrera, elementos[]}` | Sí |
| `aviso` | texto (`AVISO_CROQUIS`) | Sí |

**ElementoCroquis**: `{tipo (MODULO, TANQUE, EQUIPOS, ACCESO, ZONA_TECNICA, CIRCULACION, TERRENO, RETIRO, INVERNADERO, RACK, TANQUE_FRONTAL), id, x, y, ancho, alto, etiqueta, exterior?}`.

## 7. Entradas y salidas
- **Entradas**: resultados de los specs 01–06.
- **Salidas**: pantalla 6 (resumen + croquis + vista frontal + acciones); `ModeloCroquis` disponible para AR.

## 8. Plan de tareas
| ID | Tarea | Descripción | Archivos/componentes afectados |
|---|---|---|---|
| T-07-01 | Modelo de croquis superior | `generarModeloCroquis().vistaSuperior` con RN-037 y pruebas de geometría | lógica de propuesta |
| T-07-02 | Modelo de vista frontal | `generarModeloCroquis().vistaFrontal` + validación de alturas | lógica de propuesta |
| T-07-03 | Renderizador | Dibujo de ambas vistas desde el modelo, con escala, Norte y leyenda | componentes visuales |
| T-07-04 | Resumen | `generarResumen()` y pantalla 6 con avisos y panel de supuestos | lógica y pantalla propuesta |
| T-07-05 | Servicio AR (mock) y botones | Interfaz `ServicioAR`, mock, botones demostrativos (RF-024) | servicios, pantalla propuesta |

## 9. Criterios de aceptación
Los de RF-021 a RF-024. Adicional: para el escenario demo, la vista superior y la frontal se generan sin datos ingresados a mano fuera de los pasos 1–4.

## 10. Estrategia de verificación
- Normal: demo (NFT-8, eje EO); `n = 5`; `n = 1`; variante DWC.
- Geometría (pruebas sobre el modelo, sin UI): todos los elementos dentro del invernadero (salvo tanques «exterior»); ningún solapamiento entre módulos; invernadero dentro del retiro.
- Ejes: repetir con eje de implantación `NS` y comprobar la transformación de coordenadas.
- Estados: `PRESUPUESTO_INSUFICIENTE` (1 módulo «referencia»), `SUPERFICIE_INSUFICIENTE` (`nMaxTerreno`), paso desactualizado (ofrece recalcular).
- Tanques > 3 (p. ej. `n = 12` en un terreno grande): los excedentes aparecen «exterior».
- AR: mock no disponible → mensaje y flujo intacto.
- Datos inconsistentes: alturas de módulo mayores a la lateral → `DATO_INCONSISTENTE`.

## Dependencias
- **Depende de**: `01`, `02`, `03`, `04`, `05`, `06`, `08`.
- **Es utilizado por**: nadie (salida final del flujo).
