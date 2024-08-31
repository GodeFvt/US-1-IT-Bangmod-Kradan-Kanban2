package sit.us1.backend.dtos.boardsDTO;

import jakarta.persistence.Column;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import lombok.Data;
import sit.us1.backend.dtos.tasksDTO.SimpleTaskDTO;
import sit.us1.backend.entities.taskboard.BoardUser;
import sit.us1.backend.entities.taskboard.TaskList;

import java.util.List;
@Data
public class SimpleBoardDTO {
    private String id;
    private String name;
    private BoardUser owner;
}
