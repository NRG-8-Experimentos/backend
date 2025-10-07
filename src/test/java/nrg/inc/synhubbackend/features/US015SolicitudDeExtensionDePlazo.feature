Feature: Solicitud de extensión de plazo
  Como miembro,
  quiero pedir más tiempo
  para una tarea cuando surgen impedimentos.

  Scenario Outline: Solicitud básica:
    Given que selecciono una tarea,
    When envio un comentario pidiendo una extensión con motivo,
    Then el líder recibe las solicitud.
    Examples:
      | id tarea | nombre de tarea             | fecha límite original | motivo de solicitud              | estado solicitud |
      |     1    | Redactar informe mensual    | 2025-10-10            | Problemas de conexión a internet | Enviada          |
      |     2    | Diseñar banner publicitario | 2025-10-08            | Retraso por falta de recursos    | Enviada          |
      |     3    | Validar base de datos       | 2025-10-12            | Error técnico en servidor        | Enviada          |


  Scenario Outline: Aprobación:
    Given que el líder acepta,
    When actualiza la fecha,
    Then el sistema registra el cambio.
    Examples:
      | id tarea | nombre de tarea             | fecha límite original | nueva fecha límite | acción del líder | estado final |
      |     1    | Redactar informe mensual    | 2025-10-10            | 2025-10-13         | Aprobada         | En progreso  |
      |     2    | Diseñar banner publicitario | 2025-10-08            | 2025-10-11         | Aprobada         | En progreso  |
      |     3    | Validar base de datos       | 2025-10-12            | 2025-10-15         | Aprobada         | En progreso  |
