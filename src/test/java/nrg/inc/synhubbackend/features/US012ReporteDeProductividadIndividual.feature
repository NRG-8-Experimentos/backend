Feature: Reporte de productividad individual
  Como líder,
  quiero evaluar el desempeño de cada miembro a través de métricas claras
  para optimizar la asignación de tareas y mejorar la eficiencia del equipo.

  Scenario Outline: Datos básicos:
    Given que me encuentro "Analiticas y Reportes",
    When examino los datos de un miembro
    Then muestra: tareas completadas/tiempo promedio.
    Examples:
      |  |

  Scenario Outline: Comparativa:
    Given que veo dos miembros,
    When los comparo,
    Then resalta diferencias significativas.
    Examples:
      |  |