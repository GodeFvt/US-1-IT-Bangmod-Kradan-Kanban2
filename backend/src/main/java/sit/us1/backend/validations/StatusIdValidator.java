package sit.us1.backend.validations;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import sit.us1.backend.dtos.statusesDTO.StatusValidDTO;
import sit.us1.backend.entities.taskboard.Board;
import sit.us1.backend.entities.taskboard.TaskStatus;
import sit.us1.backend.repositories.taskboard.BoardRepository;
import sit.us1.backend.repositories.taskboard.TaskStatusRepository;

import java.util.Arrays;
import java.util.Optional;

@Component
public class StatusIdValidator implements ConstraintValidator<ValidStatusId, StatusValidDTO> {

    @Autowired
    private TaskStatusRepository statusRepository;

    @Autowired
    private BoardRepository boardRepository;

    @Value("${non-editable-statuses}")
    private String[] nonEditableStatuses;


    @Override
    public void initialize(ValidStatusId status) {
    }

    @Override
    public boolean isValid(StatusValidDTO statusAllId, ConstraintValidatorContext context) {
        if (statusAllId.getOnPathStatusId() == null) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("Status Id field is required")
                    .addConstraintViolation();
            return false;
        }
        Optional<Board> board = boardRepository.findById(statusAllId.getBoardId());
        if(board.isEmpty()){
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("Board Id does not exist")
                    .addConstraintViolation();
            return false;
        }
        Optional<TaskStatus> status ;
        if(board.get().getIsCustomStatus()){
            status = statusRepository.findByIdAndBoardId(statusAllId.getOnPathStatusId(), statusAllId.getBoardId());
        }else {
            status = statusRepository.findById(statusAllId.getOnPathStatusId());
        }

        if (status.isPresent()) {
            if (Arrays.asList(nonEditableStatuses).contains(status.get().getName())) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate("Cannot be " + status.get().getName())
                        .addConstraintViolation();
                return false;
            }
            return true;
        } else {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("Status Id does not exist")
                    .addConstraintViolation();
            return false;
        }
    }
}
