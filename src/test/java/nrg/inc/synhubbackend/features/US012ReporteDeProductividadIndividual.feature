Feature: Reporte de productividad individual
  Como líder,
  quiero evaluar el desempeño de cada miembro a través de métricas claras
  para optimizar la asignación de tareas y mejorar la eficiencia del equipo.

  Scenario Outline: Datos básicos:
    Given que me encuentro "Analiticas y Reportes",
    When examino los datos de un miembro
    Then muestra: tareas completadas/tiempo promedio.
    Examples:
      | miembro    | tareas Completadas | tiempo Promedio (h) |
      | Ana Pérez  | 25                 | 1.8                 |
      | Luis Gómez | 32                 | 2.3                 |
      | Marta Ríos | 18                 | 1.2                 |

  Scenario Outline: Comparativa:
    Given que veo dos miembros,
    When los comparo,
    Then resalta diferencias significativas.
    Examples:
      | miembro A  | tareas Completadas A | tiempo Promedio A (h) | miembro B  | tareas Completadas B | tiempo Promedio B (h) | diferencia destacada        |
      | Ana Pérez  | 25                   | 1.8                   | Luis Gómez | 32                   | 2.3                   | Mayor volumen de tareas     |
      | Marta Ríos | 18                   | 1.2                   | Ana Pérez  | 25                   | 1.8                   | Mejor tiempo promedio       |
      | Luis Gómez | 32                   | 2.3                   | Marta Ríos | 18                   | 1.2                   | Diferencia en productividad |
