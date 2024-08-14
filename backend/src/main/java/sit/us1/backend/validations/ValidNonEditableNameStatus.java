package sit.us1.backend.validations;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = NonEditableStatusNameValidator.class)
@Target({ElementType.METHOD, ElementType.FIELD ,ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidNonEditableNameStatus {
    String message() default "Invalid status";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
