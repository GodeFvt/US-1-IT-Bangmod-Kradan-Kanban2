package sit.us1.backend.controllers;


import jakarta.validation.Valid;
import jakarta.validation.Validator;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import sit.us1.backend.dtos.limitsDTO.StatusLimitResponseDTO;
import sit.us1.backend.dtos.statusesDTO.SimpleStatusDTO;
import sit.us1.backend.dtos.statusesDTO.StatusValidDTO;
import sit.us1.backend.entities.taskboard.TaskLimit;
import sit.us1.backend.utils.ValidationUtil;
import sit.us1.backend.services.StatusService;
import sit.us1.backend.validations.ValidationGroups;


import java.util.List;

@CrossOrigin(origins = {"http://localhost:5173", "https://ip23us1.sit.kmutt.ac.th", "https://intproj23.sit.kmutt.ac.th"})
@RestController
@RequestMapping("/v3/boards/{boardId}")
public class StatusController {
    @Autowired
    private StatusService statusService;

    private final ValidationUtil validationUtil;

    public StatusController(Validator validator) {
        this.validationUtil = new ValidationUtil(validator);
    }

    // Status
    @GetMapping("/statuses")
    public ResponseEntity<List<SimpleStatusDTO>> getStatusList(@PathVariable String boardId) {
        return ResponseEntity.ok(statusService.getAllStatus(boardId));
    }

    @GetMapping("/statuses/{statusId}")
    public ResponseEntity<SimpleStatusDTO> getStatusById(@PathVariable String boardId, @PathVariable Integer statusId) {
        return ResponseEntity.ok(statusService.getStatusById(boardId, statusId));
    }

    @GetMapping("/statuses/limit")
    public ResponseEntity<TaskLimit> getStatusLimit(@PathVariable String boardId) {
        return ResponseEntity.ok(statusService.getStatusLimit(boardId));
    }

    @PostMapping("/statuses")
    public ResponseEntity<SimpleStatusDTO> createStatus(@PathVariable String boardId, @Valid @RequestBody SimpleStatusDTO newStatus) {
        StatusValidDTO statusAllId = new StatusValidDTO();
        statusAllId.setBoardId(boardId);
        statusAllId.setName(newStatus.getName());
        validationUtil.validateAndThrow(statusAllId, ValidationGroups.OnCreate.class);
        SimpleStatusDTO status = statusService.createStatus(boardId, newStatus);
        return ResponseEntity.status(HttpStatus.CREATED).body(status);
    }

    @PutMapping("/statuses/{statusId}")
    public ResponseEntity<SimpleStatusDTO> updateStatus(@PathVariable String boardId, @PathVariable Integer statusId, @Validated @RequestBody SimpleStatusDTO statusDTO) {
        StatusValidDTO statusAllId = new StatusValidDTO();
        statusAllId.setBoardId(boardId);
        statusAllId.setOnPathStatusId(statusId);
        statusAllId.setOnStatusDTOId(statusDTO.getId());
        statusAllId.setName(statusDTO.getName());
        validationUtil.validateAndThrow(statusAllId, ValidationGroups.OnUpdate.class);
        SimpleStatusDTO status = statusService.updateStatus(boardId, statusId, statusDTO);
        return ResponseEntity.ok(status);
    }

    @PatchMapping("/statuses/all/maximum-task")
    public ResponseEntity<List<StatusLimitResponseDTO>> updateLimitMaxiMunTask(@PathVariable String boardId, @RequestParam @Min(0) @Max(30) Integer maximumTask, @RequestParam Boolean isLimit) {
        List<StatusLimitResponseDTO> status = statusService.updateLimitMaxiMunTask(boardId, maximumTask, isLimit);
        return ResponseEntity.ok(status);
    }

    @DeleteMapping("/statuses/{statusId}")
    public ResponseEntity<SimpleStatusDTO> deleteStatus(@PathVariable String boardId, @PathVariable Integer statusId) {
        StatusValidDTO statusAllId = new StatusValidDTO();
        statusAllId.setBoardId(boardId);
        statusAllId.setOnPathStatusId(statusId);
        validationUtil.validateAndThrow(statusAllId, ValidationGroups.OnDeleteStatus.class);
        SimpleStatusDTO status = statusService.deleteStatus(boardId, statusId);
        return ResponseEntity.ok(status);
    }

    @DeleteMapping("/statuses/{statusId}/{newStatusId}")
    public ResponseEntity<SimpleStatusDTO> deleteStatusAndTransferStatusInAllTask(@PathVariable String boardId, @PathVariable Integer statusId, @PathVariable Integer newStatusId) {
        StatusValidDTO statusAllId = new StatusValidDTO();
        statusAllId.setBoardId(boardId);
        statusAllId.setOnPathStatusId(statusId);
        statusAllId.setOldStatusId(statusId);
        statusAllId.setNewStatusId(newStatusId);
        validationUtil.validateAndThrow(statusAllId, ValidationGroups.OnDeleteStatusAndTransfer.class);
        SimpleStatusDTO status = statusService.deleteStatusAndTransferStatusInAllTask(boardId, statusId, newStatusId);
        return ResponseEntity.ok(status);
    }

}
