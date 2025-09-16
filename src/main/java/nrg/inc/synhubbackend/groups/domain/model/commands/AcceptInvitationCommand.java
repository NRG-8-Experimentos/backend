package nrg.inc.synhubbackend.groups.domain.model.commands;

public record AcceptInvitationCommand(
        Long leaderId,
        Long invitationId
) {
}
