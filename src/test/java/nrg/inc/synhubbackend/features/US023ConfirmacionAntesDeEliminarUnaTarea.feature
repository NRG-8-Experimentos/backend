Feature: Confirmación antes de eliminar una tarea
  Como líder,
  quiero recibir una confirmación antes de eliminar una tarea
  para evitar borrados accidentales.

  Scenario Outline: Confirmación requerida:
    Given que selecciono eliminar una tarea,
    When hago clic en el icono de eliminar,
    Then se muestra un mensaje de confirmación antes de proceder.
    Examples:
      | id tarea | nombre de tarea         | mensaje mostrado                                 | acción esperada                 |
      |     1    | Revisar contenido FAQ   | “¿Está seguro de que desea eliminar esta tarea?” | Mostrar ventana de confirmación |
      |     8    | Actualizar landing page | “Confirmar eliminación de tarea seleccionada”    | Mostrar mensaje emergente       |
      |     9    | Corregir error en login | “¿Desea eliminar esta tarea definitivamente?”    | Requiere acción del usuario     |


  Scenario Outline: Cancelación de eliminación:
    Given que aparece el mensaje de confirmación,
    When elijo "Cancelar",
    Then la tarea no se elimina y permanece en la lista.
    Examples:
      | id tarea | acción seleccionada | resultado esperado                                     |
      |     1    | Cancelar            | La tarea sigue visible en el listado principal         |
      |     8    | Cancelar            | No se ejecuta ningún cambio en la base de datos        |
      |     9    | Cancelar            | La ventana de confirmación se cierra sin eliminar nada |
