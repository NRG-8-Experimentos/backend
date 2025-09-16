package nrg.inc.synhubbackend.iam.domain.services;


import nrg.inc.synhubbackend.iam.domain.model.entities.Role;
import nrg.inc.synhubbackend.iam.domain.model.queries.GetAllRolesQuery;
import nrg.inc.synhubbackend.iam.domain.model.queries.GetRoleByNameQuery;

import java.util.List;
import java.util.Optional;

public interface RoleQueryService {
  List<Role> handle(GetAllRolesQuery query);
  Optional<Role> handle(GetRoleByNameQuery query);
}
