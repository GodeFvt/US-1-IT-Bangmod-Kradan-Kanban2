package sit.us1.backend.repositories.taskboard;

import org.springframework.data.jpa.repository.JpaRepository;
import sit.us1.backend.entities.taskboard.TaskAttachment;

import java.util.List;
import java.util.Optional;

public interface TaskAttachmentRepository extends JpaRepository<TaskAttachment, Integer> {
    TaskAttachment findByTaskIdAndFilename(Integer taskId, String filename);

    void deleteByTaskIdAndFilename(Integer taskId, String filename);

    Boolean existsByTaskIdAndFilename(Integer taskId, String filename);

    List<TaskAttachment> findAllByTaskId(Integer taskId);
}
