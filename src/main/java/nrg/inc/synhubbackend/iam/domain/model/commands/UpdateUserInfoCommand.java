package nrg.inc.synhubbackend.iam.domain.model.commands;

public record UpdateUserInfoCommand(
        String name,
        String surname,
        String urlImage,
        String email
) {
}
