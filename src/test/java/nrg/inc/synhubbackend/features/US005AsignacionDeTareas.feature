Feature: Asignación de tareas
  Como líder,
  quiero asignar tareas a miembros específicos
  para distribuir el trabajo.

  Scenario Outline: Asignación individual:
    Given que selecciono un miembro,
    When asigno la tarea,
    Then aparece en su listado.
    Examples:
      | id de tarea | nombre de tarea           | miembro asignado | resultado esperado                      |
      | 1           | Redactar informe semanal  | Ana Torres       | La tarea aparece en el listado de Ana   |
      | 2           | Diseñar prototipo UI      | Luis Gomez       | La tarea se muestra en el panel de Luis |
      | 3           | Revisar metricas de grupo | Carla Ruiz       | Carla visualiza la nueva tarea asignada |


  Scenario Outline:Reasignación:
    Given que la tarea está asignada,
    When cambio el responsable,
    Then ambos miembros reciben una actualización en su listado de tareas.
    Examples:
      | id de tarea | tarea                   | miembro anterior | nuevo miembro | resultado esperado                           |
      | 1           | Actualizar calendario   | Pedro Diaz       | Ana Torres    | Pedro pierde la tarea y Ana la recibe        |
      | 2           | Revisar reporte mensual | Carla Ruiz       | Luis Gomez    | Carla deja de ver la tarea en su listado     |
      | 3           | Corregir errores QA     | Ana Torres       | Pedro Diaz    | La reasignacion se refleja en ambos listados |
