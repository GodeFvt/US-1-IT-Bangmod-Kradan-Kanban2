package sit.us1.backend.controllers;


import jakarta.validation.Valid;
import jakarta.validation.Validator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sit.us1.backend.dtos.boardsDTO.BoardRequestDTO;
import sit.us1.backend.dtos.boardsDTO.SimpleBoardDTO;
import sit.us1.backend.validations.ValidationUtil;
import sit.us1.backend.services.BoardService;

import java.util.List;

@CrossOrigin(origins = {"http://localhost:5173", "https://ip23us1.sit.kmutt.ac.th", "https://intproj23.sit.kmutt.ac.th"})
@RestController
@RequestMapping("/v3/boards")
public class BoardsController {
    @Autowired
    private BoardService boardService;

    private final ValidationUtil validationUtil;

    public BoardsController(Validator validator) {
        this.validationUtil = new ValidationUtil(validator);
    }

    @GetMapping
    public ResponseEntity<List<SimpleBoardDTO>> getBoard() {
        return ResponseEntity.ok(boardService.getAllBoardByOid());
    }

    @PostMapping("")
    public ResponseEntity<SimpleBoardDTO> createBoard(@Valid @RequestBody BoardRequestDTO newBoard) {
        return ResponseEntity.status(HttpStatus.CREATED).body(boardService.createBoard(newBoard));
    }

    @GetMapping("/{id}")
    public ResponseEntity<SimpleBoardDTO> getBoardById(@PathVariable String id) {
        return ResponseEntity.ok(boardService.getBoardById(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<SimpleBoardDTO> deleteBoardById(@PathVariable String id) {
        return ResponseEntity.ok(boardService.deleteBoardById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<SimpleBoardDTO> updateBoardById(@PathVariable String id, @Valid @RequestBody BoardRequestDTO board) {
        return ResponseEntity.ok(boardService.updateBoardById(id, board));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<SimpleBoardDTO> updateVisibilityById(@PathVariable String id,@Valid @RequestBody SimpleBoardDTO board) {
        return ResponseEntity.ok(boardService.updateVisibilityById(id, board.getVisibility()));
    }

}
