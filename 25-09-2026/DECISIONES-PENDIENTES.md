# Decisiones pendientes

Estas son las decisiones que el equipo debería tomar antes o durante la implementación. No se resuelven automáticamente en los specs porque afectan el comportamiento o el alcance del prototipo, no son detalles de implementación libres.

1. **Compatibilidad cultivo/sistema (spec 02, RN-005)**: ¿la advertencia de incompatibilidad bloquea el avance o solo informa?
2. **Costo de instalación y mano de obra (spec 05, RN-016)**: ¿se modela como porcentaje de la suma de componentes o como monto fijo según escala? Definir la regla y sus valores mock.
3. **Modalidad "planificación según objetivo" (spec 05, RF-016)**: confirmar si se implementa completa para el hackatón o de forma simplificada/simulada (el prompt maestro permite esto último).
4. **Horizonte temporal de la proyección de TCO (spec 06, RN-020 / spec 10)**: definir si es de 5 o 10 años.
5. **Numeración de criterios de aceptación**: si se mantiene una numeración global (`CA-01`, `CA-02`, ...) como en estos specs, o se prefiere una numeración por módulo (`CA-01.1`, `CA-02.1`, etc.).
6. **Proyectos de demostración**: ¿son editables por el usuario durante la demo o se tratan como "solo lectura / ejemplo"?
7. **AR (spec 09)**: confirmar si se implementa una demostración mínima real o solo un botón placeholder con mensaje.
8. **Persistencia**: confirmar si el prototipo usa una base de datos simple (ej. SQLite/archivo JSON) o solo estado en memoria/localStorage para la demo del hackatón.
9. **Tecnología**: sin definir todavía (frontend, backend, base de datos, hosting). A decidir recién después de validar estos specs, según el principio "requerimientos → arquitectura lógica → necesidades técnicas → tecnología" (ver prompt maestro).

Ninguna de estas decisiones bloquea el desarrollo de los módulos ya especificados; cada spec funciona con un valor razonable por defecto donde corresponde, dejando la decisión final documentada acá para que el equipo la resuelva quirúrgicamente.
