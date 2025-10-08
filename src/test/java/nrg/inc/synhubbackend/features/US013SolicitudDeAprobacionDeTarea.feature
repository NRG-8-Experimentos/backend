Feature: Solicitud de aprobación de tarea
  Como miembro,
  quiero enviar tareas completadas
  para validación del líder.

  Scenario Outline: Envío estándar:
    Given que marco tarea como completada,
    When envío a validación,
    Then la tarea cambia a estado "Completada".
    Examples:
      | id tarea | nombre de Tarea               | estado inicial | acción realizada       | estado final |
      |     1    | Actualizar calendario semanal | En progreso    | marcar como completado | Completada   |
      |     2    | Revisar reporte de tareas     | En progreso    | marcar como completado | Completada   |
      |     3    | Subir documento final         | En progreso    | marcar como completado | Completada   |
