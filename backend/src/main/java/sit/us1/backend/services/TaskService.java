package sit.us1.backend.services;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.web.multipart.MultipartFile;
import sit.us1.backend.dtos.tasksDTO.*;
import sit.us1.backend.entities.taskboard.Board;
import sit.us1.backend.entities.taskboard.TaskAttachment;
import sit.us1.backend.entities.taskboard.TaskList;
import sit.us1.backend.entities.taskboard.TaskStatus;
import sit.us1.backend.exceptions.BadRequestException;
import sit.us1.backend.exceptions.NotFoundException;
import sit.us1.backend.properties.FileStorageProperties;
import sit.us1.backend.repositories.taskboard.*;

import java.util.ArrayList;
import java.util.List;

@Service
public class TaskService {
    @Autowired
    private TaskListRepository taskRepository;
    @Autowired
    private TaskStatusRepository statusRepository;
    @Autowired
    private BoardRepository boardRepository;
    @Autowired
    private TaskAttachmentRepository taskAttachmentRepository;

    @Autowired
    TaskAttachmentService fileService;

    @Autowired
    private ListMapper listMapper;

    @Autowired
    private ModelMapper mapper;

    private final String hostName;

    @Autowired
    public TaskService(FileStorageProperties fileStorageProperties) {
        this.hostName = fileStorageProperties.getFileServiceHostName();
    }

    public boolean isTaskExist(Integer taskId) {
        return taskRepository.existsById(taskId);
    }

    public List<SimpleTaskDTO> getAllTask() {
        return listMapper.mapList(taskRepository.findAll(), SimpleTaskDTO.class, mapper);
    }

    public List<SimpleTaskDTO> getTaskFiltered(String sortBy, String[] filterStatuses, String boardId) {
        Board board = boardRepository.findById(boardId).orElseThrow(() -> new NotFoundException("Board not found: " + boardId));
        try {
            Sort sort = Sort.by(sortBy == null || sortBy.isEmpty() ? "createdOn" : sortBy);
            if (filterStatuses.length > 0) {
                return listMapper.mapList(taskRepository.findByStatusNamesSorted(filterStatuses, boardId, sort), SimpleTaskDTO.class, mapper);
            } else {
                return listMapper.mapList(taskRepository.findAllByBoardId(boardId, sort), SimpleTaskDTO.class, mapper);
            }
        } catch (Exception e) {
            throw new BadRequestException("Invalid sortBy property: " + sortBy);
        }
    }

    public TaskDetailDTO getTaskById(String boardId, Integer id) {
        TaskList task = taskRepository.findByBoardIdAndId(boardId, id).orElseThrow(() -> new NotFoundException("the specified task does not exist"));
        TaskDetailDTO taskDetailDTO = mapper.map(task, TaskDetailDTO.class);
        List<TaskAttachment> taskAttachments = taskAttachmentRepository.findAllByTaskId(id);

        return mapper.map(task, TaskDetailDTO.class);
    }

    public StatusCountDTO getCountByStatusIdAndReturnStatusName(String boardId, Integer statusId) {
        Board board = boardRepository.findById(boardId).orElseThrow(() -> new NotFoundException("Board not found: " + boardId));
        TaskStatus status;
        if (board.getIsCustomStatus()) {
            status = statusRepository.findByBoardIdAndId(boardId, statusId).orElseThrow(() -> new NotFoundException("Status Id not found: " + statusId));
        } else {
            status = statusRepository.findById(statusId).orElseThrow(() -> new NotFoundException("Status Id not found: " + statusId));
        }
        StatusCountDTO StatusCount = taskRepository.countByStatusIdAndReturnName(boardId, statusId);
        return StatusCount == null ? new StatusCountDTO(0L, status.getName()) : StatusCount;
    }

    @Transactional
    public TaskResponseDTO createTask(String boardId, TaskRequestDTO taskRequestDTO, List<MultipartFile> files) {
        TaskList task = mapper.map(taskRequestDTO, TaskList.class);
        Board board = boardRepository.findById(boardId).orElseThrow(() -> new NotFoundException("Board not found: " + boardId));
        TaskStatus status;
        if (board.getIsCustomStatus()) {
            status = statusRepository.findByBoardIdAndName(boardId, taskRequestDTO.getStatus()).orElseThrow(() -> new NotFoundException("Status Name not found: " + taskRequestDTO.getStatus()));
        } else {
            status = statusRepository.findByNameAndBoardIdIsNull(taskRequestDTO.getStatus()).orElseThrow(() -> new NotFoundException("Status Name not found: " + taskRequestDTO.getStatus()));
        }
        try {
            task.setStatus(status);
            task.setBoard(board);
            TaskList newTask = taskRepository.save(task);
            TaskResponseDTO newTaskResponse = mapper.map(newTask, TaskResponseDTO.class);
//            if (files != null && files.length > 0) {
//                List<Attachment> taskAttachment = fileService.saveTaskAttachment(boardId,newTask.getId(), files);
////                List<Attachment> attachments = getAttachments(boardId, taskAttachment, newTask);
//                newTaskResponse.setAttachments(taskAttachment);
//            }

            if (files != null && files.size() > 0) {
                AttachmentResponseDTO taskAttachment = fileService.saveTaskAttachment(boardId, newTask.getId(), files);
                newTaskResponse.setAttachments(taskAttachment);
            }

            return newTaskResponse;
        } catch (Exception e) {
            throw new NotFoundException("Failed to add This task");
        }
    }

