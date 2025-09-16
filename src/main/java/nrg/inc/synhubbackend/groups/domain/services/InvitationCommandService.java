package nrg.inc.synhubbackend.groups.domain.services;

import nrg.inc.synhubbackend.groups.domain.model.aggregates.Invitation;
import nrg.inc.synhubbackend.groups.domain.model.commands.AcceptInvitationCommand;
import nrg.inc.synhubbackend.groups.domain.model.commands.CancelInvitationCommand;
import nrg.inc.synhubbackend.groups.domain.model.commands.CreateInvitationCommand;
import nrg.inc.synhubbackend.groups.domain.model.commands.RejectInvitationCommand;

import java.util.Optional;

public interface InvitationCommandService {
    Optional<Invitation> handle(CreateInvitationCommand command);
    void handle(RejectInvitationCommand command);
    void handle(CancelInvitationCommand command);
    void handle(AcceptInvitationCommand command);
}
