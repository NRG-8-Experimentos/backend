package nrg.inc.synhubbackend.tasks.interfaces.rest.resources;

public record TaskMemberResource(
        Long id,
        String name,
        String surname,
        String urlImage
) {
}
