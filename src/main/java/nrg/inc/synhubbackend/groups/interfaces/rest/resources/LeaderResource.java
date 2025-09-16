package nrg.inc.synhubbackend.groups.interfaces.rest.resources;

public record LeaderResource(
        String username,
        String name,
        String surname,
        String imgUrl,
        String email,
        String averageSolutionTime,
        Integer solvedRequests
) {
}
