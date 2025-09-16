package nrg.inc.synhubbackend.iam.interfaces.acl;

import nrg.inc.synhubbackend.iam.domain.model.aggregates.User;

import java.util.List;
import java.util.Optional;

/**
 * IamContextFacade
 * <p>
 *     This class is a facade for the IAM context. It provides a simple interface for other
 *     bounded contexts to interact with the
 *     IAM context.
 *     This class is a part of the ACL layer.
 * </p>
 *
 */
public interface IamContextFacade {

  Long createUser(String username, String name, String surname, String imgUrl, String email, String password);
  Long createUser(String username, String name, String surname, String imgUrl, String email, String password, List<String> roleNames);
  Long fetchUserIdByUsername(String username);
  String fetchUsernameByUserId(Long userId);
  Optional<User> fetchUserByMemberId(Long memberId);
  Optional<User> fetchUserByLeaderId(Long leaderId);
  Optional<User> fetchUserById(Long userId);
  List<User> fetchUsersByGroupId(Long groupId);
}
