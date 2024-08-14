package sit.us1.backend.validations;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import sit.us1.backend.dtos.tasksDTO.StatusCountDTO;
import sit.us1.backend.entities.TaskLimit;
import sit.us1.backend.entities.TaskStatus;
import sit.us1.backend.exceptions.NotFoundException;
import sit.us1.backend.repositories.TaskLimitRepository;
import sit.us1.backend.repositories.TaskListRepository;
import sit.us1.backend.repositories.TaskStatusRepository;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Component
public class StatusIdValidator implements ConstraintValidator<ValidStatusId, Integer> {

    @Autowired
    private TaskStatusRepository statusRepository;

    @Value("${non-editable-statuses}")
    private String[] nonEditableStatuses;


    @Override
    public void initialize(ValidStatusId status) {
    }

    @Override
    public boolean isValid(Integer statusIdField, ConstraintValidatorContext context) {
        if (statusIdField == null) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("Status Id field is required")
                    .addConstraintViolation();
            return false;
        }
        Optional<TaskStatus> status = statusRepository.findById(statusIdField);
        if (status.isPresent()) {
            if (Arrays.asList(nonEditableStatuses).contains(status.get().getName())) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate("Cannot be " + status.get().getName())
                        .addConstraintViolation();
                return false;
            }
            return true;
        } else {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("Status Id does not exist")
                    .addConstraintViolation();
            return false;
        }
    }
}
