package nrg.inc.synhubbackend.groups.domain.model.commands;

public record RemoveMemberFromGroupCommand(Long leaderId, Long memberId) {
}
