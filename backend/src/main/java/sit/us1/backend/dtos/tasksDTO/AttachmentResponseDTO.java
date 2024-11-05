package sit.us1.backend.dtos.tasksDTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import sit.us1.backend.entities.taskboard.TaskAttachment;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AttachmentResponseDTO {
    private List<ErrorAttachmentDTO> filesErrors;
    private List<TaskAttachment> filesSuccess;

    public AttachmentResponseDTO(List<ErrorAttachmentDTO> errors) {
        this.filesErrors = errors;
    }
}
