package nrg.inc.synhubbackend.requests.infrastructure.persistence.jpa.repositories;

import nrg.inc.synhubbackend.requests.domain.model.aggregates.Request;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RequestRepository extends JpaRepository<Request, Long> {
    List<Request> findByTaskId(Long taskId);
    void deleteByTaskId(Long taskId);
}
