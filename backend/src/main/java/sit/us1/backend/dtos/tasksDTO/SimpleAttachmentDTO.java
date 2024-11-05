package sit.us1.backend.dtos.tasksDTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SimpleAttachmentDTO {
    private String filename;
    private String contentType;
    private String downloadUrl;
    private String previewUrl;
    private long fileData;
}
