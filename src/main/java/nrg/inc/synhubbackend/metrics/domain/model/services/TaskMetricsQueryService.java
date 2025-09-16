package nrg.inc.synhubbackend.metrics.domain.model.services;

import nrg.inc.synhubbackend.metrics.domain.model.queries.*;
import nrg.inc.synhubbackend.metrics.interfaces.rest.resources.*;

public interface TaskMetricsQueryService {
    TaskTimePassedResource handle(GetTaskTimePassedQuery query);
    AvgCompletionTimeResource handle(GetAvgCompletionTimeQuery query);
    RescheduledTasksResource handle(GetRescheduledTasksQuery query);
    TaskDistributionResource handle(GetTaskDistributionQuery query);
    TaskOverviewResource handle(GetTaskOverviewQuery query);
    TaskOverviewResource handle(GetTaskOverviewForMemberQuery query);
    TaskDistributionResource handle(GetTaskDistributionForMemberQuery query);
    RescheduledTasksResource handle(GetRescheduledTasksForMemberQuery query);
    AvgCompletionTimeResource handle(GetAvgCompletionTimeForMemberQuery query);
}