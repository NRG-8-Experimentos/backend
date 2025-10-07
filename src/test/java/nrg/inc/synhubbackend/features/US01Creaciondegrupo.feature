Feature: US01 Creacion de grupos
  Como líder,
  quiero crear grupos
  para organizar el trabajo de mi equipo.

  Scenario Outline: Creacion Basica
    Given que soy líder
    When creo un grupo con nombre "<nombre>" y descripción "<descripcion>"
    Then el grupo se crea exitosamente con nombre "<nombre>", descripción "<descripcion>" y yo como su unico miembro
    Examples:
      | nombre   | descripcion              |
      | Equipo A | Grupo para el proyecto A |

  Scenario Outline: Imagen de grupo
    Given que ingreso el enlace de imagen "<imagen>"
    When guardo los cambios
    Then aparece como identificación visual del grupo
    Examples:
      | imagen |
      | https://static.wikia.nocookie.net/backyardigans/images/9/9d/The_backyardigans.jpg/revision/latest?cb=20200828182459&path-prefix=es   |