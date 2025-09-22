package nrg.inc.synhubbackend.iam.interfaces.rest.resources;

import java.util.List;

public record AuthenticatedUserResource(Long id, String username, String token, List<String> role) {
}
