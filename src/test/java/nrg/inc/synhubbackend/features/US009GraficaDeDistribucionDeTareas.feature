Feature: Gráfica de distribución de tareas
  Como líder,
  quiero ver un gráfico con la distribución de tareas por miembro
  para balancear cargas.

  Scenario Outline: Visualización básica:
    Given que accedo al dashboard,
    When selecciono "Analiticas y Reportes",
    Then muestra la cantidad de tareas por miembro.
    Examples:
      |  |