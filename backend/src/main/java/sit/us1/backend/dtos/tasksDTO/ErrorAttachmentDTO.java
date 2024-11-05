package sit.us1.backend.dtos.tasksDTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ErrorAttachmentDTO {
    private String message;
    private String fileName;
    private String contentType;
}
