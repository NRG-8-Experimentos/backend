Feature: Gráfica de distribución de tareas
  Como líder,
  quiero ver un gráfico con la distribución de tareas por miembro
  para balancear cargas.

  Scenario Outline: Visualización básica:
    Given que accedo al dashboard,
    When selecciono "Analiticas y Reportes",
    Then muestra la cantidad de tareas por miembro.
    Examples:
      | grupo   | miembro        | tareas asignadas | tareas completadas | tamaño de carga     | tipo de gráfico | estado visualización   |
      | Grupo A | Ana Torres     | 8                | 6                  | 25%                 | Barras          | Mostrado correctamente |
      | Grupo A | Luis Gómez     | 12               | 9                  | 35%                 | Barras          | Mostrado correctamente |
      | Grupo A | Marta Ruiz     | 10               | 7                  | 30%                 | Barras          | Mostrado correctamente |
      | Grupo A | José Fernández | 4                | 2                  | 10%                 | Barras          | Mostrado correctamente |