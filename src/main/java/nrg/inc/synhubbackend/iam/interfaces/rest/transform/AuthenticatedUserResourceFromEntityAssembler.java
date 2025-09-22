package nrg.inc.synhubbackend.iam.interfaces.rest.transform;


import nrg.inc.synhubbackend.iam.domain.model.aggregates.User;
import nrg.inc.synhubbackend.iam.domain.model.entities.Role;
import nrg.inc.synhubbackend.iam.interfaces.rest.resources.AuthenticatedUserResource;

public class AuthenticatedUserResourceFromEntityAssembler {

  public static AuthenticatedUserResource toResourceFromEntity(User user, String token) {
      var roles = user.getRoles().stream()
              .map(Role::getStringName)
              .toList();
      return new AuthenticatedUserResource(user.getId(), user.getUsername(), token, roles);
  }
}
