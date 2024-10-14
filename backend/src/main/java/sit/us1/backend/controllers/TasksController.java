package sit.us1.backend.controllers;

import jakarta.validation.Validator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sit.us1.backend.dtos.tasksDTO.*;
import sit.us1.backend.validations.ValidationUtil;
import sit.us1.backend.services.TaskService;


import java.util.List;

@CrossOrigin(origins = {"http://localhost:5173", "https://ip23us1.sit.kmutt.ac.th", "https://intproj23.sit.kmutt.ac.th"})
@RestController
@RequestMapping("/v3/boards/{boardId}")
public class TasksController {
    @Autowired
    private TaskService taskService;

    private final ValidationUtil validationUtil;

    public TasksController(Validator validator) {
        this.validationUtil = new ValidationUtil(validator);
    }

    // Task
    @GetMapping("/tasks")
    public ResponseEntity<List<SimpleTaskDTO>> getTaskFiltered(@RequestParam(defaultValue = "") String sortBy,
                                                               @RequestParam(defaultValue = "") String[] filterStatuses,
                                                               @PathVariable String boardId) {
        return ResponseEntity.ok(taskService.getTaskFiltered(sortBy, filterStatuses, boardId));
    }

    @GetMapping("/tasks/{taskId}")
    public ResponseEntity<TaskDetailDTO> getTaskById(@PathVariable String boardId, @PathVariable Integer taskId) {
        return ResponseEntity.ok(taskService.getTaskById(boardId, taskId));
    }

    @GetMapping("/tasks/count/status/{statusId}")
    public ResponseEntity<StatusCountDTO> countTasksByStatusId(@PathVariable String boardId, @PathVariable Integer statusId) {
        return ResponseEntity.ok(taskService.getCountByStatusIdAndReturnStatusName(boardId, statusId));
    }

    @PostMapping("/tasks")
    public ResponseEntity<TaskResponseDTO> createTask(@PathVariable String boardId, @RequestBody TaskRequestDTO newTask) {
        newTask.setBoardId(boardId);
        validationUtil.validateAndThrow(newTask);
        TaskResponseDTO taskList = taskService.createTask(boardId, newTask);
        return ResponseEntity.status(HttpStatus.CREATED).body(taskList);
    }

    @PutMapping("/tasks/{taskId}")
    public ResponseEntity<TaskResponseDTO> updateTask(@PathVariable String boardId, @PathVariable Integer taskId, @RequestBody TaskRequestDTO newTask) {
        newTask.setBoardId(boardId);
        validationUtil.validateAndThrow(newTask);
        TaskResponseDTO taskList = taskService.updateTask(boardId, taskId, newTask);
        return ResponseEntity.ok(taskList);
    }

    @DeleteMapping("/tasks/{taskId}")
    public ResponseEntity<SimpleTaskDTO> deleteTask(@PathVariable String boardId, @PathVariable Integer taskId) {
        SimpleTaskDTO taskList = taskService.deleteTask(boardId, taskId);
        return ResponseEntity.ok(taskList);
    }

}
