package sit.us1.backend.dtos.tasksDTO;

import lombok.Data;
import org.springframework.core.io.Resource;

@Data
public class ResourceFileDTO {
    private String fileName;
    private Resource file;
    private String contentType;
}
