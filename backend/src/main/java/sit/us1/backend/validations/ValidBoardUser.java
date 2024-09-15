package sit.us1.backend.validations;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;
@Constraint(validatedBy = BoardUserValidator.class)
@Documented
@Target({ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidBoardUser {
    String message() default "You are not the owner of this board";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
