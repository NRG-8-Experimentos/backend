Feature: Comentario en tareas
  Como miembro,
  quiero comentar en las tareas
  para comunicarme con el líder sobre el progreso.

  Scenario Outline: Añadir comentario:
    Given Given que accedo a una tarea asignada,
    When escribo un comentario y lo envío,
    Then el comentario aparece como una solicitud.
    Examples:
      | id tarea | Comentario                                        | Usuario    | Resultado Esperado                               |
      |     5    | “He completado el 80% del desarrollo.”            | Juan Pérez | Comentario visible desde el panel de solicitudes |
      |     6    | “Necesito confirmación sobre los requerimientos.” | Ana Torres | Solicitud registrada en el panel del líder       |


  Scenario Outline: Visualización del líder:
    Given que envío un comentario,
    When el líder accede a “Solicitude y validaciones”,
    Then ve el nuevo comentario.
    Examples:
      | id comentario | tarea relacionada                | usuario Emisor | resultado esperado                      |
      |       1       | Revisar documentación del módulo | Juan Pérez     | Se muestra en la bandeja de solicitudes |
      |       2       | Validar interfaz de usuario      | Ana Torres     | Comentario visible en detalle de tarea  |
