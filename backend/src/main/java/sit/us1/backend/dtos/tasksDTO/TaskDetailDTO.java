package sit.us1.backend.dtos.tasksDTO;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import sit.us1.backend.dtos.statusesDTO.SimpleStatusDTO;


import java.time.ZonedDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class TaskDetailDTO {
    private Integer id;
    private String title;
    private String description;
    private String assignees;
    private SimpleStatusDTO status;
    private ZonedDateTime createdOn;
    private ZonedDateTime updatedOn;
}
