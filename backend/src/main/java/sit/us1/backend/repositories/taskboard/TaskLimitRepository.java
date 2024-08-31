package sit.us1.backend.repositories.taskboard;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import sit.us1.backend.entities.taskboard.TaskLimit;

import java.util.Optional;
@Repository
public interface TaskLimitRepository extends JpaRepository<TaskLimit, Integer> {
//    @Transactional
//    @Modifying
//    @Query("UPDATE TaskLimit tl SET tl.maximumTask = :newLimit, tl.isLimit = :isLimit WHERE tl.id = :id")
//    void updateLimit(Integer id, Integer newLimit, Boolean isLimit);

    @Transactional
    @Modifying
    @Query("UPDATE TaskLimit tl SET tl.maximumTask = :newLimit , tl.isLimit = :isLimit WHERE tl.boardId = :boardId")
    void updateLimit(String boardId, Integer newLimit, Boolean isLimit);

    Optional<TaskLimit> findByBoardId(String boardId);

}
