Feature: Confirmación antes de eliminar una tarea
  Como líder,
  quiero recibir una confirmación antes de eliminar una tarea
  para evitar borrados accidentales.

  Scenario Outline: Confirmación requerida:
    Given que selecciono eliminar una tarea,
    When hago clic en el icono de eliminar,
    Then se muestra un mensaje de confirmación antes de proceder.
    Examples:
      |  |

  Scenario Outline: Cancelación de eliminación:
    Given que aparece el mensaje de confirmación,
    When elijo "Cancelar",
    Then la tarea no se elimina y permanece en la lista.
    Examples:
      |  |