package sit.us1.backend.validations;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Constraint(validatedBy = BoardExistsValidator.class)
@Documented
@Target({ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidBoardExists {
    String message() default "Board Id does not exist";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
