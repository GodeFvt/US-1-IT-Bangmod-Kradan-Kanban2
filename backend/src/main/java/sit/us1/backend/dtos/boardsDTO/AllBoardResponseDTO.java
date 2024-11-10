package sit.us1.backend.dtos.boardsDTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AllBoardResponseDTO {
    private List<SimpleBoardDTO> boards;
    private List<SimpleBoardDTO> collab;
    private List<SimpleBoardDTO> invited;
}
