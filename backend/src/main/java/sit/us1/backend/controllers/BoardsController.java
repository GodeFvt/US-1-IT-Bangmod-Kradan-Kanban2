package sit.us1.backend.controllers;


import jakarta.validation.Valid;
import jakarta.validation.Validator;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.groups.Default;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import sit.us1.backend.dtos.boardsDTO.BoardRequestDTO;
import sit.us1.backend.dtos.boardsDTO.SimpleBoardDTO;
import sit.us1.backend.dtos.boardsDTO.SimpleCollaboratorDTO;
import sit.us1.backend.dtos.limitsDTO.StatusLimitResponseDTO;
import sit.us1.backend.dtos.statusesDTO.SimpleStatusDTO;
import sit.us1.backend.dtos.statusesDTO.StatusValidDTO;
import sit.us1.backend.dtos.tasksDTO.*;
import sit.us1.backend.entities.account.CustomUserDetails;
import sit.us1.backend.entities.taskboard.TaskLimit;
import sit.us1.backend.exceptions.AccessDeniedException;
import sit.us1.backend.exceptions.BadRequestException;
import sit.us1.backend.exceptions.NotFoundException;
import sit.us1.backend.exceptions.ValidationUtil;
import sit.us1.backend.services.BoardService;
import sit.us1.backend.services.SecurityUtil;
import sit.us1.backend.services.StatusService;
import sit.us1.backend.services.TaskService;
import sit.us1.backend.validations.ValidBoardExists;
import sit.us1.backend.validations.ValidBoardUser;
import sit.us1.backend.validations.ValidationGroups;

import java.util.List;

@CrossOrigin(origins = {"http://localhost:5173", "https://ip23us1.sit.kmutt.ac.th", "https://intproj23.sit.kmutt.ac.th"})
@RestController
@RequestMapping("/v3/boards")
public class BoardsController {
    @Autowired
    private BoardService boardService;

    @Autowired
    private TaskService taskService;

    @Autowired
    private StatusService statusService;

    @Autowired
    private Validator validator;

    private final ValidationUtil validationUtil;

