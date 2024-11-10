package sit.us1.backend.dtos.tasksDTO;

import lombok.Data;
import sit.us1.backend.dtos.statusesDTO.SimpleStatusDTO;
import sit.us1.backend.entities.taskboard.TaskAttachment;

import java.util.List;

@Data
public class SimpleTaskDTO {
    private Integer id;
    private String title;
    private String assignees;
    private List<TaskAttachment> attachments;
    private SimpleStatusDTO status;

}
