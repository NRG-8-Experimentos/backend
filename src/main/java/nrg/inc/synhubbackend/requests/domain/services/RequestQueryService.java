package nrg.inc.synhubbackend.requests.domain.services;

import nrg.inc.synhubbackend.requests.domain.model.aggregates.Request;
import nrg.inc.synhubbackend.requests.domain.model.queries.GetAllRequestsQuery;
import nrg.inc.synhubbackend.requests.domain.model.queries.GetRequestByIdQuery;
import nrg.inc.synhubbackend.requests.domain.model.queries.GetRequestsByTaskIdQuery;

import java.util.List;
import java.util.Optional;

public interface RequestQueryService {
    List<Request> handle(GetAllRequestsQuery query);
    List<Request> handle(GetRequestsByTaskIdQuery query);
    Optional<Request> handle(GetRequestByIdQuery query);
}
