package sit.us1.backend.validations;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import sit.us1.backend.repositories.taskboard.TaskStatusRepository;

import java.util.Arrays;

public class UniqueStatusNameValidator implements ConstraintValidator<ValidUniqueStatusName, String> {

    @Autowired
    private TaskStatusRepository statusRepository;
    @Value("${non-editable-statuses}")
    private String[] nonEditableStatuses;

    @Override
    public void initialize(ValidUniqueStatusName constraintAnnotation) {
    }

    @Override
    public boolean isValid(String statusName, ConstraintValidatorContext context) {
        if(Arrays.asList(nonEditableStatuses).contains(statusName)) {
            return false;
        }
        return statusName != null && !statusRepository.existsByName(statusName);
    }
}
