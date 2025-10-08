Feature: Revisión de comentarios en tareas
  Como líder,
  quiero revisar los comentarios en las tareas
  para proporcionar retroalimentación oportuna.

  Scenario Outline: Acceso a comentarios:
    Given que accedo a una tarea,
    When visualizo la sección de comentarios,
    Then puedo leer todos los mensajes dejados por los miembros.
    Examples:
      | id tarea | nombre de tarea       | comentarios existentes                | Resultado Esperado                                     |
      |     5    | Actualizar reporte UX | “Necesito aclarar el flujo del login” | Se muestra el comentario del miembro en la lista       |
      |     2    | Revisar diseño mobile | “El botón no se ve en pantalla chica” | El líder puede leer el mensaje en la sección de tareas |
      |     3    | Integrar API de pagos | “Hay error al conectar con endpoint”  | El comentario aparece con autor correspondiente        |


  Scenario Outline: Solución a comentarios:
    Given que leo un comentario,
    When decido si reprogramar o eliminar,
    Then el miembro puede ver los cambios hechos en la tarea.
    Examples:
      | id tarea | acción tomada     | resultado esperado                                              |
      |     5    | Reprogramar fecha | El miembro ve la nueva fecha y recibe notificación              |
      |     2    | Eliminar tarea    | El miembro recibe alerta de eliminación de la tarea             |
      |     3    | Mantener tarea    | No hay cambios visibles, el comentario se marca como “revisado” |
