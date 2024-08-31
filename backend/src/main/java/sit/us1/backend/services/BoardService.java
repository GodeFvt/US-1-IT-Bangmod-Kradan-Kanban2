package sit.us1.backend.services;

import com.soundicly.jnanoidenhanced.jnanoid.NanoIdUtils;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import sit.us1.backend.dtos.boardsDTO.BoardRequestDTO;
import sit.us1.backend.dtos.boardsDTO.SimpleBoardDTO;
import sit.us1.backend.dtos.tasksDTO.SimpleTaskDTO;
import sit.us1.backend.entities.taskboard.Board;
import sit.us1.backend.entities.taskboard.BoardUser;
import sit.us1.backend.exceptions.BadRequestException;
import sit.us1.backend.repositories.taskboard.BoardRepository;

import java.util.List;

@Service
public class BoardService {
    @Autowired
    private BoardRepository boardRepository;
    @Autowired
    private ListMapper listMapper;
    @Autowired
    private ModelMapper mapper;

    public List<SimpleBoardDTO> getAllBoard() {
        return listMapper.mapList(boardRepository.findAll(), SimpleBoardDTO.class, mapper);
    }

    public List<SimpleBoardDTO> getAllBoardByOid() {
        String Oid = SecurityUtil.getCurrentUserDetails().getOid();
        return listMapper.mapList(boardRepository.findAllByOwner_Id(Oid), SimpleBoardDTO.class, mapper);
    }

    public SimpleBoardDTO createBoard(BoardRequestDTO newBoard) {
        Board board = mapper.map(newBoard, Board.class);
        board.setId(NanoIdUtils.randomNanoId(10));
        BoardUser owner = new BoardUser();
        owner.setId(SecurityUtil.getCurrentUserDetails().getOid());
        owner.setName(SecurityUtil.getCurrentUserDetails().getName());
        board.setOwner(owner);
        board.setIsCustomStatus(false);
        try {
            boardRepository.save(board);
        }catch (Exception e){
            board.setId(NanoIdUtils.randomNanoId(10));
            boardRepository.save(board);
        }
        return mapper.map(board, SimpleBoardDTO.class);
    }

    public SimpleBoardDTO getBoardById(String id) {
        return mapper.map(boardRepository.findById(id).orElseThrow(() -> new BadRequestException("the specified board does not exist")), SimpleBoardDTO.class);
    }

    public SimpleBoardDTO deleteBoardById(String id) {
        try {
            Board board = boardRepository.findById(id).orElseThrow(() -> new BadRequestException("the specified board does not exist"));
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
