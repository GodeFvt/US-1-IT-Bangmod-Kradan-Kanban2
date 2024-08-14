package sit.us1.backend.dtos.tasksDTO;

import lombok.Data;
import sit.us1.backend.dtos.statusesDTO.SimpleStatusDTO;

@Data

public class SimpleTaskDTO {
    private Integer id;
    private String title;
    private String assignees;
    private SimpleStatusDTO status;

}
