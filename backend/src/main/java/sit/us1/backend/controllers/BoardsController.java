package sit.us1.backend.controllers;


import jakarta.validation.Valid;
import jakarta.validation.Validator;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.groups.Default;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import sit.us1.backend.dtos.boardsDTO.BoardRequestDTO;
import sit.us1.backend.dtos.boardsDTO.SimpleBoardDTO;
import sit.us1.backend.dtos.limitsDTO.StatusLimitResponseDTO;
import sit.us1.backend.dtos.statusesDTO.SimpleStatusDTO;
import sit.us1.backend.dtos.statusesDTO.StatusValidDTO;
import sit.us1.backend.dtos.tasksDTO.*;
import sit.us1.backend.entities.taskboard.TaskLimit;
import sit.us1.backend.exceptions.ValidationUtil;
import sit.us1.backend.services.BoardService;
import sit.us1.backend.services.StatusService;
import sit.us1.backend.services.TaskService;
import sit.us1.backend.validations.ValidationGroups;

import java.util.List;

@CrossOrigin(origins = {"http://localhost:5173", "http://ip23us1.sit.kmutt.ac.th", "http://intproj23.sit.kmutt.ac.th"})
@RestController
@RequestMapping("/boards")
public class BoardsController {
    @Autowired
    private BoardService boardService;

    @Autowired
    private TaskService taskService;

    @Autowired
    private StatusService statusService;

    @Autowired
    private Validator validator;

    private final ValidationUtil validationUtil ;

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
    public StatusCountDTO countTasksByStatusId(@PathVariable String id, @PathVariable Integer statusId) {
        return taskService.getCountByStatusIdAndReturnStatusName(id, statusId);
    }

    @PostMapping("/{id}/tasks")
    public ResponseEntity<TaskResponseDTO> createTask(@PathVariable String id,@RequestBody TaskRequestDTO newTask){
        newTask.setBoardId(id);
        validationUtil.validateAndThrow(newTask);
        TaskResponseDTO taskList = taskService.createTask(id, newTask);
        return ResponseEntity.status(HttpStatus.CREATED).body(taskList);
    }

    @PutMapping("/{id}/tasks/{taskId}")
    public ResponseEntity<TaskResponseDTO> updateTask(@PathVariable String id, @PathVariable Integer taskId, @Validated @RequestBody TaskRequestDTO task) {
        TaskResponseDTO taskList = taskService.updateTask(id, taskId, task);
        return ResponseEntity.ok(taskList);
    }

    @DeleteMapping("/{id}/tasks/{taskId}")
    public ResponseEntity<SimpleTaskDTO> deleteTask(@Valid @PathVariable String id, @PathVariable Integer taskId) {
        SimpleTaskDTO taskList = taskService.deleteTask(id, taskId);
        return ResponseEntity.ok(taskList);
    }

    // Status
    @GetMapping("/{id}/statuses")
    public ResponseEntity<List<SimpleStatusDTO>> getStatusList(@PathVariable String id) {
        return ResponseEntity.ok(statusService.getAllStatus(id));
    }

    @GetMapping("/{id}/statuses/{statusId}")
    public ResponseEntity<SimpleStatusDTO> getStatusById(@PathVariable String id ,@PathVariable Integer statusId) {
        return ResponseEntity.ok(statusService.getStatusById(id,statusId));
    }

    @GetMapping("/{id}/statuses/limit")
    public ResponseEntity<TaskLimit> getStatusLimit(@PathVariable String id) {
        return ResponseEntity.ok(statusService.getStatusLimit(id));
    }

    @PostMapping("/{id}/statuses")
    public ResponseEntity<SimpleStatusDTO> createStatus(@PathVariable String id, @RequestBody SimpleStatusDTO newStatus) {
        StatusValidDTO statusAllId = new StatusValidDTO();
        statusAllId.setBoardId(id);
        statusAllId.setName(newStatus.getName());
        validationUtil.validateAndThrow(statusAllId, ValidationGroups.OnCreate.class);
        SimpleStatusDTO status = statusService.createStatus(id,newStatus);
        return ResponseEntity.status(HttpStatus.CREATED).body(status);
    }

    @PutMapping("/{id}/statuses/{statusId}")
    public ResponseEntity<SimpleStatusDTO> updateStatus(@PathVariable String id,@PathVariable Integer statusId, @Validated @RequestBody SimpleStatusDTO statusDTO) {
        StatusValidDTO statusAllId = new StatusValidDTO();
        statusAllId.setBoardId(id);
        statusAllId.setNewStatusId(statusId);
        validationUtil.validateAndThrow(statusAllId, ValidationGroups.OnUpdate.class);
        SimpleStatusDTO status = statusService.updateStatus(id,statusId, statusDTO);
        return ResponseEntity.ok(status);
    }

    @PatchMapping("/{id}/statuses/all/maximum-task")
    public ResponseEntity<List<StatusLimitResponseDTO>> updateLimitMaxiMunTask(@PathVariable String id,@RequestParam @Min(0) @Max(30) Integer maximumTask, @RequestParam Boolean isLimit) {
        List<StatusLimitResponseDTO> status = statusService.updateLimitMaxiMunTask(id,maximumTask, isLimit);
        return ResponseEntity.ok(status);
    }

    @DeleteMapping("/{id}/statuses/{statusId}")
    public ResponseEntity<SimpleStatusDTO> deleteStatus(@PathVariable String id, @PathVariable Integer statusId) {
        StatusValidDTO statusAllId = new StatusValidDTO();
        statusAllId.setBoardId(id);
        statusAllId.setNewStatusId(statusId);
        validationUtil.validateAndThrow(statusAllId, ValidationGroups.OnDeleteStatus.class);
        SimpleStatusDTO status = statusService.deleteStatus(id,statusId);
        return ResponseEntity.ok(status);
    }

    @DeleteMapping("/{id}/statuses/{statusId}/{newStatusId}")
    public ResponseEntity<SimpleStatusDTO> deleteStatusAndTransferStatusInAllTask(@PathVariable String id, @PathVariable Integer statusId,@PathVariable Integer newStatusId) {
        StatusValidDTO statusAllId = new StatusValidDTO();
        statusAllId.setBoardId(id);
        statusAllId.setOldStatusId(statusId);
        statusAllId.setNewStatusId(newStatusId);
        validationUtil.validateAndThrow(statusAllId, ValidationGroups.OnDeleteStatusAndTransfer.class);
        SimpleStatusDTO status = statusService.deleteStatusAndTransferStatusInAllTask(id,statusId, newStatusId);
        return ResponseEntity.ok(status);
    }

}
