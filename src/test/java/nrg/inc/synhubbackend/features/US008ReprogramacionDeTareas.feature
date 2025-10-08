Feature: Reprogramación de tareas
  Como líder,
  quiero cambiar fechas límite cuando surgen imprevistos
  para mantener el proyecto en marcha.

  Scenario Outline: Cambio de fecha:
    Given que selecciono una tarea,
    When modificó la fecha límite,
    Then el responsable puede ver el nuevo plazo.
    Examples:
      | id de tarea | nombre de tarea                  | fecha anterior | nueva fecha | responsable | vista del miembro                                     |
      |  1          | Actualizar documentación interna | 05/10/2025     | 10/10/2025  | Ana Torres  | La fecha límite de la tarea cambio en el listado      |
      |  2          | Diseñar logo del grupo           | 08/10/2025     | 12/10/2025  | Luis Gómez  | La fecha ha cambiado                                  |
      |  3          | Revisar entregables del sprint   | 06/10/2025     | 09/10/2025  | Marta Ruiz  | Se extendió la fecha límite de la tarea al 09/10/2025.|
