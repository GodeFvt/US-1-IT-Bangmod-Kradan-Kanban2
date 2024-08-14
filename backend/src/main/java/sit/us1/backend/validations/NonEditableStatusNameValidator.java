package sit.us1.backend.validations;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.beans.factory.annotation.Value;

import java.util.Arrays;

public class NonEditableStatusNameValidator implements ConstraintValidator<ValidNonEditableNameStatus, String> {

    @Value("${non-editable-statuses}")
    private String[] nonEditableStatuses;

    @Override
    public void initialize(ValidNonEditableNameStatus status) {
    }

    @Override
    public boolean isValid(String statusNameField, ConstraintValidatorContext context) {
        if(statusNameField == null ) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("Status name field is required")
                    .addConstraintViolation();
            return false;
        }
        if (Arrays.asList(nonEditableStatuses).contains(statusNameField)) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("Cannot be " + statusNameField)
                    .addConstraintViolation();
            return false;
        } else {
            return true;
        }
    }
}

