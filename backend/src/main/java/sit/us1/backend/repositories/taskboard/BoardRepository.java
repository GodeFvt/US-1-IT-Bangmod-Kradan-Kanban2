package sit.us1.backend.repositories.taskboard;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import sit.us1.backend.entities.taskboard.Board;

import java.util.List;
@Repository
public interface BoardRepository extends JpaRepository<Board, String> {
    List<Board> findAllByOwner_Id(String oid);

    @Query("SELECT b FROM Board b " +
            "WHERE b.owner.id = :oid " +
            "OR b.id IN (SELECT c.boardId FROM Collaboration c WHERE c.oid = :oid)" +
            "ORDER BY b.createdOn")
    List<Board> findAllByOwnerOrCollaborationOrderByCreatedOn(@Param("oid") String oid);
}
