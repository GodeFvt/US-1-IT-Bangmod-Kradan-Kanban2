package sit.us1.backend.repositories;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import sit.us1.backend.dtos.tasksDTO.StatusCountDTO;
import sit.us1.backend.entities.TaskList;

import java.util.List;


public interface TaskListRepository extends JpaRepository<TaskList, Integer> {
    @Transactional
    @Modifying
    @Query("UPDATE TaskList tl SET tl.status.id = :newId WHERE tl.status.id = :oldId")
    void updateStatusId(Integer oldId, Integer newId);

    @Query("SELECT new sit.us1.backend.dtos.tasksDTO.StatusCountDTO( COUNT(ts), ts.status.name) FROM TaskList ts WHERE ts.status.id = :statusId GROUP BY ts.status.name")
    StatusCountDTO countByStatusIdAndReturnName(Integer statusId);

    @Query("SELECT new sit.us1.backend.dtos.tasksDTO.StatusCountDTO( COUNT(ts), ts.status.name) FROM TaskList ts WHERE ts.status.name NOT IN :nonDeletableStatuses GROUP BY ts.status.name HAVING COUNT(ts) > :maximumTask")
    List<StatusCountDTO> countTasksExceedingMaxAndNotInNonDeletableStatuses(@Param("nonDeletableStatuses") String[] nonDeletableStatuses, @Param("maximumTask") Integer maximumTask);

    @Query("SELECT t FROM TaskList t WHERE t.status.name IN :filterStatuses")
    List<TaskList> findByStatusNamesSorted(@Param("filterStatuses") String[] filterStatuses, Sort sort);

    List<TaskList> findAllByStatusId(Integer statusId);

    List<TaskList> findAllByStatusNameIn(String[] statusNames);
}
