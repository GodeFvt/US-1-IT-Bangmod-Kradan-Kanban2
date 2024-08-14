package sit.us1.backend.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;
import sit.us1.backend.entities.TaskLimit;

public interface TaskLimitRepository extends JpaRepository<TaskLimit, Integer> {
    @Transactional
    @Modifying
    @Query("UPDATE TaskLimit tl SET tl.maximumTask = :newLimit, tl.isLimit = :isLimit WHERE tl.id = :id")
    void updateLimit(Integer id, Integer newLimit, Boolean isLimit);


}
