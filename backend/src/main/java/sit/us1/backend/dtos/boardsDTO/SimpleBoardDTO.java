package sit.us1.backend.dtos.boardsDTO;

import lombok.Data;
import sit.us1.backend.entities.taskboard.BoardUser;

@Data
public class SimpleBoardDTO {
    private String id;
    private String name;
    private String visibility = "PRIVATE";
    private Boolean isCustomStatus;
    private BoardUser owner;

    public void setVisibility(String visibility) {
        if (visibility == null || visibility.isEmpty()) {
            this.visibility = "PRIVATE";
        } else {
            this.visibility = visibility.toUpperCase();
        }
    }
}
