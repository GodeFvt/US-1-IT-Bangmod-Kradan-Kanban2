package sit.us1.backend.dtos.boardsDTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import sit.us1.backend.entities.taskboard.BoardUser;

@Data
public class SimpleBoardDTO {
    private String id;
    private String name;
    @NotBlank
    @NotNull
    private String visibility ;
    private Boolean isCustomStatus;
    private BoardUser owner;

    public void setVisibility(String visibility) {
        if (visibility != null) {
            this.visibility = visibility.toUpperCase();
        }
    }
}
