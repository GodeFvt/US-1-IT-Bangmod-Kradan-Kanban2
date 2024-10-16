package sit.us1.backend.controllers;

import jakarta.validation.Validator;
import jakarta.validation.groups.Default;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import sit.us1.backend.dtos.boardsDTO.SimpleCollaboratorDTO;
import sit.us1.backend.validations.ValidationUtil;
import sit.us1.backend.services.CollaborationService;
import sit.us1.backend.validations.ValidationGroups;

import java.util.List;

@CrossOrigin(origins = {"http://localhost:5173", "https://ip23us1.sit.kmutt.ac.th", "https://intproj23.sit.kmutt.ac.th"})
@RestController
@RequestMapping("/v3/boards/{boardId}")
public class CollaborationController {
    @Autowired
    private CollaborationService collabService;

    private final ValidationUtil validationUtil;

    public CollaborationController(Validator validator) {
        this.validationUtil = new ValidationUtil(validator);
    }

    @GetMapping("/collabs")
    public ResponseEntity<List<SimpleCollaboratorDTO>> getCollaborator(@PathVariable String boardId) {
        return ResponseEntity.ok(collabService.getCollaborator(boardId));
    }

    @GetMapping("/collabs/{collabId}")
    public ResponseEntity<SimpleCollaboratorDTO> getCollaboratorById(@PathVariable String boardId, @PathVariable String collabId) {
        return ResponseEntity.ok(collabService.getCollaboratorById(boardId, collabId));
    }

    @PostMapping("/collabs")
    public ResponseEntity<SimpleCollaboratorDTO> addCollaborator(@PathVariable String boardId, @Validated({ValidationGroups.OnCreate.class, Default.class}) @RequestBody SimpleCollaboratorDTO newCollab) {
        return ResponseEntity.status(HttpStatus.CREATED).body(collabService.addCollaborator(boardId, newCollab));
    }

    @PatchMapping("/collabs/{collabId}")
    public ResponseEntity<SimpleCollaboratorDTO> updateCollaborator(@PathVariable String boardId, @PathVariable String collabId, @Validated({ValidationGroups.OnUpdate.class, Default.class}) @RequestBody SimpleCollaboratorDTO collab) {
        return ResponseEntity.ok(collabService.updateCollaborator(boardId, collabId, collab));
    }

    @DeleteMapping("/collabs/{collabId}")
    public ResponseEntity<SimpleCollaboratorDTO> deleteCollaborator(@PathVariable String boardId, @PathVariable String collabId) {
        return ResponseEntity.ok(collabService.deleteCollaborator(boardId, collabId));
    }
}
