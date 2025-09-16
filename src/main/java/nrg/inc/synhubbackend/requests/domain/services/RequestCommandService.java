package nrg.inc.synhubbackend.requests.domain.services;

import nrg.inc.synhubbackend.requests.domain.model.aggregates.Request;
import nrg.inc.synhubbackend.requests.domain.model.commands.CreateRequestCommand;
import nrg.inc.synhubbackend.requests.domain.model.commands.DeleteAllRequestsByTaskIdCommand;
import nrg.inc.synhubbackend.requests.domain.model.commands.DeleteRequestCommand;
import nrg.inc.synhubbackend.requests.domain.model.commands.UpdateRequestCommand;

import java.util.Optional;

public interface RequestCommandService {
    Long handle(CreateRequestCommand command);
    Optional<Request> handle(UpdateRequestCommand command);
    void handle(DeleteRequestCommand command);
    void handle(DeleteAllRequestsByTaskIdCommand command);
}
