package sit.us1.backend.validations;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = TaskStatusLimitValidator.class)
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidTaskStatusLimit {
    String message() default "The status has reached the limit";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
