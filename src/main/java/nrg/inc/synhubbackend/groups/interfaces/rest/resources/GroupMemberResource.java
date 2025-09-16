package nrg.inc.synhubbackend.groups.interfaces.rest.resources;

public record GroupMemberResource(
        Long id,
        String username,
        String name,
        String surname,
        String imgUrl
) {
}
