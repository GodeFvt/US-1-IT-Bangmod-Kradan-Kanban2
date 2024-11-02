package sit.us1.backend.controllers;

import org.springframework.core.io.Resource;
import jakarta.validation.Validator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import sit.us1.backend.dtos.tasksDTO.*;
import sit.us1.backend.services.TaskAttachmentService;
import sit.us1.backend.validations.ValidationUtil;
import sit.us1.backend.services.TaskService;


import java.util.List;

@CrossOrigin(origins = {"http://localhost:5173", "https://ip23us1.sit.kmutt.ac.th", "https://intproj23.sit.kmutt.ac.th"})
@RestController
@RequestMapping("/v3/boards/{boardId}/tasks")
public class TasksController {
    @Autowired
    private TaskService taskService;
    @Autowired
    TaskAttachmentService fileService;

    private final ValidationUtil validationUtil;

    public TasksController(Validator validator) {
        this.validationUtil = new ValidationUtil(validator);
    }

    // Task
    @GetMapping()
    public ResponseEntity<List<SimpleTaskDTO>> getTaskFiltered(@RequestParam(defaultValue = "") String sortBy,
                                                               @RequestParam(defaultValue = "") String[] filterStatuses,
                                                               @PathVariable String boardId) {
        return ResponseEntity.ok(taskService.getTaskFiltered(sortBy, filterStatuses, boardId));
    }

    @GetMapping("/{taskId}")
    public ResponseEntity<TaskDetailDTO> getTaskById(@PathVariable String boardId, @PathVariable Integer taskId) {
        return ResponseEntity.ok(taskService.getTaskById(boardId, taskId));
    }

    @GetMapping("/count/status/{statusId}")
    public ResponseEntity<StatusCountDTO> countTasksByStatusId(@PathVariable String boardId, @PathVariable Integer statusId) {
        return ResponseEntity.ok(taskService.getCountByStatusIdAndReturnStatusName(boardId, statusId));
    }

    @PostMapping(consumes = "application/json")
    public ResponseEntity<TaskResponseDTO> createTaskWithoutFile(@PathVariable String boardId, @RequestBody TaskRequestDTO newTask) {
        newTask.setBoardId(boardId);
        validationUtil.validateAndThrow(newTask);
        TaskResponseDTO taskList = taskService.createTask(boardId, newTask, null);
        return ResponseEntity.status(HttpStatus.CREATED).body(taskList);
    }

    @PostMapping(consumes = "multipart/form-data")
    public ResponseEntity<TaskResponseDTO> createTaskWithFile(@PathVariable String boardId,
                                                              @RequestPart("task") TaskRequestDTO newTask,
                                                              @RequestPart(value = "file", required = false) MultipartFile[] file) {
        newTask.setBoardId(boardId);
        validationUtil.validateAndThrow(newTask);
        TaskResponseDTO taskList = taskService.createTask(boardId, newTask, file);

        return ResponseEntity.status(HttpStatus.CREATED).body(taskList);
    }

    @PutMapping("/{taskId}")
    public ResponseEntity<TaskResponseDTO> updateTask(@PathVariable String boardId, @PathVariable Integer taskId, @RequestBody TaskRequestDTO newTask) {
        newTask.setBoardId(boardId);
        validationUtil.validateAndThrow(newTask);
        TaskResponseDTO taskList = taskService.updateTask(boardId, taskId, newTask);
        return ResponseEntity.ok(taskList);
    }

    @DeleteMapping("/{taskId}")
    public ResponseEntity<SimpleTaskDTO> deleteTask(@PathVariable String boardId, @PathVariable Integer taskId) {
        SimpleTaskDTO taskList = taskService.deleteTask(boardId, taskId);
        return ResponseEntity.ok(taskList);
    }


    @GetMapping("/{taskId}/attachments/{filename:.+}")
    public ResponseEntity<Resource> downloadFile(
            @PathVariable Integer taskId,
            @PathVariable String filename,
            @RequestParam(value = "disposition", defaultValue = "attachment") String disposition) {

        ResourceFileDTO resourceFile = fileService.loadFileAsResource(taskId, filename);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(resourceFile.getContentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition + "; filename=\"" + resourceFile.getFileName() + "\"")
                .body(resourceFile.getFile());
    }

    @DeleteMapping("/{taskId}/attachments/{filename:.+}")
    public ResponseEntity<Void> deleteFile(@PathVariable Integer taskId, @PathVariable String filename) {
        fileService.removeTaskResource(taskId, filename);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{taskId}/attachments")
    public ResponseEntity<List<Attachment>> uploadFile(@PathVariable String boardId, @PathVariable Integer taskId, @RequestParam("file") MultipartFile[] files) {
        List<Attachment> taskAttachments = fileService.saveTaskAttachment(boardId, taskId, files);
        return ResponseEntity.status(HttpStatus.CREATED).body(taskAttachments);
    }

}
