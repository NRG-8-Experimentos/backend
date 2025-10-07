Feature: Actualización de estado
  Como miembro,
  quiero actualizar el estado de mis tareas
  para reflejar mi progreso.

  Scenario Outline: Marcar como completada
    Given que finalizó una tarea,
    When cambio el estado,
    Then el líder recibe una solicitud de validación.
    Examples:
      |  |
