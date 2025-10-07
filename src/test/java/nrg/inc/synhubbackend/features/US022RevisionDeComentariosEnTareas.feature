Feature: Revisión de comentarios en tareas
  Como líder,
  quiero revisar los comentarios en las tareas
  para proporcionar retroalimentación oportuna.

  Scenario Outline: Acceso a comentarios:
    Given que accedo a una tarea,
    When visualizo la sección de comentarios,
    Then puedo leer todos los mensajes dejados por los miembros.
    Examples:
      |  |

  Scenario Outline: Solución a comentarios:
    Given que leo un comentario,
    When decido si reprogramar o eliminar,
    Then el miembro puede ver los cambios hechos en la tarea.
    Examples:
      |  |