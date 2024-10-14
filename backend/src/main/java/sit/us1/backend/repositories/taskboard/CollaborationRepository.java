package sit.us1.backend.repositories.taskboard;

import org.springframework.data.jpa.repository.JpaRepository;
import sit.us1.backend.entities.taskboard.Collaboration;
import sit.us1.backend.entities.taskboard.CollaborationId;

import java.util.List;

public interface CollaborationRepository extends JpaRepository<Collaboration, CollaborationId> {
    List<Collaboration> findAllByBoardIdOrderByAddedOn (String boardId);
    List<Collaboration> findAllByBoardId (String boardId);

    boolean existsByOid(String oid);
}
