package sit.us1.backend.utils;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.springframework.stereotype.Component;
import sit.us1.backend.exceptions.NotFoundException;
import sit.us1.backend.exceptions.ValidationException;

import java.util.Set;
@Component
public class ValidationUtil {

    private final Validator validator;

    public ValidationUtil(Validator validator) {
        this.validator = validator;
    }

    public <T> void validateAndThrow(T object, Class<?>... groups) {
        Set<ConstraintViolation<T>> violations = validator.validate(object, groups);
        if (!violations.isEmpty()) {
            if(violations.iterator().next().getPropertyPath().toString().isEmpty() || violations.iterator().next().getPropertyPath().toString().isBlank()){
                throw new NotFoundException(violations.iterator().next().getMessage());
            }
            else {
                ValidationException validationException = new ValidationException("Validation error. Check 'errors' field for details.");
                violations.forEach(violation -> {
                    validationException.addValidationError(violation.getPropertyPath().toString(), violation.getMessage());
                });
                throw validationException;
            }
        }
    }
}