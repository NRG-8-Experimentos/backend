package nrg.inc.synhubbackend.groups.domain.model.commands;

public record CancelInvitationCommand(
        Long memberId,
        Long invitationId
) {
}
