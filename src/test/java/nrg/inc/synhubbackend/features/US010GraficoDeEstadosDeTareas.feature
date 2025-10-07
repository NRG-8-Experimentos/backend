Feature: Gráfico de estados de tareas
  Como líder,
  quiero ver un gráfico con el estado de todas las tareas del grupo
  para monitorear el progreso.

  Scenario Outline: Datos actualizados:
    Given que hay tareas en diferentes estados,
    When visualizo el reporte,
    Then muestra cantidades de tareas por estado.
    Examples:
      |  |