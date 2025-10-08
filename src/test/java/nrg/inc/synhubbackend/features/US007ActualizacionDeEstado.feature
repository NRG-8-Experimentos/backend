Feature: Actualización de estado
  Como miembro,
  quiero actualizar el estado de mis tareas
  para reflejar mi progreso.

  Scenario Outline: Marcar como completada
    Given que finalizó una tarea,
    When cambio el estado,
    Then el líder recibe una solicitud de validación.
    Examples:
      | id de tarea | nombre de tarea             | estado anterior | estado nuevo | notificación al líder                                   |
      |  1          | Redactar informe semanal    | En progreso     | Completada   | “El miembro Juan Pérez marcó la tarea como completada.” |
      |  2          | Diseñar banner del grupo    | En progreso     | Completada   | “La tarea fue completada y requiere validación.”        |
      |  3          | Revisar lista de pendientes | En progreso     | Completada   | “Nueva solicitud de validación recibida.”               |

