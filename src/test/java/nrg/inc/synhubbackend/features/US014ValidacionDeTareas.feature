Feature: Validación de tareas
  Como líder,
  quiero aprobar o rechazar tareas completadas
  para asegurar calidad.

  Scenario Outline: Aprobación:
    Given que la tarea cumple estándares,
    When la apruebo,
    Then se marca como definitivamente completada.
    Examples:
      | id tarea | nombre de tarea              | estado inicial | acción del líder | estado final |
      |     1    | Redactar informe mensual     | Completada     | Aprobar          | Completada   |
      |     2    | Actualizar base de contactos | Completada     | Aprobar          | Completada   |
      |     3    | Revisar plan semanal         | Completada     | Aprobar          | Completada   |

  Scenario Outline: Reprogramación:
    Given que encuentro deficiencias,
    When reprogramo la tarea,
    Then la tarea vuelve a "En progreso".
    Examples:
      | id tarea | nombre de tarea               | estado inicial | acción del líder | estado final |
      |     1    | Elaborar informe técnico      | Completada     | Errores en los datos           | En progreso  |
      |     2    | Diseñar presentación final    | Completada     | Falta de material gráfico      | En progreso  |
      |     3    | Revisar documentación interna | Completada     | Requiere correcciones formales | En progreso  |
