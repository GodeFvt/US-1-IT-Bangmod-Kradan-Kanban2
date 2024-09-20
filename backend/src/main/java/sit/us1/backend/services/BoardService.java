package sit.us1.backend.services;

import com.soundicly.jnanoidenhanced.jnanoid.NanoIdUtils;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sit.us1.backend.dtos.boardsDTO.BoardRequestDTO;
import sit.us1.backend.dtos.boardsDTO.SimpleBoardDTO;
import sit.us1.backend.dtos.tasksDTO.SimpleTaskDTO;
import sit.us1.backend.entities.account.CustomUserDetails;
import sit.us1.backend.entities.taskboard.*;
import sit.us1.backend.exceptions.BadRequestException;
import sit.us1.backend.exceptions.NotFoundException;
import sit.us1.backend.repositories.taskboard.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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
    BoardUserRepository boardUserRepository;
    @Autowired
    private ListMapper listMapper;
    @Autowired
    private ModelMapper mapper;

    public List<SimpleBoardDTO> getAllBoard() {
        return listMapper.mapList(boardRepository.findAll(), SimpleBoardDTO.class, mapper);
    }

    public boolean isBoardPublic(String boardId) {
        if (boardId == null) {
            throw new BadRequestException("the specified board does not exist");
        }

        Board board = boardRepository.findById(boardId).orElseThrow(() -> new NotFoundException("the specified board does not exist"));
        System.out.println("PUBLIC".equalsIgnoreCase(board.getVisibility()));
        return "PUBLIC".equalsIgnoreCase(board.getVisibility());
    }

    public boolean isOwnerOfBoard(String boardId, String oid) {
        if (boardId == null) {
            throw new BadRequestException("the specified board does not exist");
        }
        Board board = boardRepository.findById(boardId).orElseThrow(() -> new NotFoundException("the specified board does not exist"));
        return board.getOwner().getId().equals(oid);
    }

    public List<SimpleBoardDTO> getAllBoardByOid() {
        try {
            String Oid = SecurityUtil.getCurrentUserDetails().getOid();
            return listMapper.mapList(boardRepository.findAllByOwner_Id(Oid), SimpleBoardDTO.class, mapper);
        } catch (Exception e) {
            throw new BadRequestException("the specified board does not exist");
        }
    }

    @Transactional
    public SimpleBoardDTO createBoard(BoardRequestDTO newBoard) {
        String Oid = SecurityUtil.getCurrentUserDetails().getOid();
        CustomUserDetails userDetails = SecurityUtil.getCurrentUserDetails();
        try {
            if (boardUserRepository.findById(Oid).isEmpty()) {
                BoardUser user = new BoardUser();
                user.setId(userDetails.getOid());
                boardUserRepository.save(user);
            }
        } catch (Exception e) {
            throw new BadRequestException("Cannot create user");
        }
//        try {
//            taskLimit.setBoardId(boardRepository.save(board).getId());
//        } catch (Exception e) {
//            board.setId(NanoIdUtils.randomNanoId(10));
//            taskLimit.setBoardId(boardRepository.save(board).getId());
//        }
        Board board = mapper.map(newBoard, Board.class);
        try {
            String boardId;
            do {
                boardId = NanoIdUtils.randomNanoId(10);
            } while (boardRepository.existsById(boardId));
            board.setId(boardId);
            BoardUser owner = new BoardUser();
            owner.setId(SecurityUtil.getCurrentUserDetails().getOid());
            board.setOwner(owner);
            board.setIsCustomStatus(false);
            board.setVisibility("PRIVATE");
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

    public SimpleBoardDTO getBoardById(String id) {
        return mapper.map(boardRepository.findById(id).orElseThrow(() -> new BadRequestException("the specified board does not exist")), SimpleBoardDTO.class);
    }

    @Transactional
    public SimpleBoardDTO deleteBoardById(String id) {
        List<TaskList> allTask = taskListRepository.findAllByBoard_Id(id);
        TaskLimit taskLimits = taskLimitRepository.findByBoardId(id).orElseThrow(() -> new BadRequestException("the specified board does not exist"));
        try {
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
    public SimpleBoardDTO updateVisibilityById(String id, String visibility) {
        Board board = boardRepository.findById(id).orElseThrow(() -> new BadRequestException("the specified board does not exist"));
        if(visibility.equalsIgnoreCase("PUBLIC") || visibility.equalsIgnoreCase("PRIVATE")) {
            board.setVisibility(visibility);
        } else {
            throw new BadRequestException("Invalid visibility");
        }
        board.setVisibility(visibility);
        try {
            boardRepository.save(board);
        } catch (Exception e) {
            throw new BadRequestException("Cannot update board");
        }
        return mapper.map(board, SimpleBoardDTO.class);
    }


}
