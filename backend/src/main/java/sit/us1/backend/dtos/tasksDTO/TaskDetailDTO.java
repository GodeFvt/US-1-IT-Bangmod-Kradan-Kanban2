package sit.us1.backend.dtos.tasksDTO;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import sit.us1.backend.dtos.statusesDTO.SimpleStatusDTO;
import sit.us1.backend.entities.taskboard.TaskAttachment;


import java.time.ZonedDateTime;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class TaskDetailDTO {
    private Integer id;
    private String title;
    private String description;
    private String assignees;
    private SimpleStatusDTO status;
    private List<TaskAttachment> attachments;
    private ZonedDateTime createdOn;
    private ZonedDateTime updatedOn;
}
