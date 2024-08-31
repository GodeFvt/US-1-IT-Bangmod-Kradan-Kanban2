package sit.us1.backend.validations;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.ValidationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import sit.us1.backend.dtos.tasksDTO.StatusCountDTO;
import sit.us1.backend.dtos.tasksDTO.TaskRequestDTO;
import sit.us1.backend.entities.taskboard.TaskLimit;
import sit.us1.backend.entities.taskboard.TaskStatus;
import sit.us1.backend.repositories.taskboard.TaskLimitRepository;
import sit.us1.backend.repositories.taskboard.TaskListRepository;
import sit.us1.backend.repositories.taskboard.TaskStatusRepository;

import java.util.Arrays;
import java.util.Optional;

@Component
public class TaskStatusLimitValidator implements ConstraintValidator<ValidTaskStatusLimit, TaskRequestDTO> {
    @Autowired
    private TaskListRepository taskListRepository;
    @Autowired
    private TaskLimitRepository limitRepository;
    @Autowired
    private TaskStatusRepository statusRepository;
    @Value("${non-editable-statuses}")
    private String[] nonEditableStatuses;
    @Override
    public void initialize(ValidTaskStatusLimit constraintAnnotation) {
    }

    @Override
    public boolean isValid(TaskRequestDTO taskRequestDTO, ConstraintValidatorContext context) {
        String boardId = taskRequestDTO.getBoardId();
        if (boardId == null) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("Board ID is missing")
                    .addPropertyNode("boardId")
                    .addConstraintViolation();
            return false;
        }
        TaskLimit taskLimit = limitRepository.findByBoardId(boardId).orElseThrow(() -> new ValidationException("Limit not found"));
        Optional<TaskStatus> status = statusRepository.findByName(taskRequestDTO.getStatus());
        if (status.isPresent()) {
            StatusCountDTO statusCount = taskListRepository.countByStatusIdAndReturnName(boardId,status.get().getId());
            if (statusCount != null && taskLimit.getIsLimit() && statusCount.getCount() >= taskLimit.getMaximumTask() && !Arrays.asList(nonEditableStatuses).contains(status.get().getName())) {
                if (taskRequestDTO.getStatus().equals(status.get().getName())) {
                    context.disableDefaultConstraintViolation();
                    context.buildConstraintViolationWithTemplate("The status has reached the limit")
                            .addPropertyNode("status")
                            .addConstraintViolation();
                    return false;
                }
            }
            return true;
        } else {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("does not exist")
                    .addPropertyNode("status")
                    .addConstraintViolation();
            return false;
        }
    }

}