    private List<SimpleAttachmentDTO> getAttachments(String boardId, List<TaskAttachment> taskAttachment, TaskList task) {
        List<SimpleAttachmentDTO> attachments = new ArrayList<>();
        for (TaskAttachment attachment : taskAttachment) {
            SimpleAttachmentDTO newAttachment = new SimpleAttachmentDTO();
            newAttachment.setFilename(attachment.getFilename());
            newAttachment.setContentType(attachment.getContentType());
            newAttachment.setDownloadUrl(this.hostName + "/boards/" + boardId + "/tasks/" + task.getId() + "/attachments/" + attachment.getFilename() + "?disposition=attachment");
            newAttachment.setPreviewUrl(this.hostName + "/boards/" + boardId + "/tasks/" + task.getId() + "/attachments/" + attachment.getFilename() + "?disposition=inline");
            attachments.add(newAttachment);
        }
        return attachments;
    }

    @Transactional
    public TaskResponseDTO updateTask(String boardId, Integer taskId, TaskRequestDTO taskRequestDTO, List<MultipartFile> files, List<String> filesToDelete) {
        TaskList taskList = taskRepository.findById(taskId).orElseThrow(() -> new NotFoundException("the specified task does not exist"));
        TaskStatus status;
        Board board = boardRepository.findById(boardId).orElseThrow(() -> new NotFoundException("Board not found: " + boardId));

        if (board.getIsCustomStatus()) {
            status = statusRepository.findByBoardIdAndName(boardId, taskRequestDTO.getStatus()).orElseThrow(() -> new NotFoundException("Status Name not found: " + taskRequestDTO.getStatus()));
        } else {
            status = statusRepository.findByNameAndBoardIdIsNull(taskRequestDTO.getStatus()).orElseThrow(() -> new NotFoundException("Status Name not found: " + taskRequestDTO.getStatus()));
        }
        taskRequestDTO.setId(taskId);
        TaskList task = mapper.map(taskRequestDTO, TaskList.class);
        TaskList newTask;

        try {
            task.setStatus(status);
            task.setBoard(board);
            newTask = taskRepository.save(task);
        } catch (Exception e) {
            throw new BadRequestException("Failed to update this task with Id number :" + taskId);
        }

        TaskResponseDTO newTaskResponse = mapper.map(newTask, TaskResponseDTO.class);



        if (filesToDelete != null && !filesToDelete.isEmpty()) {
            AttachmentResponseDTO taskAttachment = newTaskResponse.getAttachments();
            for (String filename : filesToDelete) {
                AttachmentResponseDTO taskAttachmentDelete = fileService.removeTaskResource(taskId, filename);
                if (taskAttachment == null) {
                    taskAttachment = new AttachmentResponseDTO();
                }
                if(taskAttachment.getFilesErrors() == null) {
                    taskAttachment.setFilesErrors(new ArrayList<>());
                }
                if(taskAttachment.getFilesSuccess() == null) {
                    taskAttachment.setFilesSuccess(new ArrayList<>());
                }

                taskAttachment.getFilesErrors().addAll(taskAttachmentDelete.getFilesErrors());
                taskAttachment.getFilesSuccess().addAll(taskAttachmentDelete.getFilesSuccess());
            }
            newTaskResponse.setAttachments(taskAttachment);
        }

        if (files != null && !files.isEmpty()) {
            AttachmentResponseDTO taskAttachment = fileService.saveTaskAttachment(boardId, taskId, files);
            if(newTaskResponse.getAttachments() == null) {
                newTaskResponse.setAttachments(taskAttachment);
            } else {
                if(taskAttachment.getFilesErrors() != null) {
                    newTaskResponse.getAttachments().getFilesErrors().addAll(taskAttachment.getFilesErrors());
                }
                if(taskAttachment.getFilesSuccess() != null) {
                    newTaskResponse.getAttachments().getFilesSuccess().addAll(taskAttachment.getFilesSuccess());
                }
            }

        }

        return mapper.map(newTaskResponse, TaskResponseDTO.class);
    }

    @Transactional
    public SimpleTaskDTO deleteTask(String boardId, Integer taskId) {
        Board board = boardRepository.findById(boardId).orElseThrow(() -> new NotFoundException("Board not found: " + boardId));
        TaskList taskList = taskRepository.findByBoardIdAndId(boardId, taskId).orElseThrow(() -> new NotFoundException("the specified task does not exist"));
        fileService.removeAllTaskResource(taskId);
        try {
            taskRepository.delete(taskList);
            return mapper.map(taskList, SimpleTaskDTO.class);
        } catch (Exception e) {
            throw new NotFoundException("Failed to delete this task with Id number :" + taskId);
        }
    }
}





