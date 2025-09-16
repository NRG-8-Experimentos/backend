package nrg.inc.synhubbackend.shared.application.external.outboundedservices;

import nrg.inc.synhubbackend.iam.domain.model.aggregates.User;
import nrg.inc.synhubbackend.iam.interfaces.acl.IamContextFacade;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ExternalIamService {
    private final IamContextFacade iamContextFacade;

    public ExternalIamService(IamContextFacade iamContextFacade) {
        this.iamContextFacade = iamContextFacade;
    }

    public Optional<User> getUserByUsername(String username) {
        var userId = this.iamContextFacade.fetchUserIdByUsername(username);

        if (userId == null || userId == 0L) {
            throw new IllegalArgumentException("User not found for username: " + username);
        }
        var user = this.iamContextFacade.fetchUserById(userId);
        if (user.isEmpty()) {
            throw new IllegalArgumentException("User not found for userId: " + userId);
        }
        return user;
    }
}
