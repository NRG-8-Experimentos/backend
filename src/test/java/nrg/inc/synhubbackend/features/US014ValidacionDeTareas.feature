Feature: Validación de tareas
  Como líder,
  quiero aprobar o rechazar tareas completadas
  para asegurar calidad.

  Scenario Outline: Aprobación:
    Given que la tarea cumple estándares,
    When la apruebo,
    Then se marca como definitivamente completada.
    Examples:
      |  |

  Scenario Outline: Reprogramación:
    Given que encuentro deficiencias,
    When reprogramo la tarea,
    Then la tarea vuelve a "En progreso".
    Examples:
      |  |