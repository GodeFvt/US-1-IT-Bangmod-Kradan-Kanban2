package sit.us1.backend.dtos.tasksDTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import sit.us1.backend.validations.ValidTaskStatusLimit;

import java.beans.PropertyEditorSupport;

@ValidTaskStatusLimit
@Data
public class TaskRequestDTO extends PropertyEditorSupport {
    private Integer id;

    @NotBlank
    @NotNull
    @Size(max = 100)
    private String title;
    @Size(min = 0, max = 500)
    private String description;
    @Size(min = 0, max = 30)
    private String assignees;
    private String boardId;
    private String status = "No Status";


    public void setTitle(String value) {
        if (value != null ) {
            this.title = value.trim();
        }

    }

    public void setDescription(String value) {
        if (value == null || value.isEmpty()) {
            this.description = null;
        } else {
            this.description = value.trim();
        }

    }

    public void setAssignees(String value) {
        if (value == null || value.isEmpty()) {
            this.assignees = null;
        } else {
            this.assignees = value.trim();
        }

    }

    public void setStatus(String value) {
        if (value == null || value.isEmpty()) {
            this.status = "No Status";
        } else {
            this.status = value;
        }
    }

}
