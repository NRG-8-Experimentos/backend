Feature: Creación de tareas
  Como líder,
  quiero crear tareas
  para asignar trabajo a los miembros.

  Scenario Outline: Tarea básica:
    Given que completo título y descripción,
    When guardo la tarea,
    Then aparece en el listado con estado "En progreso".
    Examples:
      |  |

  Scenario Outline: Tarea sin responsable:
    Given que creo tarea sin asignar,
    When intento guardar,
    Then el sistema muestra error "Se requiere responsable".
    Examples:
      |  |