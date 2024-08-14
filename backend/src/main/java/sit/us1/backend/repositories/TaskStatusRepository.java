package sit.us1.backend.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import sit.us1.backend.dtos.limitsDTO.StatusLimitResponseDTO;
import sit.us1.backend.entities.TaskStatus;

import java.util.List;
import java.util.Optional;

public interface TaskStatusRepository extends JpaRepository<TaskStatus, Integer> {
    Optional<TaskStatus> findByName(String statusName);

    @Query("SELECT new sit.us1.backend.dtos.limitsDTO.StatusLimitResponseDTO(ts.id, ts.name, COUNT(tl.id))" +
            "FROM TaskList tl JOIN tl.status ts " +
            "GROUP BY ts.id")
    List<StatusLimitResponseDTO> countTaskByStatus();

    Boolean existsByName (String statusName);

    Boolean existsByNameAndIdNot(String statusName, Integer id);

}
