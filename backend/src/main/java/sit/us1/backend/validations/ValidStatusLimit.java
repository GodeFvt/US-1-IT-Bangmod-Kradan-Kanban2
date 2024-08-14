package sit.us1.backend.validations;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = StatusLimitValidator.class)
@Target({ElementType.METHOD, ElementType.FIELD ,ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidStatusLimit {
    String message() default "The destination status cannot be over the limit after transfer";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
