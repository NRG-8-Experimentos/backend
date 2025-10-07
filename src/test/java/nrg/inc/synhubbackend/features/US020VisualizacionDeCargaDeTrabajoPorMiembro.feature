Feature: Visualización de carga de trabajo por miembro
  Como líder,
  quiero ver la carga de trabajo de cada miembro
  para equilibrar las asignaciones.

  Scenario Outline: Gráfico de barras:
    Given que accedo al panel de analítica,
    When observo su tiempo de tareas asignado,
    Then se muestra el tiempo total de tareas que tiene cada miembro.
    Examples:
      | id miembro | nombre      | tareas asignadas | horas totales | Resultado Esperado                      |
      |     1      | Juan Pérez  | 5                | 20 h          | El número muestra 20 h para Juan Pérez  |
      |     2      | Ana Torres  | 3                | 12 h          | El número muestra 12 h para Ana Torres  |
      |     3      | Luis Romero | 6                | 24 h          | El número muestra 24 h para Luis Romero |


  Scenario Outline: Identificación de sobrecarga:
    Given que un miembro tiene más carga que otros,
    When visualizo los datos,
    Then se muestra claramente la diferencia.
    Examples:
      | id miembro sobrecargado | nombre       | horas totales | promedio Equipo | resultado esperado                               |
      |           3             | Luis Romero  | 24 h          | 15 h            | El sistema resalta al miembro con sobrecarga     |
      |           5             | Carla Medina | 28 h          | 16 h            | Se muestra advertencia de desequilibrio de carga |
