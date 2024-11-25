package sit.us1.backend.controllers;

import jakarta.validation.Validator;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.groups.Default;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import sit.us1.backend.dtos.boardsDTO.CollaboratorResponseDTO;
import sit.us1.backend.dtos.boardsDTO.SimpleCollaboratorDTO;
import sit.us1.backend.services.EmailService;
import sit.us1.backend.services.SecurityUtil;
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
    @Autowired
    private EmailService emailService;

    private final ValidationUtil validationUtil;

    public CollaborationController(Validator validator) {
        this.validationUtil = new ValidationUtil(validator);
    }

    @GetMapping("/collabs")
    public ResponseEntity<List<CollaboratorResponseDTO>> getCollaborator(@PathVariable String boardId) {
        return ResponseEntity.ok(collabService.getCollaborator(boardId));
    }

    @GetMapping("/collabs/{collabId}")
    public ResponseEntity<CollaboratorResponseDTO> getCollaboratorById(@PathVariable String boardId, @PathVariable String collabId) {
        return ResponseEntity.ok(collabService.getCollaboratorById(boardId, collabId));
    }

    @PostMapping("/collabs")
    public ResponseEntity<CollaboratorResponseDTO> addCollaborator(@PathVariable String boardId, @Validated({ValidationGroups.OnCreate.class, Default.class}) @RequestBody SimpleCollaboratorDTO newCollab) {
        CollaboratorResponseDTO collaboratorResponseDTO = collabService.addCollaborator(boardId, newCollab);
        try {
            emailService.sendInvitationEmail(collaboratorResponseDTO.getName(), collaboratorResponseDTO.getEmail(), collaboratorResponseDTO.getBoardName(), collaboratorResponseDTO.getAccessRight(), boardId);
            collaboratorResponseDTO.setEmailStatus("Email sent successfully");
        } catch (Exception e) {
            collaboratorResponseDTO.setEmailStatus("Failed to send email");
        }
        collaboratorResponseDTO.setBoardName(null);
        return ResponseEntity.status(HttpStatus.CREATED).body(collaboratorResponseDTO);
    }

    @PatchMapping("/collabs/{collabId}" )
    public ResponseEntity<CollaboratorResponseDTO> updateCollaborator(@PathVariable String boardId, @PathVariable String collabId, @Validated({ValidationGroups.OnUpdate.class, Default.class}) @RequestBody SimpleCollaboratorDTO collab) {
        return ResponseEntity.ok(collabService.updateCollaborator(boardId, collabId, collab));
    }

    @DeleteMapping("/collabs/{collabId}")
    public ResponseEntity<CollaboratorResponseDTO> deleteCollaborator(@PathVariable String boardId, @PathVariable String collabId) {
        return ResponseEntity.ok(collabService.deleteCollaborator(boardId, collabId));
    }

    @PatchMapping("/collabs/invitations")
    public ResponseEntity<CollaboratorResponseDTO> acceptCollaborator(@PathVariable String boardId, @NotBlank @Pattern(regexp = "accept|decline") @RequestParam String action) {
        return ResponseEntity.ok(collabService.acceptCollaborator(boardId, action));
    }
}
