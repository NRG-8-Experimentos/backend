Feature: Envío de invitaciones
  Como miembro,
  quiero solicitar unirme a un grupo
  para poder trabajar.

  Scenario Outline: Invitación válida:
    Given que ingreso un código de grupo,
    When envío mi petición,
    Then el líder puede ver la invitación.
    Examples:
      |  |

  Scenario Outline: Cancelación de invitación:
    Given que tengo una invitación pendiente,
    When intento cancelarla,
    Then el sistema cancela la invitación.
    Examples:
      |  |