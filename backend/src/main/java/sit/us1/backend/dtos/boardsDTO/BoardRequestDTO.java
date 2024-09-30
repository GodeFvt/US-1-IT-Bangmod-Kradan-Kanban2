package sit.us1.backend.dtos.boardsDTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;


@Data
public class BoardRequestDTO {
    @NotBlank
    @NotNull
    @Size(max = 120)
    private String name;
}
