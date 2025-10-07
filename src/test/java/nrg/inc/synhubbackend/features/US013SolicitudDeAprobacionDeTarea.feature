Feature: Solicitud de aprobación de tarea
  Como miembro,
  quiero enviar tareas completadas
  para validación del líder.

  Scenario Outline: Envío estándar:
    Given que marco tarea como completada,
    When envío a validación,
    Then la tarea cambia a estado "Completada".
    Examples:
      |  |