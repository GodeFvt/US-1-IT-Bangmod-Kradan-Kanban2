package sit.us1.backend.repositories.taskboard;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import sit.us1.backend.dtos.tasksDTO.StatusCountDTO;
import sit.us1.backend.entities.taskboard.TaskList;

import java.util.List;
import java.util.Optional;

@Repository
public interface TaskListRepository extends JpaRepository<TaskList, Integer> {
    @Transactional
    @Modifying
    @Query("UPDATE TaskList tl SET tl.status.id = :newId WHERE tl.status.id = :oldId AND tl.board.id = :boardId")
    void updateStatusIdAllTaskList(String boardId,Integer oldId, Integer newId);

    @Query("SELECT new sit.us1.backend.dtos.tasksDTO.StatusCountDTO( COUNT(t), t.status.name) FROM TaskList t JOIN Board b ON t.board.id = b.id WHERE b.id = :boardID AND t.status.id = :statusId GROUP BY t.status.name")
    StatusCountDTO countByStatusIdAndReturnName(String boardID,Integer statusId);

    @Query("SELECT new sit.us1.backend.dtos.tasksDTO.StatusCountDTO( COUNT(ts), ts.status.name) FROM TaskList ts WHERE ts.status.name NOT IN :nonDeletableStatuses GROUP BY ts.status.name HAVING COUNT(ts) > :maximumTask")
    List<StatusCountDTO> countTasksExceedingMaxAndNotInNonDeletableStatuses(@Param("nonDeletableStatuses") String[] nonDeletableStatuses, @Param("maximumTask") Integer maximumTask);

//    @Query("SELECT t FROM TaskList t JOIN Board b ON t.board.id = b.id WHERE b.id=:boardID AND b.owner.id = :oid AND t.status.name IN :filterStatuses")
//    List<TaskList> findByStatusNamesSorted(@Param("filterStatuses") String[] filterStatuses, @Param("boardID") String boardID, @Param("oid") String oid, Sort sort);
//
//    @Query("SELECT t FROM TaskList t JOIN Board b ON t.board.id = b.id WHERE b.id=:boardID AND b.owner.id = :oid")
//    List<TaskList> findAllByBoardIdAndOid(@Param("boardID") String boardID, @Param("oid") String oid,Sort sort);

    @Query("SELECT t FROM TaskList t JOIN Board b ON t.board.id = b.id WHERE b.id=:boardID  AND t.status.name IN :filterStatuses")
    List<TaskList> findByStatusNamesSorted(@Param("filterStatuses") String[] filterStatuses, @Param("boardID") String boardID, Sort sort);

    @Query("SELECT t FROM TaskList t JOIN Board b ON t.board.id = b.id WHERE b.id=:boardID")
    List<TaskList> findAllByBoardId(@Param("boardID") String boardID,Sort sort);

    List<TaskList> findAllByStatusId(Integer statusId);

    Optional<TaskList> findByBoardIdAndId(String boardID, Integer id);

    List<TaskList> findAllByBoard_Id(String boardID);
}
