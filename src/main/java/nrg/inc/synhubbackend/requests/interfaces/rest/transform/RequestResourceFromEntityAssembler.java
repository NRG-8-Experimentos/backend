package nrg.inc.synhubbackend.requests.interfaces.rest.transform;

import nrg.inc.synhubbackend.requests.domain.model.aggregates.Request;
import nrg.inc.synhubbackend.requests.interfaces.rest.resources.RequestResource;
import nrg.inc.synhubbackend.tasks.interfaces.rest.transform.TaskResourceFromEntityAssembler;

public class RequestResourceFromEntityAssembler {
    public static RequestResource toResourceFromEntity(Request entity) {
        return new RequestResource(
                entity.getId(),
                entity.getDescription(),
                entity.getRequestType(),
                entity.getRequestStatus(),
                TaskResourceFromEntityAssembler.toResourceFromEntity(entity.getTask())
        );
    }
}
