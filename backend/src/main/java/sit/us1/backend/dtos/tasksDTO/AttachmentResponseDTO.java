package sit.us1.backend.dtos.tasksDTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AttachmentResponseDTO {
    private List<ErrorAttachmentDTO> filesErrors;
    private List<SimpleAttachmentDTO> filesSuccess;

    public AttachmentResponseDTO(List<ErrorAttachmentDTO> errors) {
        this.filesErrors = errors;
    }
}
