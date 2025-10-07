Feature: Visualización de miembros del grupo
  Como líder,
  quiero ver la lista de miembros de mi grupo
  para gestionar la colaboración.

  Scenario Outline: Lista de miembros:
    Given que accedo a la sección de miembros,
    When visualizo la lista,
    Then se muestran los nombres y tareas próximas de cada miembro.
    Examples:
      | id miembro | nombre      | tarea próxima                      |
      |     1      | Ana Torres  | Crear banner principal             |
      |     2      | Luis García | Actualizar módulo de autenticación |
      |     3      | Carla Rivas | Revisar flujo de registro          |


  Scenario Outline: Detalles del miembro:
    Given que selecciono un miembro,
    When hago clic en su nombre,
    Then se muestran sus tareas asignadas y estado actual.
    Examples:
      | id miembro | nombre      | tareas asignadas                           |
      |     1      | Ana Torres  | Crear banner principal, Ajustar íconos     |
      |     2      | Luis García | Corregir errores del login, Documentar API |
      |     3      | Carla Rivas | Ejecutar pruebas del módulo de tareas      |
