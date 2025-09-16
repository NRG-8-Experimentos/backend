package nrg.inc.synhubbackend.groups.domain.model.commands;

public record RejectInvitationCommand(
        Long leaderId,
        Long invitationId
) {
}
