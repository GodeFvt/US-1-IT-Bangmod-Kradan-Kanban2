package sit.us1.backend.repositories.taskboard;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import sit.us1.backend.entities.taskboard.Board;

import java.util.List;
import java.util.Optional;

@Repository
public interface BoardRepository extends JpaRepository<Board, String> {
    //    @Query("SELECT b FROM Board b " +
//            "WHERE b.owner.id = :oid " +
//            "OR b.id IN (SELECT c.boardId FROM Collaboration c WHERE c.oid = :oid) " +
//            "ORDER BY b.createdOn")
//    List<Board> findAllByOwnerOrCollaborationOrderByCreatedOn(@Param("oid") String oid);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT b FROM Board b WHERE b.id = :id")
    Optional<Board> findByIdWithLock(@Param("id") String id);

    @Query("SELECT b FROM Board b WHERE b.owner.id = :oid ORDER BY b.createdOn")
    List<Board> findAllOwnerBoards(@Param("oid") String oid);

    @Query("SELECT b FROM Board b JOIN b.collaborators c " +
            "WHERE c.oid = :oid AND c.isPending = false AND b.owner.id != :oid " +
            "ORDER BY b.createdOn")
    List<Board> findAllCollaboratorBoards(@Param("oid") String oid);

    @Query("SELECT b FROM Board b JOIN b.collaborators c " +
            "WHERE c.oid = :oid AND c.isPending = true AND b.owner.id != :oid " +
            "ORDER BY b.createdOn")
    List<Board> findAllPendingBoards(@Param("oid") String oid);
}
