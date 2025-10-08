Feature: Gráfico de estados de tareas
  Como líder,
  quiero ver un gráfico con el estado de todas las tareas del grupo
  para monitorear el progreso.

  Scenario Outline: Datos actualizados:
    Given que hay tareas en diferentes estados,
    When visualizo el reporte,
    Then muestra cantidades de tareas por estado.
    Examples:
      | grupo   | tareas pendientes | tareas en progreso | tareas completadas | Tipo de gráfico | Estado visualización   |
      | Grupo A | 5                 | 8                  | 12                 | cards númericas | Mostrado correctamente |
      | Grupo B | 3                 | 6                  | 9                  | cards númericas | Mostrado correctamente |
      | Grupo C | 7                 | 4                  | 10                 | cards númericas | Mostrado correctamente |
