Feature: Eliminación de tareas
  Como líder,
  quiero eliminar tareas incorrectas o duplicadas
  para mantener la claridad en la gestión de proyectos.

  Scenario Outline: Eliminación estándar:
    Given que selecciono una tarea,
    When la elimino,
    Then desaparece del listado principal.
    Examples:
      |  |

  Scenario Outline:Tarea completada:
    Given que la tarea está marcada como completada,
    When intento eliminarla,
    Then el sistema requiere confirmación adicional.
    Examples:
      |  |