package sit.us1.backend.dtos.boardsDTO;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;


@Data
public class BoardRequestDTO {
    @NotBlank
    private String name;
}
