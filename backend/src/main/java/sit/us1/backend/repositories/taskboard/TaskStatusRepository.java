package sit.us1.backend.repositories.taskboard;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import sit.us1.backend.dtos.limitsDTO.StatusLimitResponseDTO;
import sit.us1.backend.entities.taskboard.TaskStatus;

import java.util.List;
import java.util.Optional;
@Repository
public interface TaskStatusRepository extends JpaRepository<TaskStatus, Integer> {
    Optional<TaskStatus> findByName(String statusName);

//    @Query("SELECT ts FROM TaskStatus ts JOIN TaskList tl ON ts.id = tl.status.id WHERE ts.name = :statusName AND tl.board.id = :boardId")
//    Optional<TaskStatus>  findByNameAndBoardId(@Param("boardId") String boardId , @Param("statusName") String statusName);

//    @Query("SELECT new sit.us1.backend.dtos.limitsDTO.StatusLimitResponseDTO(ts.id, ts.name, COUNT(tl.id))" +
//            "FROM TaskList tl JOIN tl.status ts " +
//            "GROUP BY ts.id")
//    List<StatusLimitResponseDTO> countTaskByStatus();

    @Query("SELECT new sit.us1.backend.dtos.limitsDTO.StatusLimitResponseDTO(ts.id, ts.name, COUNT(tl.id))" +
            "FROM TaskList tl JOIN tl.status ts " +
            "WHERE tl.board.id = :boardId " +
            "GROUP BY ts.id")
    List<StatusLimitResponseDTO> countTaskByStatus(String boardId);

    Boolean existsByNameAndBoardId(String statusName, String boardId);

    Boolean existsByName (String statusName);

    Boolean existsByNameAndIdNotAndBoardId(String statusName, Integer id, String boardId);

//    @Query("SELECT ts FROM TaskStatus ts JOIN TaskList tl ON ts.id = tl.status.id WHERE tl.board.id = :boardId")
//    List<TaskStatus> findAllByBoardId(String boardId);

    @Query("SELECT ts FROM TaskStatus ts JOIN TaskList tl ON ts.id = tl.status.id WHERE tl.board.id = :boardId AND ts.name = :statusName")
    Optional<TaskStatus> findByNameAndBoardId(String boardId, String statusName);

    Optional<TaskStatus> findByBoardIdAndId(String boardId, Integer statusId);

    //@Query("SELECT ts FROM TaskStatus ts  WHERE ts.isDefault = true")
    List<TaskStatus> findAllByBoardId(String boardId);
    @Query("SELECT ts FROM TaskStatus ts WHERE ts.boardId IS NULL")
    List<TaskStatus> findAllDefaultStatus();

    @Transactional
    @Modifying
    @Query("UPDATE TaskList tl SET tl.status.id = :newId WHERE tl.status.id = :oldId AND tl.board.id = :boardId")
    void updateStatusIdAllTaskList(String boardId,Integer oldId, Integer newId);

    Optional<TaskStatus> findByIdAndBoardIdIsNull(Integer id);

}
