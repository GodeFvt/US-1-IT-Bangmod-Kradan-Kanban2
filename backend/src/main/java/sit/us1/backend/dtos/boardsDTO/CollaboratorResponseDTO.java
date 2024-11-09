package sit.us1.backend.dtos.boardsDTO;


import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;


@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@AllArgsConstructor
@NoArgsConstructor
public class CollaboratorResponseDTO {
    private String oid;
    private String name;
    private String email;
    private String accessRight;
    private Boolean isPending;
    private ZonedDateTime addedOn;
    private String emailStatus;
}
