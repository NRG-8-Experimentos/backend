package nrg.inc.synhubbackend.metrics.interfaces.rest.resources;

import java.util.Map;

public record TaskDistributionResource(
        String type,
        int value,
        Map<String, MemberTaskInfo> details
) {}
