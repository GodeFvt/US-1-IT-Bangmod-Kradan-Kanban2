package sit.us1.backend.validations;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.beans.factory.annotation.Autowired;
import sit.us1.backend.entities.account.CustomUserDetails;
import sit.us1.backend.entities.taskboard.Board;
import sit.us1.backend.repositories.taskboard.BoardRepository;
import sit.us1.backend.utils.SecurityUtil;

import java.util.Optional;

public class BoardUserValidator implements ConstraintValidator<ValidBoardUser, String> {
    @Autowired
    private BoardRepository boardRepository;

    @Override
    public void initialize(ValidBoardUser boardId) {
    }

    @Override
    public boolean isValid(String boardId, ConstraintValidatorContext context) {
        Optional<Board> board = boardRepository.findById(boardId);
        if(board.isEmpty()){
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("Board Id does not exist")
                    .addConstraintViolation();
            return false;
        }
        CustomUserDetails currentUserOid = SecurityUtil.getCurrentUserDetails();
        if (currentUserOid != null && !board.get().getOwner().getId().equals(currentUserOid.getOid())) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("You are not the owner of this board")
                    .addConstraintViolation();
            return false;
        } else {
            return true;
        }
    }
}
