package sit.us1.backend.dtos.boardsDTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import sit.us1.backend.entities.taskboard.Collaboration;
import sit.us1.backend.validations.ValidationGroups;

import java.time.ZonedDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SimpleCollaboratorDTO {
    private String oid;
    private String name;
    @NotNull(groups = {ValidationGroups.OnCreate.class})
    @NotBlank(groups = {ValidationGroups.OnCreate.class})
    private String email;
    private Collaboration.Access access;
    private ZonedDateTime addedOn;
}
