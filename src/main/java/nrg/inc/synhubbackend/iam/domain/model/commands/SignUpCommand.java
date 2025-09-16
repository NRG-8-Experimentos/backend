package nrg.inc.synhubbackend.iam.domain.model.commands;

import nrg.inc.synhubbackend.iam.domain.model.entities.Role;

import java.util.List;

public record SignUpCommand(
        String username,
        String name,
        String surname,
        String imgUrl,
        String email,
        String password,
        List<Role> roles) {
}
