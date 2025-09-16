package nrg.inc.synhubbackend.iam.domain.model.queries;


import nrg.inc.synhubbackend.iam.domain.model.valueobjects.Roles;

public record GetRoleByNameQuery(Roles name) {
}
