package nrg.inc.synhubbackend.iam.interfaces.rest.transform;

import nrg.inc.synhubbackend.iam.domain.model.commands.SignInCommand;
import nrg.inc.synhubbackend.iam.interfaces.rest.resources.SignInResource;

public class SignInCommandFromResourceAssembler {

  public static SignInCommand toCommandFromResource(SignInResource signInResource) {
    return new SignInCommand(signInResource.username(), signInResource.password());
  }
}
