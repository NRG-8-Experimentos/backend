Feature: Reprogramación de tareas
  Como líder,
  quiero cambiar fechas límite cuando surgen imprevistos
  para mantener el proyecto en marcha.

  Scenario Outline: Cambio de fecha:
    Given que selecciono una tarea,
    When modificó la fecha límite,
    Then el responsable puede ver el nuevo plazo.
    Examples:
      |  |