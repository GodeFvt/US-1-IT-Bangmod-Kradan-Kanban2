package sit.us1.backend.validations;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import sit.us1.backend.dtos.statusesDTO.StatusValidDTO;
import sit.us1.backend.entities.taskboard.Board;
import sit.us1.backend.repositories.taskboard.BoardRepository;
import sit.us1.backend.repositories.taskboard.TaskStatusRepository;

import java.util.Arrays;
import java.util.Optional;

public class UniqueStatusNameOuUpdateValidator implements ConstraintValidator<ValidUniqueStatusNameOnUpdate, StatusValidDTO> {

    @Autowired
    private TaskStatusRepository statusRepository;
    @Autowired
    private BoardRepository boardRepository;
    @Value("${non-editable-statuses}")
    private String[] nonEditableStatuses;

    @Override
    public void initialize(ValidUniqueStatusNameOnUpdate constraintAnnotation) {
    }

    @Override
    public boolean isValid(StatusValidDTO statusValid, ConstraintValidatorContext context) {
        Optional<Board> board = boardRepository.findById(statusValid.getBoardId());
        if (Arrays.asList(nonEditableStatuses).contains(statusValid.getName())) {
            return false;
        } else if (statusValid.getOnStatusDTOId() != null && statusValid.getOnPathStatusId() != statusValid.getOnStatusDTOId()) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("Id in path and body must be the same")
                    .addPropertyNode("id")
                    .addConstraintViolation();
            return false;
        } else if (board.isEmpty()) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("Board Id does not exist")
                    .addConstraintViolation();
            return false;
        } else if (statusRepository.existsByNameAndIdNotAndBoardId(statusValid.getName(), statusValid.getOnPathStatusId(), statusValid.getBoardId())) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("must be unique")
                    .addPropertyNode("name")
                    .addConstraintViolation();
            return false;
        } else {
            return true;
        }


    }
}