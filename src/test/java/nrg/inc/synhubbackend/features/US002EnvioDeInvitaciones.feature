Feature: Envío de invitaciones
  Como miembro,
  quiero solicitar unirme a un grupo
  para poder trabajar.

  Scenario Outline: Invitación válida:
    Given que ingreso un código de grupo,
    When envío mi petición,
    Then el líder puede ver la invitación.
    Examples:
      | codigo     | resultado                                    |
      | GRPAFD123  | Invitacion enviada correctamente             |
      | TESVAM456  | Solicitud registrada y visible para el lider |
      | FAVSFM789  | Peticion en espera de aprobacion             |

  Scenario Outline: Cancelación de invitación:
    Given que tengo una invitación pendiente,
    When intento cancelarla,
    Then el sistema cancela la invitación.
    Examples:
      | id de invitacion| Resultado esperado                         |
      | INV1WE001       | Invitacion cancelada correctamente         |
      | INV1WE045       | Invitacion retirada antes de aprobacion    |
      | INV1WE078       | Invitacion eliminada de la lista del lider |
