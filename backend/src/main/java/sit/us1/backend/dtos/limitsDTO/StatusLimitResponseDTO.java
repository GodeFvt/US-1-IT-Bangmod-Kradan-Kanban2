package sit.us1.backend.dtos.limitsDTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class StatusLimitResponseDTO {
    private Integer id;
    private String name;
    private Long noOfTasks;
    private List<TaskInLimitDTO> tasks;

    public StatusLimitResponseDTO(Integer id, String name, Long noOfTasks) {
        this.id = id;
        this.name = name;
        this.noOfTasks = noOfTasks;
    }
}
