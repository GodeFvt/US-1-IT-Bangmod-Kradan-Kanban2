package sit.us1.backend.services;

import com.soundicly.jnanoidenhanced.jnanoid.NanoIdUtils;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import sit.us1.backend.dtos.boardsDTO.BoardRequestDTO;
import sit.us1.backend.dtos.boardsDTO.SimpleBoardDTO;
import sit.us1.backend.dtos.tasksDTO.SimpleTaskDTO;
import sit.us1.backend.entities.taskboard.*;
import sit.us1.backend.exceptions.BadRequestException;
import sit.us1.backend.repositories.taskboard.BoardRepository;
import sit.us1.backend.repositories.taskboard.TaskLimitRepository;
import sit.us1.backend.repositories.taskboard.TaskListRepository;
import sit.us1.backend.repositories.taskboard.TaskStatusRepository;

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
    private ListMapper listMapper;
    @Autowired
    private ModelMapper mapper;

    public List<SimpleBoardDTO> getAllBoard() {
        return listMapper.mapList(boardRepository.findAll(), SimpleBoardDTO.class, mapper);
    }

    public List<SimpleBoardDTO> getAllBoardByOid() {
        try {
            String Oid = SecurityUtil.getCurrentUserDetails().getOid();
            return listMapper.mapList(boardRepository.findAllByOwner_Id(Oid), SimpleBoardDTO.class, mapper);
        } catch (Exception e) {
            throw new BadRequestException("the specified board does not exist");
        }
    }

    public SimpleBoardDTO createBoard(BoardRequestDTO newBoard) {
        Board board = mapper.map(newBoard, Board.class);
        board.setId(NanoIdUtils.randomNanoId(10));
        BoardUser owner = new BoardUser();
        owner.setId(SecurityUtil.getCurrentUserDetails().getOid());
        board.setOwner(owner);
        board.setIsCustomStatus(false);
        TaskLimit taskLimit = new TaskLimit();
        taskLimit.setIsLimit(false);
        taskLimit.setMaximumTask(10);
        try {
            taskLimit.setBoardId(boardRepository.save(board).getId());
        } catch (Exception e) {
            board.setId(NanoIdUtils.randomNanoId(10));
            taskLimit.setBoardId(boardRepository.save(board).getId());
        }
        try {
            taskLimitRepository.save(taskLimit);
        } catch (Exception e) {
            throw new BadRequestException("Cannot create board");
        }
        return mapper.map(board, SimpleBoardDTO.class);
    }

    public SimpleBoardDTO getBoardById(String id) {
        return mapper.map(boardRepository.findById(id).orElseThrow(() -> new BadRequestException("the specified board does not exist")), SimpleBoardDTO.class);
    }

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

    public SimpleBoardDTO updateBoardById(String id, BoardRequestDTO newBoard) {
        Board board = boardRepository.findById(id).orElseThrow(() -> new BadRequestException("the specified board does not exist"));
        board.setName(newBoard.getName());
        boardRepository.save(board);
        return mapper.map(board, SimpleBoardDTO.class);
    }
}
