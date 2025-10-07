Feature: Visualización de miembros del grupo
  Como líder,
  quiero ver la lista de miembros de mi grupo
  para gestionar la colaboración.

  Scenario Outline: Lista de miembros:
    Given que accedo a la sección de miembros,
    When visualizo la lista,
    Then se muestran los nombres y tareas próximas de cada miembro.
    Examples:
      |  |

  Scenario Outline: Detalles del miembro:
    Given que selecciono un miembro,
    When hago clic en su nombre,
    Then se muestran sus tareas asignadas y estado actual.
    Examples:
      |  |