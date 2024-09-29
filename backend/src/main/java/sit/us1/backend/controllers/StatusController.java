package sit.us1.backend.controllers;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sit.us1.backend.dtos.statusesDTO.SimpleStatusDTO;
import sit.us1.backend.entities.taskboard.Board;
import sit.us1.backend.repositories.taskboard.BoardRepository;
import sit.us1.backend.services.SecurityUtil;
import sit.us1.backend.services.StatusService;


import java.util.ArrayList;
import java.util.List;

@CrossOrigin(origins = {"http://localhost:5173", "https://ip23us1.sit.kmutt.ac.th", "https://intproj23.sit.kmutt.ac.th"})
@RestController
@RequestMapping("/v2/statuses")
public class StatusController {
    @Autowired
    private StatusService statusService;

    @Autowired
    BoardRepository boardRepository;

    @GetMapping
    public ResponseEntity<List<SimpleStatusDTO>> getStatusList() {
        String Oid = SecurityUtil.getCurrentUserDetails().getOid();
        List<Board> board = boardRepository.findAllByOwner_Id(Oid);
        if (board.isEmpty()) {
            return ResponseEntity.ok(new ArrayList<>());
        } else {
            return ResponseEntity.ok(statusService.getAllStatus(board.get(0).getId()));
        }
    }

}
