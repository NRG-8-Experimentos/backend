Feature: Solicitud de extensión de plazo
  Como miembro,
  quiero pedir más tiempo
  para una tarea cuando surgen impedimentos.

  Scenario Outline: Solicitud básica:
    Given que selecciono una tarea,
    When envio un comentario pidiendo una extensión con motivo,
    Then el líder recibe las solicitud.
    Examples:
      |  |

  Scenario Outline: Aprobación:
    Given que el líder acepta,
    When actualiza la fecha,
    Then el sistema registra el cambio.
    Examples:
      |  |