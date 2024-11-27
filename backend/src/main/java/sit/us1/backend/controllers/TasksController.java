package sit.us1.backend.controllers;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import net.coobird.thumbnailator.Thumbnails;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.springframework.core.io.ByteArrayResource;
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
import sit.us1.backend.entities.taskboard.TaskAttachment;
import sit.us1.backend.exceptions.NotFoundException;
import sit.us1.backend.services.TaskAttachmentService;
import sit.us1.backend.utils.FilePreviewUtil;
import sit.us1.backend.utils.ValidationUtil;
import sit.us1.backend.services.TaskService;


import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
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
                                                              @RequestPart(value = "files", required = false) List<MultipartFile> files) {
        newTask.setBoardId(boardId);
        validationUtil.validateAndThrow(newTask);
        TaskResponseDTO taskList = taskService.createTask(boardId, newTask, files);

        return ResponseEntity.status(HttpStatus.CREATED).body(taskList);
    }

    @PutMapping(value = "/{taskId}", consumes = "application/json")
    public ResponseEntity<TaskResponseDTO> updateTaskWithoutFile(@PathVariable String boardId, @PathVariable Integer taskId, @RequestBody TaskRequestDTO newTask) {
        newTask.setBoardId(boardId);
        validationUtil.validateAndThrow(newTask);
        TaskResponseDTO taskList = taskService.updateTask(boardId, taskId, newTask, null, null);
        return ResponseEntity.ok(taskList);
    }

    @PutMapping(value = "/{taskId}", consumes = "multipart/form-data")
    public ResponseEntity<TaskResponseDTO> updateTaskWithFile(@PathVariable String boardId,
                                                              @PathVariable Integer taskId,
                                                              @RequestPart("task") TaskRequestDTO newTask,
                                                              @RequestParam(value = "files", required = false) List<MultipartFile> files,
                                                              @RequestParam(value = "filesToDelete", required = false) List<String> filesToDelete) {
        newTask.setBoardId(boardId);
        validationUtil.validateAndThrow(newTask);
        TaskResponseDTO taskList = taskService.updateTask(boardId, taskId, newTask, files, filesToDelete);
        return ResponseEntity.ok(taskList);
    }

    @DeleteMapping("/{taskId}")
    public ResponseEntity<SimpleTaskDTO> deleteTask(@PathVariable String boardId, @PathVariable Integer taskId) {
        SimpleTaskDTO taskList = taskService.deleteTask(boardId, taskId);
        return ResponseEntity.ok(taskList);
    }

    @PostMapping("/{taskId}/attachments")
    public ResponseEntity<AttachmentResponseDTO> addAttachments(@PathVariable String boardId, @PathVariable Integer taskId, @RequestParam("files") List<MultipartFile> files) {
        AttachmentResponseDTO errorMessages = fileService.saveTaskAttachment(boardId, taskId, files);
        return ResponseEntity.status(HttpStatus.CREATED).body(errorMessages);
    }

    @DeleteMapping("/{taskId}/attachments/{filename:.+}")
    public ResponseEntity<TaskAttachment> deleteFile(@PathVariable Integer taskId, @PathVariable String filename) {
        AttachmentResponseDTO taskAttachmentResponse = fileService.removeTaskResource(taskId, filename);
        if (!taskAttachmentResponse.getFilesErrors().isEmpty()) {
            throw new NotFoundException(taskAttachmentResponse.getFilesErrors().get(0).getMessage());
        }else {
            TaskAttachment taskAttachment = new TaskAttachment();
            taskAttachment.setTaskId(taskId);
            taskAttachment.setFilename(filename);
            taskAttachment.setContentType(taskAttachmentResponse.getFilesSuccess().get(0).getContentType());
            return ResponseEntity.ok(taskAttachment);
        }
    }

    @GetMapping("/{taskId}/attachments/{filename:.+}")
    public ResponseEntity<Resource> downloadFile(
            @PathVariable Integer taskId,
            @PathVariable String filename,
            @Pattern(regexp = "attachment|inline") @RequestParam(value = "disposition", defaultValue = "attachment") String disposition) {
        ResourceFileDTO resourceFile = fileService.loadFileAsResource(taskId, filename);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(resourceFile.getContentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition + "; filename=\"" + resourceFile.getFileName() + "\"")
                .body(resourceFile.getFile());  
    }

    @GetMapping("/{taskId}/attachments/{filename:.+}/preview")
    public ResponseEntity<Resource> previewFile(
            @PathVariable Integer taskId,
            @PathVariable String filename,
            @Min(1) @RequestParam(value = "width", defaultValue = "200") int width,
            @Min(1) @RequestParam(value = "height", defaultValue = "200") int height,
            @Pattern(regexp = "attachment|inline") @RequestParam(value = "disposition", defaultValue = "inline") String disposition) throws IOException {

        ResourceFileDTO resourceFile = fileService.loadFileAsResource(taskId, filename);
        String contentType = resourceFile.getContentType();
        String extension = filename.substring(filename.lastIndexOf('.') + 1).toLowerCase();

        ByteArrayResource resource;

        return switch (extension) {
            case "jpg", "jpeg", "png" -> {
                resource = FilePreviewUtil.generateImagePreview(resourceFile, width, height);
                yield ResponseEntity.ok()
                        .contentType(MediaType.IMAGE_JPEG)
                        .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"thumbnail-" + resourceFile.getFileName() + "\"")
                        .body(resource);
            }
            case "txt", "rtf" -> {
                resource = FilePreviewUtil.generateTextPreview(resourceFile);
                yield ResponseEntity.ok()
                        .contentType(MediaType.TEXT_PLAIN)
                        .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resourceFile.getFileName() + "\"")
                        .body(resource);
            }
            case "pdf" -> {
                resource = FilePreviewUtil.generatePdfPreview(resourceFile, width, height);
                yield ResponseEntity.ok()
                        .contentType(MediaType.IMAGE_JPEG)
                        .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"preview-" + resourceFile.getFileName() + ".jpeg\"")
                        .body(resource);
            }
            default -> {
                resource = FilePreviewUtil.handleUnsupportedFile(resourceFile);
                yield ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType(contentType))
                        .header(HttpHeaders.CONTENT_DISPOSITION, disposition + "; filename=\"" + resourceFile.getFileName() + "\"")
                        .body(resource);
            }
        };
    }




}
