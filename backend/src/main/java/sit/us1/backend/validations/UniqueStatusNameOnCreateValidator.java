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

public class UniqueStatusNameOnCreateValidator implements ConstraintValidator<ValidUniqueStatusNameOnCreate, StatusValidDTO> {

    @Autowired
    private TaskStatusRepository statusRepository;
    @Autowired
    private BoardRepository boardRepository;
    @Value("${non-editable-statuses}")
    private String[] nonEditableStatuses;

    @Override
    public void initialize(ValidUniqueStatusNameOnCreate constraintAnnotation) {
    }

    @Override
    public boolean isValid(StatusValidDTO statusValid, ConstraintValidatorContext context) {
        if(Arrays.asList(nonEditableStatuses).contains(statusValid.getName())) {
            return false;
        }
        Optional<Board> board = boardRepository.findById(statusValid.getBoardId());
        if(board.isEmpty()){
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("Board Id does not exist")
                    .addConstraintViolation();
            return false;
        }
        if(board.get().getIsCustomStatus()){
            return statusValid.getName() != null && !statusRepository.existsByNameAndBoardId(statusValid.getName(), statusValid.getBoardId());
        }else {
            return true;
        }

    }
}
