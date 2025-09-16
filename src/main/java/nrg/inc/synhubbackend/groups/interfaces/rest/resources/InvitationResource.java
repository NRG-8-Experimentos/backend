package nrg.inc.synhubbackend.groups.interfaces.rest.resources;

public record InvitationResource(
        Long id,
        InvitationMemberResource member,
        GroupResource group
) {
}
