package sit.us1.backend.dtos.tasksDTO;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import sit.us1.backend.dtos.statusesDTO.SimpleStatusDTO;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class TaskResponseDTO {
    private Integer id;
    private String title;
    private String description;
    private String assignees;
    private SimpleStatusDTO status;
    private AttachmentResponseDTO attachments;

}
