Feature: Reporte de reprogramaciones
  Como líder,
  quiero ver un gráfico de líneas con la cantidad de tareas reprogramadas
  para analizar patrones de reprogramación.

  Scenario Outline: Reprogramaciones de usuario:
    Given que me encuentro "Analiticas y Reportes",
    When miro la sección de miembros,
    Then muestra la cantidad de tareas reprogramadas que tiene.
    Examples:
      |  |

  Scenario Outline: Reprogramaciones total:
    Given que me encuentro "Analiticas y Reportes",
    When miro la sección de resumen,
    Then muestra la cantidad de tareas reprogramadas de todo el equipo.
    Examples:
      |  |