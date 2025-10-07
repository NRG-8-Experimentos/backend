Feature: Visualización de carga de trabajo por miembro
  Como líder,
  quiero ver la carga de trabajo de cada miembro
  para equilibrar las asignaciones.

  Scenario Outline: Gráfico de barras:
    Given que accedo al panel de analítica,
    When observo su tiempo de tareas asignado,
    Then se muestra el tiempo total de tareas que tiene cada miembro.
    Examples:
      |  |

  Scenario Outline: Identificación de sobrecarga:
    Given que un miembro tiene más carga que otros,
    When visualizo los datos,
    Then se muestra claramente la diferencia.
    Examples:
      |  |