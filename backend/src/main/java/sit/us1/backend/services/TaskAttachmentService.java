package sit.us1.backend.services;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import sit.us1.backend.dtos.tasksDTO.AttachmentResponseDTO;
import sit.us1.backend.dtos.tasksDTO.ErrorAttachmentDTO;
import sit.us1.backend.dtos.tasksDTO.ResourceFileDTO;
import sit.us1.backend.entities.taskboard.TaskAttachment;
import sit.us1.backend.entities.taskboard.TaskList;
import sit.us1.backend.exceptions.BadRequestException;
import sit.us1.backend.exceptions.NotFoundException;
import sit.us1.backend.properties.FileStorageProperties;
import sit.us1.backend.repositories.taskboard.TaskAttachmentRepository;
import sit.us1.backend.repositories.taskboard.TaskListRepository;


import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

@Service
@Getter
public class TaskAttachmentService {

    private final Path fileStorageLocation;
    private final String fileServiceHostName;
    private final Integer maxFilePerTask;
    private final Integer maxFileSize;


    @Autowired
    private TaskAttachmentRepository taskAttachmentRepository;
    @Autowired
    private TaskListRepository taskRepository;

    @Autowired
    public TaskAttachmentService(FileStorageProperties fileStorageProperties) {
        this.fileStorageLocation = Paths.get(fileStorageProperties.getUploadDir()).toAbsolutePath().normalize();
        this.fileServiceHostName = fileStorageProperties.getFileServiceHostName();
        this.maxFilePerTask = fileStorageProperties.getMaxFilePerTask();
        this.maxFileSize = fileStorageProperties.getMaxFileSize();
        try {
            if (!Files.exists(this.fileStorageLocation)) {
                Files.createDirectories(this.fileStorageLocation);
            }
        } catch (IOException ex) {
            throw new RuntimeException("Could not create the directory where the uploaded files will be stored.", ex);
        }
    }

    @Transactional
    public AttachmentResponseDTO saveTaskAttachment(String boardId, Integer taskId, List<MultipartFile> files) {
        taskRepository.findByBoardIdAndId(boardId, taskId).orElseThrow(() -> new NotFoundException("the specified task does not exist"));
        List<ErrorAttachmentDTO> errorMessages = new ArrayList<>();
        List<TaskAttachment> addedFiles = new ArrayList<>();
        long MAX_FILE_SIZE = maxFileSize * 1024 * 1024;
        long MAX_TOTAL_SIZE = ((long) maxFilePerTask * maxFileSize) * 1024 * 1024;

        // ตรวจสอบขนาดไฟล์รวม
        long totalSize = files.stream().mapToLong(MultipartFile::getSize).sum();
        if (totalSize > MAX_TOTAL_SIZE) {
            errorMessages.add(new ErrorAttachmentDTO("Total file size exceeds " + MAX_TOTAL_SIZE / (1024 * 1024) + " MB", null, null));
            return new AttachmentResponseDTO(errorMessages,addedFiles);
        }

        List<TaskAttachment> existingAttachments = taskAttachmentRepository.findAllByTaskId(taskId);

        // ตรวจสอบจำนวนไฟล์สูงสุดต่อ Task
        if (existingAttachments.size() >= maxFilePerTask) {
            errorMessages.add(new ErrorAttachmentDTO("Max " + maxFilePerTask + " files allowed per task", null, null));
            return new AttachmentResponseDTO(errorMessages,addedFiles);
        }


        for (MultipartFile file : files) {
            String fileName = file.getOriginalFilename();
            // ตรวจสอบขนาดของแต่ละไฟล์
            if (file.getSize() > MAX_FILE_SIZE) {
                errorMessages.add(new ErrorAttachmentDTO("File exceeds " + maxFileSize + " MB", fileName, file.getContentType()));
                continue;
            }

            // ตรวจสอบจำนวนไฟล์ใน Task รวมกับไฟล์ที่จะเพิ่มใหม่
            if (existingAttachments.size() + addedFiles.size() >= maxFilePerTask) {
                errorMessages.add(new ErrorAttachmentDTO("Max " + maxFilePerTask + " files allowed", fileName, file.getContentType()));
                break;
            }

            // ตรวจสอบชื่อไฟล์ซ้ำ
            boolean isDuplicate = existingAttachments.stream().anyMatch(att -> att.getFilename().equals(fileName));
            if (isDuplicate) {
                errorMessages.add(new ErrorAttachmentDTO("Duplicate filename not allowed", fileName, file.getContentType()));
                continue;
            }

            String storedName = "task" + taskId + "_" + fileName;

            try {
                Path targetLocation = this.fileStorageLocation.resolve(storedName).normalize();
                Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
                TaskAttachment newAttachment =new TaskAttachment(taskId, fileName, storedName, file.getContentType(), file.getSize());
                taskAttachmentRepository.save(newAttachment);
                addedFiles.add(newAttachment);
            } catch (Exception ex) {
                errorMessages.add(new ErrorAttachmentDTO("Failed to save file", fileName, file.getContentType()));
            }
        }

        return new AttachmentResponseDTO(errorMessages, addedFiles);
    }