    public BoardsController(Validator validator) {
        this.validator = validator;
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

    @GetMapping("/{id}/collabs")
    public ResponseEntity<List<SimpleCollaboratorDTO>> getCollaborator(@PathVariable String id) {
        return ResponseEntity.ok(boardService.getCollaborator(id));
    }

    @GetMapping("/{id}/collabs/{collabId}")
    public ResponseEntity<SimpleCollaboratorDTO> getCollaboratorById(@PathVariable String id, @PathVariable String collabId) {
        return ResponseEntity.ok(boardService.getCollaboratorById(id, collabId));
    }

    @PostMapping("/{id}/collabs")
    public ResponseEntity<SimpleCollaboratorDTO> addCollaborator(@PathVariable String id, @Validated(ValidationGroups.OnCreate.class) @RequestBody SimpleCollaboratorDTO newCollab) {
        return ResponseEntity.status(HttpStatus.CREATED).body(boardService.addCollaborator(id, newCollab));
    }

    @PatchMapping("/{id}/collabs/{collabId}")
    public ResponseEntity<SimpleCollaboratorDTO> updateCollaborator(@PathVariable String id, @PathVariable String collabId, @Validated(ValidationGroups.OnUpdate.class) @RequestBody SimpleCollaboratorDTO collab) {
        return ResponseEntity.ok(boardService.updateCollaborator(id, collabId, collab));
    }

    @DeleteMapping("/{id}/collabs/{collabId}")
    public ResponseEntity<SimpleCollaboratorDTO> deleteCollaborator(@PathVariable String id, @PathVariable String collabId) {
        return ResponseEntity.ok(boardService.deleteCollaborator(id, collabId));
    }

    // Task
    @GetMapping("/{id}/tasks")
    public ResponseEntity<List<SimpleTaskDTO>> getTaskFiltered(@RequestParam(defaultValue = "") String sortBy,
                                                               @RequestParam(defaultValue = "") String[] filterStatuses,
                                                               @PathVariable String id) {
        return ResponseEntity.ok(taskService.getTaskFiltered(sortBy, filterStatuses, id));
    }

    @GetMapping("/{id}/tasks/{taskId}")
    public ResponseEntity<TaskDetailDTO> getTaskById(@PathVariable String id, @PathVariable Integer taskId) {
        return ResponseEntity.ok(taskService.getTaskById(id, taskId));
    }

    @GetMapping("/{id}/tasks/count/status/{statusId}")
    public ResponseEntity<StatusCountDTO> countTasksByStatusId(@PathVariable String id, @PathVariable Integer statusId) {
        return ResponseEntity.ok(taskService.getCountByStatusIdAndReturnStatusName(id, statusId));
    }

    @PostMapping("/{id}/tasks")
    public ResponseEntity<TaskResponseDTO> createTask(@PathVariable String id, @RequestBody TaskRequestDTO newTask) {
        newTask.setBoardId(id);
        validationUtil.validateAndThrow(newTask);
        TaskResponseDTO taskList = taskService.createTask(id, newTask);
        return ResponseEntity.status(HttpStatus.CREATED).body(taskList);
    }

    @PutMapping("/{id}/tasks/{taskId}")
    public ResponseEntity<TaskResponseDTO> updateTask(@PathVariable String id, @PathVariable Integer taskId, @RequestBody TaskRequestDTO newTask) {
        newTask.setBoardId(id);
        validationUtil.validateAndThrow(newTask);
        TaskResponseDTO taskList = taskService.updateTask(id, taskId, newTask);
        return ResponseEntity.ok(taskList);
    }

    @DeleteMapping("/{id}/tasks/{taskId}")
    public ResponseEntity<SimpleTaskDTO> deleteTask(@PathVariable String id, @PathVariable Integer taskId) {
        SimpleTaskDTO taskList = taskService.deleteTask(id, taskId);
        return ResponseEntity.ok(taskList);
    }

    // Status
    @GetMapping("/{id}/statuses")
    public ResponseEntity<List<SimpleStatusDTO>> getStatusList(@PathVariable String id) {
        return ResponseEntity.ok(statusService.getAllStatus(id));
    }

    @GetMapping("/{id}/statuses/{statusId}")
    public ResponseEntity<SimpleStatusDTO> getStatusById(@PathVariable String id, @PathVariable Integer statusId) {
        return ResponseEntity.ok(statusService.getStatusById(id, statusId));
    }

    @GetMapping("/{id}/statuses/limit")
    public ResponseEntity<TaskLimit> getStatusLimit(@PathVariable String id) {
        return ResponseEntity.ok(statusService.getStatusLimit(id));
    }

    @PostMapping("/{id}/statuses")
    public ResponseEntity<SimpleStatusDTO> createStatus(@PathVariable String id, @Valid @RequestBody SimpleStatusDTO newStatus) {
        StatusValidDTO statusAllId = new StatusValidDTO();
        statusAllId.setBoardId(id);
        statusAllId.setName(newStatus.getName());
        validationUtil.validateAndThrow(statusAllId, ValidationGroups.OnCreate.class);
        SimpleStatusDTO status = statusService.createStatus(id, newStatus);
        return ResponseEntity.status(HttpStatus.CREATED).body(status);
    }

    @PutMapping("/{id}/statuses/{statusId}")
    public ResponseEntity<SimpleStatusDTO> updateStatus(@PathVariable String id, @PathVariable Integer statusId, @Validated @RequestBody SimpleStatusDTO statusDTO) {
        StatusValidDTO statusAllId = new StatusValidDTO();
        statusAllId.setBoardId(id);
        statusAllId.setOnPathStatusId(statusId);
        statusAllId.setOnStatusDTOId(statusDTO.getId());
        statusAllId.setName(statusDTO.getName());
        validationUtil.validateAndThrow(statusAllId, ValidationGroups.OnUpdate.class);
        SimpleStatusDTO status = statusService.updateStatus(id, statusId, statusDTO);
        return ResponseEntity.ok(status);
    }

    @PatchMapping("/{id}/statuses/all/maximum-task")
    public ResponseEntity<List<StatusLimitResponseDTO>> updateLimitMaxiMunTask(@PathVariable String id, @RequestParam @Min(0) @Max(30) Integer maximumTask, @RequestParam Boolean isLimit) {
        List<StatusLimitResponseDTO> status = statusService.updateLimitMaxiMunTask(id, maximumTask, isLimit);
        return ResponseEntity.ok(status);
    }

    @DeleteMapping("/{id}/statuses/{statusId}")
    public ResponseEntity<SimpleStatusDTO> deleteStatus(@PathVariable String id, @PathVariable Integer statusId) {
        StatusValidDTO statusAllId = new StatusValidDTO();
        statusAllId.setBoardId(id);
        statusAllId.setOnPathStatusId(statusId);
        validationUtil.validateAndThrow(statusAllId, ValidationGroups.OnDeleteStatus.class);
        SimpleStatusDTO status = statusService.deleteStatus(id, statusId);
        return ResponseEntity.ok(status);
    }

    @DeleteMapping("/{id}/statuses/{statusId}/{newStatusId}")
    public ResponseEntity<SimpleStatusDTO> deleteStatusAndTransferStatusInAllTask(@PathVariable String id, @PathVariable Integer statusId, @PathVariable Integer newStatusId) {
        StatusValidDTO statusAllId = new StatusValidDTO();
        statusAllId.setBoardId(id);
        statusAllId.setOnPathStatusId(statusId);
        statusAllId.setOldStatusId(statusId);
        statusAllId.setNewStatusId(newStatusId);
        validationUtil.validateAndThrow(statusAllId, ValidationGroups.OnDeleteStatusAndTransfer.class);
        SimpleStatusDTO status = statusService.deleteStatusAndTransferStatusInAllTask(id, statusId, newStatusId);
        return ResponseEntity.ok(status);
    }

}
