package sit.us1.backend.dtos.boardsDTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import sit.us1.backend.entities.taskboard.Board;
import sit.us1.backend.entities.taskboard.BoardUser;
import sit.us1.backend.entities.taskboard.Collaboration;

import java.util.List;

@Data
public class SimpleBoardDTO {
    private String id;
    private String name;
    private Board.Visibility visibility ;
    private Boolean isCustomStatus;
    private BoardUser owner;
    private List<Collaboration> collaborators;

//    public void setVisibility(String visibility) {
//        if (visibility != null) {
//            this.visibility = visibility.toUpperCase();
//        }
//    }
}
