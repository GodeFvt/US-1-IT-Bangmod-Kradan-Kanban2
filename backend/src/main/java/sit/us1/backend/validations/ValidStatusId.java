package sit.us1.backend.validations;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = StatusIdValidator.class)
@Target({ElementType.METHOD, ElementType.FIELD ,ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidStatusId {
    String message() default "Status Id does not exist";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
