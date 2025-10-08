Feature: Eliminación de tareas
  Como líder,
  quiero eliminar tareas incorrectas o duplicadas
  para mantener la claridad en la gestión de proyectos.

  Scenario Outline: Eliminación estándar:
    Given que selecciono una tarea,
    When la elimino,
    Then desaparece del listado principal.
    Examples:
      | id de tarea | nombre de tarea           | estado      | resultado esperado                                |
      |  1          | Revision duplicada de UI  | En progreso | La tarea se elimina del listado principal         |
      |  2          | Crear documento de prueba | En progreso | La tarea desaparece del panel de tareas activas   |
      |  3          | Validar datos duplicados  | En progreso | Se actualiza la vista y la tarea ya no se muestra |

  Scenario Outline:Tarea completada:
    Given que la tarea está marcada como completada,
    When intento eliminarla,
    Then el sistema requiere confirmación adicional.
    Examples:
      | id de tarea | nombre de tarea         | estado     | mensaje de confirmación esperado                           |
      |  1          | Generar informe final   | Completada | “¿Desea eliminar una tarea completada?”                    |
      |  2          | Subir entrega del grupo | Completada | “Confirmar eliminación: esta acción no se puede deshacer.” |
      |  3          | Cerrar sprint semanal   | Completada | “¿Está seguro de eliminar esta tarea finalizada?”          |
