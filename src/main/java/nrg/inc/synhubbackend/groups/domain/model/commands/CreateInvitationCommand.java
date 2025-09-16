package nrg.inc.synhubbackend.groups.domain.model.commands;

public record CreateInvitationCommand(
        Long memberId,
        Long groupId
) {
}
