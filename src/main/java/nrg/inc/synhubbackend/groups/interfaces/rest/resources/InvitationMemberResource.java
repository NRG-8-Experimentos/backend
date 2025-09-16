package nrg.inc.synhubbackend.groups.interfaces.rest.resources;

public record InvitationMemberResource(
        Long id,
        String username,
        String name,
        String surname,
        String imgUrl
) {
}
