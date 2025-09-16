package nrg.inc.synhubbackend.groups.domain.model.commands;

public record UpdateGroupCommand(
        Long leaderId,
        String name,
        String description,
        String imgUrl
) {
}
