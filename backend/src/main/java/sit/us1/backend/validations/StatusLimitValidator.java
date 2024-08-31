package sit.us1.backend.validations;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import sit.us1.backend.dtos.statusesDTO.StatusValidDTO;
import sit.us1.backend.dtos.tasksDTO.StatusCountDTO;
import sit.us1.backend.entities.taskboard.TaskLimit;
import sit.us1.backend.exceptions.NotFoundException;
import sit.us1.backend.repositories.taskboard.TaskLimitRepository;
import sit.us1.backend.repositories.taskboard.TaskListRepository;

import java.util.Arrays;

@Component
public class StatusLimitValidator implements ConstraintValidator<ValidStatusLimit, StatusValidDTO> {

    @Autowired
    private TaskListRepository repository;

    @Autowired
    private TaskLimitRepository limitRepository;

    @Value("${non-editable-statuses}")
    private String[] nonEditableStatuses;


    @Override
    public void initialize(ValidStatusLimit status) {
    }

    @Override
    public boolean isValid(StatusValidDTO statusAllId, ConstraintValidatorContext context) {
        StatusCountDTO statusCount = repository.countByStatusIdAndReturnName(statusAllId.getBoardId(),statusAllId.getNewStatusId());
        TaskLimit taskLimit = limitRepository.findByBoardId(statusAllId.getBoardId()).orElseThrow(() -> new NotFoundException("Limit not found"));
        if (statusCount != null  && taskLimit.getIsLimit() && statusCount.getCount() >= taskLimit.getMaximumTask() && !Arrays.asList(nonEditableStatuses).contains(statusCount.getName())) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("The destination status cannot be over the limit after transfer")
                    .addConstraintViolation();
            return false;
        } else {
            return true;
        }
    }
}
