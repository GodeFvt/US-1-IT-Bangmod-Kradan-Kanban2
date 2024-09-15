package sit.us1.backend.dtos.boardsDTO;

import lombok.Data;
import sit.us1.backend.entities.taskboard.BoardUser;

@Data
public class SimpleBoardDTO {
    private String id;
    private String name;
    private BoardUser owner;
}
