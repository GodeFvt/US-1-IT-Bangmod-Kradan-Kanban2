package sit.us1.backend.validations;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = UniqueStatusNameOuUpdateValidator.class)
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidUniqueStatusNameOnUpdate {
    String message() default "Status name must be unique";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
