Feature: Asignación de tareas
  Como líder,
  quiero asignar tareas a miembros específicos
  para distribuir el trabajo.

  Scenario Outline: Asignación individual:
    Given que selecciono un miembro,
    When asigno la tarea,
    Then aparece en su listado.
    Examples:
      |  |

  Scenario Outline:Reasignación:
    Given que la tarea está asignada,
    When cambio el responsable,
    Then ambos miembros reciben una actualización en su listado de tareas.
    Examples:
      |  |