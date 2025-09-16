package nrg.inc.synhubbackend.groups.interfaces.rest.resources;

public record CreateGroupResource(
        String name,
        String imgUrl,
        String description
) {
}
