Feature: Comentario en tareas
  Como miembro,
  quiero comentar en las tareas
  para comunicarme con el líder sobre el progreso.

  Scenario Outline: Añadir comentario:
    Given Given que accedo a una tarea asignada,
    When escribo un comentario y lo envío,
    Then el comentario aparece como una solicitud.
    Examples:
      |  |

  Scenario Outline: Visualización del líder:
    Given que envío un comentario,
    When el líder accede a “Solicitude y validaciones”,
    Then ve el nuevo comentario.
    Examples:
      |  |