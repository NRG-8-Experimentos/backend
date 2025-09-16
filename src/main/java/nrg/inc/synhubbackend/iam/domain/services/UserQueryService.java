package nrg.inc.synhubbackend.iam.domain.services;


import nrg.inc.synhubbackend.iam.domain.model.aggregates.User;
import nrg.inc.synhubbackend.iam.domain.model.queries.*;

import java.util.List;
import java.util.Optional;

public interface UserQueryService {
  List<User> handle(GetAllUsersQuery query);
  Optional<User> handle(GetUserByIdQuery query);
  Optional<User> handle(GetUserByUsernameQuery query);
  Optional<User> handle(GetUserByMemberId query);
  Optional<User> handle(GetUserByLeaderId query);
  List<User> handle(GetUsersByGroupIdQuery query);
}
