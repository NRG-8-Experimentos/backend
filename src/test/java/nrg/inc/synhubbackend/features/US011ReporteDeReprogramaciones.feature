Feature: Reporte de reprogramaciones
  Como líder,
  quiero ver un gráfico de líneas con la cantidad de tareas reprogramadas
  para analizar patrones de reprogramación.

  Scenario Outline: Reprogramaciones de usuario:
    Given que me encuentro "Analiticas y Reportes",
    When miro la sección de miembros,
    Then muestra la cantidad de tareas reprogramadas que tiene.
    Examples:
      | miembro     | tareas reprogramadas | gráfico mostrado | estado visualización   |
      | Ana Torres  | 3                    | gráfico de barra | Mostrado correctamente |
      | Luis Ramos  | 5                    | gráfico de barra | Mostrado correctamente |
      | Sofía Pérez | 1                    | gráfico de barra | Mostrado correctamente |


  Scenario Outline: Reprogramaciones total:
    Given que me encuentro "Analiticas y Reportes",
    When miro la sección de resumen,
    Then muestra la cantidad de tareas reprogramadas de todo el equipo.
    Examples:
      | miembro | tareas reprogramadas | gráfico mostrado | estado visualización   |
      | grupo A | 10                   | gráfico de barra | Mostrado correctamente |
      | grupo B | 23                   | gráfico de barra | Mostrado correctamente |
      | grupo C | 7                    | gráfico de barra | Mostrado correctamente |