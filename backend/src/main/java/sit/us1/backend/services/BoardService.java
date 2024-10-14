package sit.us1.backend.services;

import com.soundicly.jnanoidenhanced.jnanoid.NanoIdUtils;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sit.us1.backend.dtos.boardsDTO.BoardRequestDTO;
import sit.us1.backend.dtos.boardsDTO.SimpleBoardDTO;
import sit.us1.backend.dtos.boardsDTO.SimpleCollaboratorDTO;
import sit.us1.backend.dtos.tasksDTO.SimpleTaskDTO;
import sit.us1.backend.entities.account.CustomUserDetails;
import sit.us1.backend.entities.account.User;
import sit.us1.backend.entities.taskboard.*;
import sit.us1.backend.exceptions.BadRequestException;
import sit.us1.backend.exceptions.ConflictException;
import sit.us1.backend.exceptions.NotFoundException;
import sit.us1.backend.repositories.account.UserRepository;
import sit.us1.backend.repositories.taskboard.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static sit.us1.backend.entities.taskboard.Board.Visibility.*;

@Service
public class BoardService {
    @Autowired
    private BoardRepository boardRepository;
    @Autowired
    private TaskLimitRepository taskLimitRepository;
    @Autowired
    private TaskStatusRepository taskStatusRepository;
    @Autowired
    private TaskListRepository taskListRepository;
    @Autowired
    private BoardUserRepository boardUserRepository;
    @Autowired
    private CollaborationRepository collaborationRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ListMapper listMapper;
    @Autowired
    private ModelMapper mapper;

    public List<SimpleBoardDTO> getAllBoard() {
        return listMapper.mapList(boardRepository.findAll(), SimpleBoardDTO.class, mapper);
    }

    public boolean isBoardPublic(String boardId) {
        Board board = boardRepository.findById(boardId).orElseThrow(() -> new NotFoundException("the specified board does not exist"));
        return PUBLIC.equals(board.getVisibility());
    }

    public boolean isOwnerOfBoard(String boardId, String oid) {
        Board board = boardRepository.findById(boardId).orElseThrow(() -> new NotFoundException("the specified board does not exist"));
        return board.getOwner().getId().equals(oid);
    }

    public boolean boardExists(String boardId) {
        if (boardId == null) {
            throw new BadRequestException("the specified board does not exist");
        }
        return boardRepository.existsById(boardId);
    }



    public List<SimpleBoardDTO> getAllBoardByOid() {
        try {
            String Oid = SecurityUtil.getCurrentUserDetails().getOid();
            return listMapper.mapList(boardRepository.findAllByOwnerOrCollaboration(Oid), SimpleBoardDTO.class, mapper);
        } catch (Exception e) {
            throw new BadRequestException("the specified board does not exist");
        }
    }

    public SimpleBoardDTO getBoardById(String id) {
        return mapper.map(boardRepository.findById(id).orElseThrow(() -> new BadRequestException("the specified board does not exist")), SimpleBoardDTO.class);
    }

    @Transactional
    public SimpleBoardDTO createBoard(BoardRequestDTO newBoard) {
        String Oid = SecurityUtil.getCurrentUserDetails().getOid();
        CustomUserDetails userDetails = SecurityUtil.getCurrentUserDetails();
        try {
            if (boardUserRepository.findById(Oid).isEmpty()) {
                BoardUser user = new BoardUser();
                user.setId(userDetails.getOid());
                user.setUsername(userDetails.getUsername());
                user.setName(userDetails.getName());
                boardUserRepository.save(user);
            }
        } catch (Exception e) {
            throw new BadRequestException("Cannot create user");
        }
        Board board = mapper.map(newBoard, Board.class);
        try {
            String boardId;
            do {
                boardId = NanoIdUtils.randomNanoId(10);
            } while (boardRepository.existsById(boardId));
            board.setId(boardId);
            BoardUser owner = new BoardUser();
            owner.setId(SecurityUtil.getCurrentUserDetails().getOid());
            owner.setUsername(SecurityUtil.getCurrentUserDetails().getUsername());
            board.setOwner(owner);
            board.setIsCustomStatus(false);
            board.setVisibility(PRIVATE);
            board.setCollaborators(new ArrayList<>());
            TaskLimit taskLimit = new TaskLimit();
            taskLimit.setIsLimit(false);
            taskLimit.setMaximumTask(10);
            taskLimit.setBoardId(boardRepository.save(board).getId());
            taskLimitRepository.save(taskLimit);
        } catch (Exception e) {
            throw new BadRequestException("Cannot create board");
        }
        return mapper.map(board, SimpleBoardDTO.class);
    }

    @Transactional
    public SimpleBoardDTO deleteBoardById(String id) {
        List<TaskList> allTask = taskListRepository.findAllByBoard_Id(id);
        List<Collaboration> allCollaboration = collaborationRepository.findAllByBoardId(id);
        TaskLimit taskLimits = taskLimitRepository.findByBoardId(id).orElseThrow(() -> new BadRequestException("the specified board does not exist"));
        try {
            collaborationRepository.deleteAll(allCollaboration);
            taskListRepository.deleteAll(allTask);
            Board board = boardRepository.findById(id).orElseThrow(() -> new BadRequestException("the specified board does not exist"));
            List<TaskStatus> allStatus = taskStatusRepository.findAllByBoardId(id);
            if (board.getIsCustomStatus()) {
                taskStatusRepository.deleteAll(allStatus);
            }

            taskLimitRepository.delete(taskLimits);
            boardRepository.delete(board);
            return mapper.map(board, SimpleBoardDTO.class);
        } catch (Exception e) {
            throw new BadRequestException("the specified board does not exist");
        }

    }

    @Transactional
    public SimpleBoardDTO updateBoardById(String id, BoardRequestDTO newBoard) {
        Board board = boardRepository.findById(id).orElseThrow(() -> new BadRequestException("the specified board does not exist"));
        board.setName(newBoard.getName());
        try {
            boardRepository.save(board);
        } catch (Exception e) {
            throw new BadRequestException("Cannot update board");
        }
        return mapper.map(board, SimpleBoardDTO.class);
    }

    @Transactional
    public SimpleBoardDTO updateVisibilityById(String id, Board.Visibility visibility) {
        Board board = boardRepository.findById(id).orElseThrow(() -> new BadRequestException("the specified board does not exist"));
        board.setVisibility(visibility);
        try {
            boardRepository.save(board);
        } catch (Exception e) {
            throw new BadRequestException("Cannot update board");
        }
        return mapper.map(board, SimpleBoardDTO.class);
    }

}
