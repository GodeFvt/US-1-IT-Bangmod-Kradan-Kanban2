package sit.us1.backend.services;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sit.us1.backend.dtos.limitsDTO.TaskInLimitDTO;
import sit.us1.backend.dtos.statusesDTO.SimpleStatusDTO;
import sit.us1.backend.dtos.limitsDTO.StatusLimitResponseDTO;
import sit.us1.backend.dtos.tasksDTO.StatusCountDTO;
import sit.us1.backend.entities.taskboard.Board;
import sit.us1.backend.entities.taskboard.TaskLimit;
import sit.us1.backend.entities.taskboard.TaskList;
import sit.us1.backend.entities.taskboard.TaskStatus;
import sit.us1.backend.exceptions.BadRequestException;
import sit.us1.backend.exceptions.NotFoundException;
import sit.us1.backend.exceptions.ValidationException;
import sit.us1.backend.repositories.taskboard.BoardRepository;
import sit.us1.backend.repositories.taskboard.TaskLimitRepository;
import sit.us1.backend.repositories.taskboard.TaskListRepository;
import sit.us1.backend.repositories.taskboard.TaskStatusRepository;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class StatusService {
    @Autowired
    private TaskListRepository taskRepository;
    @Autowired
    private TaskStatusRepository statusRepository;
    @Autowired
    private TaskLimitRepository limitRepository;
    @Autowired
    private BoardRepository boardRepository;
    @Autowired
    private ListMapper listMapper;
    @Autowired
    private ModelMapper mapper;
    @Value("${non-editable-statuses}")
    private String[] nonEditableStatuses;

    public List<SimpleStatusDTO> getAllStatus(String boardId) {
        Board board = boardRepository.findById(boardId).orElseThrow(() -> new NotFoundException("Board not found: " + boardId));
        try {
            if (board.getIsCustomStatus()) {
                return listMapper.mapList(statusRepository.findAllByBoardId(boardId), SimpleStatusDTO.class, mapper);
            } else {
                return listMapper.mapList(statusRepository.findAllDefaultStatus(), SimpleStatusDTO.class, mapper);
            }
        } catch (Exception e) {
            throw new BadRequestException("Failed to get all status");
        }
    }

    public SimpleStatusDTO getStatusById(String boardId, Integer statusId) {
        return mapper.map(getStatus(boardId, statusId), SimpleStatusDTO.class);
    }

    public TaskLimit getStatusLimit(String boardId) {
        return limitRepository.findByBoardId(boardId).orElseThrow(() -> new BadRequestException("Limit not found"));
    }

    @Transactional
    public SimpleStatusDTO createStatus(String boardId, SimpleStatusDTO statusDTO) {
        Board board = boardRepository.findById(boardId).orElseThrow(() -> new NotFoundException("Board not found: " + boardId));
        if (!board.getIsCustomStatus()) {
            copyDefaultStatusesToBoard(boardId, board);
        }
        try {
            TaskStatus status = mapper.map(statusDTO, TaskStatus.class);
            status.setBoardId(boardId);
            TaskStatus newStatus = statusRepository.save(status);
            return mapper.map(newStatus, SimpleStatusDTO.class);
        } catch (Exception e) {
            throw new BadRequestException("Failed to add This status");
        }
    }

    @Transactional
    public SimpleStatusDTO updateStatus(String boardId, Integer id, SimpleStatusDTO statusDTO) {
        ValidationException validationException = new ValidationException("Validation error. Check 'errors' field for details.");
        if (statusDTO.getId() != null && id != statusDTO.getId()) {
            validationException.addValidationError("id", "Id in path and body must be the same");
            throw validationException;
        }
        if (statusRepository.existsByNameAndIdNotAndBoardId(statusDTO.getName(), id, boardId)) {
            validationException.addValidationError("name", "must be unique");
            throw validationException;
        }
        Board board = boardRepository.findById(boardId).orElseThrow(() -> new NotFoundException("Board not found: " + boardId));
//        TaskStatus oldStatus = statusRepository.findById(id).orElseThrow(() -> new BadRequestException("the specified status does not exist"));
        TaskStatus oldStatus = getStatus(boardId, id);
        if (Arrays.asList(nonEditableStatuses).contains(oldStatus.getName())) {
            throw new BadRequestException("This status " + oldStatus.getName() + " cannot be updated");
        }

        if (!board.getIsCustomStatus()) {
            List<TaskStatus> statusList = copyDefaultStatusesToBoard(boardId, board);
            System.out.println(statusList);
            TaskStatus newStatus;
            for (TaskStatus status : statusList) {
                if (status.getName().equals(oldStatus.getName())) {
                    newStatus = status;
                    id = newStatus.getId();
                }
            }
        }

        try {
            TaskStatus status = mapper.map(statusDTO, TaskStatus.class);
            status.setId(id);
            status.setBoardId(boardId);
            TaskStatus updatedStatus = statusRepository.save(status);
            return mapper.map(updatedStatus, SimpleStatusDTO.class);
        } catch (Exception e) {
            throw new BadRequestException("Failed to update This status");
        }
    }

    @Transactional
    public List<StatusLimitResponseDTO> updateLimitMaxiMunTask(String boardId, Integer maximumTask, Boolean isLimit) {
        Board board = boardRepository.findById(boardId).orElseThrow(() -> new NotFoundException("Board not found: " + boardId));
        try {
            limitRepository.updateLimit(boardId, maximumTask, isLimit);
            List<StatusLimitResponseDTO> statusLimits = statusRepository.countTaskByStatus(boardId);
            System.out.println(statusLimits);
            statusLimits = statusLimits.stream()
                    .filter(statusLimit -> !Arrays.asList(nonEditableStatuses).contains(statusLimit.getName()))
                    .collect(Collectors.toList());
            statusLimits.forEach(statusLimit -> {
                if (statusLimit.getNoOfTasks() >= maximumTask) {
                    List<TaskList> tasks = taskRepository.findAllByStatusId(statusLimit.getId());
                    List<TaskInLimitDTO> taskInLimitDTOS = listMapper.mapList(tasks, TaskInLimitDTO.class, mapper);
                    statusLimit.setTasks(taskInLimitDTOS);
                }
            });
                System.out.println(statusLimits);
            return statusLimits;
        } catch (Exception e) {
            throw new BadRequestException("Failed to update This status");
        }
    }

    @Transactional
    public SimpleStatusDTO deleteStatus(String boardId, Integer id) {
//        Board board = boardRepository.findById(boardId).orElseThrow(() -> new NotFoundException("Board not found: " + boardId));
//        TaskStatus status = statusRepository.findByBoardIdAndId(boardId,id).orElseThrow(() -> new BadRequestException("the specified status does not exist"));
        TaskStatus status = getStatus(boardId, id);
        StatusCountDTO statusCount = taskRepository.countByStatusIdAndReturnName(boardId, id);
        if (Arrays.asList(nonEditableStatuses).contains(status.getName())) {
            throw new BadRequestException("This status " + status.getName() + " cannot be deleted");
        }
        if (statusCount != null && statusCount.getCount() >= 0) {
            throw new BadRequestException("This status " + status.getName() + " cannot be deleted because it has tasks");
        }
        try {
            statusRepository.delete(status);
            return mapper.map(status, SimpleStatusDTO.class);
        } catch (Exception e) {
            throw new BadRequestException("Failed to delete This status");
        }
    }

    @Transactional
    public SimpleStatusDTO deleteStatusAndTransferStatusInAllTask(String boardId, Integer id, Integer newId) {
//        Board board = boardRepository.findById(boardId).orElseThrow(() -> new NotFoundException("Board not found: " + boardId));
//        TaskStatus status = statusRepository.findByBoardIdAndId(boardId,id).orElseThrow(() -> new BadRequestException("the specified status for task transfer does not exist"));
//        TaskStatus newStatus = statusRepository.findByBoardIdAndId(boardId,newId).orElseThrow(() -> new BadRequestException("the specified status for task transfer does not exist"));
        TaskStatus status = getStatus(boardId, id);
        TaskStatus newStatus = getStatus(boardId, newId);
        if (id.equals(newId)) {
            throw new BadRequestException("destination status for task transfer must be different from current status");
        }
        StatusCountDTO statusCountNewId = taskRepository.countByStatusIdAndReturnName(boardId, newId);
        StatusCountDTO statusCount = taskRepository.countByStatusIdAndReturnName(boardId, id);
        TaskLimit taskLimit = limitRepository.findByBoardId(boardId).orElseThrow(() -> new NotFoundException("Limit not found"));
        if (statusCount != null && taskLimit.getIsLimit() && statusCount.getCount() > taskLimit.getMaximumTask() && !Arrays.asList(nonEditableStatuses).contains(newStatus.getName())) {
            throw new BadRequestException("This status " + status.getName() + " has reached the maximum limit of " + taskLimit.getMaximumTask());
        }
        if (statusCountNewId != null && (statusCountNewId.getCount() + statusCount.getCount()) > taskLimit.getMaximumTask() && taskLimit.getIsLimit() && !Arrays.asList(nonEditableStatuses).contains(newStatus.getName())) {
            throw new BadRequestException("This task cannot be updated because the status " + statusCountNewId.getName() + " has reached the maximum limit of " + taskLimit.getMaximumTask());
        }

        try {
            taskRepository.updateStatusIdAllTaskList(boardId, id, newId);
            statusRepository.delete(status);
            return mapper.map(status, SimpleStatusDTO.class);
        } catch (Exception e) {
            throw new BadRequestException("Failed to delete This status");
        }
    }

    @Transactional
    public List<TaskStatus> copyDefaultStatusesToBoard(String boardId, Board board) {
        try {
            List<TaskStatus> defaultStatus = statusRepository.findAllDefaultStatus();
            board.setIsCustomStatus(true);
            boardRepository.save(board);
            List<TaskStatus> statuses = new ArrayList<>();
            for (TaskStatus status : defaultStatus) {
                TaskStatus copyStatus = new TaskStatus();
                copyStatus.setName(status.getName());
                copyStatus.setDescription(status.getDescription());
                copyStatus.setColor(status.getColor());
                copyStatus.setBoardId(boardId);
                TaskStatus newStatus = statusRepository.save(copyStatus);
                statusRepository.updateStatusIdAllTaskList(boardId, status.getId(), newStatus.getId());
                statuses.add(newStatus);
            }
            return statuses;
        } catch (Exception e) {
            throw new BadRequestException("Failed to copy default statuses.");
        }
    }

    public TaskStatus getStatus(String boardId, Integer statusId) {
        Board board = boardRepository.findById(boardId).orElseThrow(() -> new NotFoundException("Board not found: " + boardId));
        TaskStatus status;
        if (board.getIsCustomStatus()) {
            status = statusRepository.findByBoardIdAndId(boardId, statusId).orElseThrow(() -> new NotFoundException("Status Id not found: " + statusId));
        } else {
            status = statusRepository.findById(statusId).orElseThrow(() -> new NotFoundException("Status Id not found: " + statusId));
        }
        return status;
    }
}
