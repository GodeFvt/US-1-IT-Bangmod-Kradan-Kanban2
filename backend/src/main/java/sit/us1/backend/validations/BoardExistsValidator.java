package sit.us1.backend.validations;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.beans.factory.annotation.Autowired;
import sit.us1.backend.entities.account.CustomUserDetails;
import sit.us1.backend.entities.taskboard.Board;
import sit.us1.backend.exceptions.NotFoundException;
import sit.us1.backend.repositories.taskboard.BoardRepository;
import sit.us1.backend.services.SecurityUtil;

import java.util.Optional;

public class BoardExistsValidator implements ConstraintValidator<ValidBoardExists, String> {
    @Autowired
    private BoardRepository boardRepository;

    @Override
    public void initialize(ValidBoardExists boardId) {
    }

    @Override
    public boolean isValid(String boardId, ConstraintValidatorContext context) {
        Optional<Board> board = boardRepository.findById(boardId);
        if (board.isEmpty()) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("Board Id does not exist")
                    .addConstraintViolation();
            return false;
        } else {
            return true;
        }
    }
}