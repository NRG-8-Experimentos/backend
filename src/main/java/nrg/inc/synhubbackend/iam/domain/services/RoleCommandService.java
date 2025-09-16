package nrg.inc.synhubbackend.iam.domain.services;


import nrg.inc.synhubbackend.iam.domain.model.commands.SeedRolesCommand;

public interface RoleCommandService {
  void handle(SeedRolesCommand command);
}