    public ResourceFileDTO loadFileAsResource(Integer taskId, String fileName) {
        TaskAttachment taskAttachment = taskAttachmentRepository.findByTaskIdAndFilename(taskId, fileName);
        if (taskAttachment == null) {
            throw new NotFoundException("File not found " + fileName);
        }
        try {
            Path filePath = this.fileStorageLocation.resolve(taskAttachment.getStoredName()).normalize();
            Resource resource = new UrlResource(filePath.toUri());
            ResourceFileDTO resourceFileDTO = new ResourceFileDTO();
            resourceFileDTO.setFile(resource);
            resourceFileDTO.setFileName(taskAttachment.getFilename());
            resourceFileDTO.setContentType(taskAttachment.getContentType());
            if (resource.exists()) {
                return resourceFileDTO;
            } else {
                throw new NotFoundException("File not found " + fileName);
            }
        } catch (MalformedURLException ex) {
            throw new BadRequestException("File operation error: " + fileName);
        }
    }
    @Transactional
    public AttachmentResponseDTO removeTaskResource(String boardId,Integer taskId, String fileName) {
        taskRepository.findByBoardIdAndId(boardId, taskId).orElseThrow(() -> new NotFoundException("the specified task does not exist"));
        TaskAttachment taskAttachment = taskAttachmentRepository.findByTaskIdAndFilename(taskId, fileName);
        List<ErrorAttachmentDTO> errorMessages = new ArrayList<>();
        List<TaskAttachment> addedFiles = new ArrayList<>();
        if (taskAttachment == null ) {
            errorMessages.add(new ErrorAttachmentDTO("File not found", fileName, null));
            return new AttachmentResponseDTO(errorMessages, addedFiles);
        }
        try {
            Path filePath = this.fileStorageLocation.resolve(taskAttachment.getStoredName()).normalize();
            if (Files.exists(filePath)) {
                taskAttachmentRepository.delete(taskAttachment);
                Files.delete(filePath);
                TaskAttachment newAttachment = new TaskAttachment( taskId, fileName, taskAttachment.getStoredName(), taskAttachment.getContentType(), taskAttachment.getFileData());
                addedFiles.add(newAttachment);
            } else {
                errorMessages.add(new ErrorAttachmentDTO("File not found", fileName, taskAttachment.getContentType()));
            }
        } catch (Exception ex) {
            throw new BadRequestException("File operation error: " + taskAttachment.getStoredName());
        }

        return new AttachmentResponseDTO(errorMessages, addedFiles);
    }

    @Transactional
    public void removeAllTaskResource(Integer taskId) {
        List<Path> deletedFiles = new ArrayList<>();
        try {
            List<TaskAttachment> taskAttachment = taskAttachmentRepository.findAllByTaskId(taskId);
            taskAttachmentRepository.deleteAll(taskAttachment);
            if (!taskAttachment.isEmpty()) {
                for (TaskAttachment attachment : taskAttachment) {
                    Path filePath = this.fileStorageLocation.resolve(attachment.getStoredName()).normalize();
                    if (Files.exists(filePath)) {
                        Files.delete(filePath);
                        deletedFiles.add(filePath);
                    } else {
                        throw new FileNotFoundException("File not found " + attachment.getFilename());
                    }
                }
            }
        } catch (Exception ex) {
            for (Path filePath : deletedFiles) {
                try {
                    Files.copy(filePath, this.fileStorageLocation.resolve(filePath.getFileName()), StandardCopyOption.REPLACE_EXISTING);
                } catch (IOException ioEx) {
                    throw new BadRequestException("Failed to restore file: " + filePath.getFileName());
                }
            }
            throw new BadRequestException("File operation error: " + taskId);
        }
    }
}