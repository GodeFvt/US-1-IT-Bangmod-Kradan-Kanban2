package sit.us1.backend.services;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import sit.us1.backend.dtos.tasksDTO.Attachment;
import sit.us1.backend.dtos.tasksDTO.ResourceFileDTO;
import sit.us1.backend.entities.taskboard.TaskAttachment;
import sit.us1.backend.exceptions.BadRequestException;
import sit.us1.backend.exceptions.ConflictException;
import sit.us1.backend.exceptions.NotFoundException;
import sit.us1.backend.properties.FileStorageProperties;
import sit.us1.backend.repositories.taskboard.TaskAttachmentRepository;


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
    private final String hostName ;
    @Autowired
    private TaskAttachmentRepository taskAttachmentRepository;

    @Autowired
    public TaskAttachmentService(FileStorageProperties fileStorageProperties) {
        this.fileStorageLocation = Paths.get(fileStorageProperties.getUploadDir()).toAbsolutePath().normalize();
        this.hostName = fileStorageProperties.getFileServiceHostName();
        try {
            if (!Files.exists(this.fileStorageLocation)) {
                Files.createDirectories(this.fileStorageLocation);
            }
        } catch (IOException ex) {
            throw new RuntimeException(
                    "Could not create the directory where the uploaded files will be stored.", ex);
        }
    }

    public List<Attachment> saveTaskAttachment(String boardId,Integer taskId, MultipartFile[] files) {
        List<Attachment> attachments = new ArrayList<>();

        for (MultipartFile file : files) {
            String fileName = StringUtils.cleanPath(file.getOriginalFilename());

            if (taskAttachmentRepository.existsByTaskIdAndFilename(taskId, fileName)) {
                throw new ConflictException("File name already exists in this task");
            }

            try {
                if (fileName.contains("..")) {
                    throw new RuntimeException("Sorry! Filename contains invalid path sequence " + fileName);
                }
                String storedName = "task" + taskId + "_" + fileName;
                Path targetLocation = this.fileStorageLocation.resolve(storedName);
                Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

                Attachment newAttachment = new Attachment();
                newAttachment.setFilename(fileName);
                newAttachment.setContentType(file.getContentType());
                newAttachment.setDownloadUrl(this.hostName + "/v3/boards/" + boardId + "/tasks/" + taskId + "/attachments/" + fileName + "?disposition=attachment");
                newAttachment.setPreviewUrl(this.hostName + "/v3/boards/" + boardId + "/tasks/" + taskId + "/attachments/" + fileName + "?disposition=inline");
                attachments.add(newAttachment);

            } catch (Exception ex) {
                throw new BadRequestException("Could not store file " + fileName + ". Please try again!" + ex);
            }
        }

        return attachments;
    }


    public ResourceFileDTO loadFileAsResource(Integer taskId,String fileName) {
        TaskAttachment taskAttachment = taskAttachmentRepository.findByTaskIdAndFilename(taskId, fileName).orElseThrow(() -> new NotFoundException("File not found " + fileName));
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
            throw new RuntimeException("File operation error: " + fileName, ex);
        }
    }

    public void removeTaskResource(Integer taskId,String fileName) {
        TaskAttachment taskAttachment = taskAttachmentRepository.findByTaskIdAndFilename(taskId, fileName).orElseThrow(() -> new NotFoundException("File not found " + fileName));
        try {
            Path filePath = this.fileStorageLocation.resolve(taskAttachment.getStoredName()).normalize();
            if (Files.exists(filePath)) {
                Files.delete(filePath);
            } else {
                throw new NotFoundException("File not found " + taskAttachment.getStoredName());
            }
        } catch (Exception ex) {
            throw new NotFoundException("File operation error: " + taskAttachment.getStoredName());
        }
    }

    public void removeAllTaskResource(Integer taskId) {
        try {
            List<TaskAttachment> taskAttachment = taskAttachmentRepository.findAllByTaskId(taskId);
            taskAttachmentRepository.deleteAll(taskAttachment);
            if (!taskAttachment.isEmpty()) {
                for (TaskAttachment attachment : taskAttachment) {
                    Path filePath = this.fileStorageLocation.resolve(attachment.getStoredName()).normalize();
                    if (Files.exists(filePath)) {
                        Files.delete(filePath);
                    } else {
                        throw new FileNotFoundException("File not found " + attachment.getFilename());
                    }
                }
            }
        } catch (Exception ex) {
            throw new NotFoundException("File operation error: " + taskId);
        }
    }
}