package sit.us1.backend.validations;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import sit.us1.backend.dtos.statusesDTO.StatusValidDTO;
import sit.us1.backend.dtos.tasksDTO.StatusCountDTO;
import sit.us1.backend.entities.taskboard.Board;
import sit.us1.backend.entities.taskboard.TaskLimit;
import sit.us1.backend.exceptions.NotFoundException;
import sit.us1.backend.repositories.taskboard.BoardRepository;
import sit.us1.backend.repositories.taskboard.TaskLimitRepository;
import sit.us1.backend.repositories.taskboard.TaskListRepository;

import java.util.Arrays;
import java.util.Optional;

@Component
public class StatusLimitValidator implements ConstraintValidator<ValidStatusLimit, StatusValidDTO> {

    @Autowired
    private TaskListRepository repository;
    @Autowired
    private TaskLimitRepository limitRepository;
    @Autowired
    private BoardRepository boardRepository;

    @Value("${non-editable-statuses}")
    private String[] nonEditableStatuses;

    @Override
    public void initialize(ValidStatusLimit status) {
    }

    @Override
    public boolean isValid(StatusValidDTO statusAllId, ConstraintValidatorContext context) {
        if (statusAllId.getOnPathStatusId() == null) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("Status Id field is required")
                    .addConstraintViolation();
            return false;
        }
        Optional<Board> board = boardRepository.findById(statusAllId.getBoardId());
        Optional<TaskLimit> taskLimit = limitRepository.findByBoardId(statusAllId.getBoardId());
        if(board.isEmpty()||taskLimit.isEmpty()){
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("Board Id does not exist")
                    .addConstraintViolation();
            return false;
        }
        StatusCountDTO statusCount = repository.countByStatusIdAndReturnName(statusAllId.getBoardId(),statusAllId.getNewStatusId());

        if (statusCount != null  && taskLimit.get().getIsLimit() && statusCount.getCount() >= taskLimit.get().getMaximumTask() && !Arrays.asList(nonEditableStatuses).contains(statusCount.getName())) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("The destination status cannot be over the limit after transfer")
                    .addConstraintViolation();
            return false;
        } else {
            return true;
        }
    }
}
