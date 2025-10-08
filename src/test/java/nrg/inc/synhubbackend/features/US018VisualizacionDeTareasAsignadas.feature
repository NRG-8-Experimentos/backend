Feature: Visualización de tareas asignadas
  Como miembro,
  quiero ver las tareas que me han sido asignadas
  para gestionar mi trabajo

  Scenario Outline: Lista de tareas:
    Given que accedo a mi panel de tareas,
    When visualizo la lista,
    Then se muestran todas las tareas asignadas con su estado actual.
    Examples:
      | id tarea | nombre de la tarea                        | fecha límite | estado      |
      |     1    | Revisar documentación del proyecto        | 2025-10-12   | En progreso |
      |     2    | Implementar validaciones del formulario   | 2025-10-15   | Pendiente   |
      |     3    | Corregir errores en el módulo de reportes | 2025-10-09   | Completada  |


  Scenario Outline: Detalle de tarea:
    Given que selecciono una tarea,
    When hago clic en ella,
    Then se muestran los detalles completos de la tarea.
    Examples:
      | id tarea | nombre de la tarea                        | descripción                                                         | Fecha Límite | Estado      |
      |     2    | Implementar validaciones del formulario   | Añadir validaciones de campos requeridos en el registro de usuarios | 2025-10-15   | En Progreso |
      |     3    | Corregir errores en el módulo de reportes | Resolver errores de visualización en el dashboard                   | 2025-10-09   | Completada  |
