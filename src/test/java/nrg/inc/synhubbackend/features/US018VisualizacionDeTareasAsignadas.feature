Feature: Visualización de tareas asignadas
  Como miembro,
  quiero ver las tareas que me han sido asignadas
  para gestionar mi trabajo

  Scenario Outline: Lista de tareas:
    Given que accedo a mi panel de tareas,
    When visualizo la lista,
    Then se muestran todas las tareas asignadas con su estado actual.
    Examples:
      |  |

  Scenario Outline: Detalle de tarea:
    Given que selecciono una tarea,
    When hago clic en ella,
    Then se muestran los detalles completos de la tarea.
    Examples:
      |  |