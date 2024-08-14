package sit.us1.backend.dtos.tasksDTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class StatusCountDTO {
    private Long count;
    private String name;
}
