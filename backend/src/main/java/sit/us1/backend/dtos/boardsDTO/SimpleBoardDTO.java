package sit.us1.backend.dtos.boardsDTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.groups.Default;
import lombok.Data;
import sit.us1.backend.entities.taskboard.Board;
import sit.us1.backend.entities.taskboard.BoardUser;
import sit.us1.backend.entities.taskboard.Collaboration;
import sit.us1.backend.validations.ValidEnum;

import java.util.List;

@Data
public class SimpleBoardDTO {
    private String id;
    private String name;
    @ValidEnum(enumClass = Board.Visibility.class, message = "Invalid access type (PRIVATE, PUBLIC)")
    private String visibility ;
    private Boolean isCustomStatus;
    private BoardUser owner;
    private List<Collaboration> collaborators;

    public Board.Visibility getVisibility() {
        return Board.Visibility.valueOf(this.visibility.toUpperCase());
    }

//    public void setVisibility(String visibility) {
//        if (visibility != null) {
//            this.visibility = visibility.toUpperCase();
//        }
//    }
}
