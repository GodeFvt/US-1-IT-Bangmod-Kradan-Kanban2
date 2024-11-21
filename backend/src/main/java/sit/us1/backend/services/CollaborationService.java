package sit.us1.backend.services;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sit.us1.backend.dtos.boardsDTO.CollaboratorResponseDTO;
import sit.us1.backend.dtos.boardsDTO.SimpleCollaboratorDTO;
import sit.us1.backend.entities.account.User;
import sit.us1.backend.entities.taskboard.Board;
import sit.us1.backend.entities.taskboard.BoardUser;
import sit.us1.backend.entities.taskboard.Collaboration;
import sit.us1.backend.entities.taskboard.CollaborationId;
import sit.us1.backend.exceptions.BadRequestException;
import sit.us1.backend.exceptions.ConflictException;
import sit.us1.backend.exceptions.NotFoundException;
import sit.us1.backend.repositories.account.UserRepository;
import sit.us1.backend.repositories.taskboard.BoardRepository;
import sit.us1.backend.repositories.taskboard.BoardUserRepository;
import sit.us1.backend.repositories.taskboard.CollaborationRepository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;

@Service
public class CollaborationService {
    @Autowired
    private CollaborationRepository collaborationRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private BoardRepository boardRepository;
    @Autowired
    private BoardUserRepository boardUserRepository;
    @Autowired
    private ListMapper listMapper;
    @Autowired
    private ModelMapper mapper;

    public boolean collaboratorExists(String oid) {
        return collaborationRepository.existsByOid(oid);
    }

    public boolean isCollaborator(String boardId, String oid) {
//        return collaborationRepository.existsById(new CollaborationId(boardId, oid));
        return collaborationRepository.existsByOidAndBoardIdAndIsPendingFalse(oid, boardId);
    }

    public Collaboration getCollaboration(String boardId, String oid) {
        return collaborationRepository.findById(new CollaborationId(boardId, oid)).orElseThrow(() -> new NotFoundException("the specified collaborator does not exist"));
    }

    public List<CollaboratorResponseDTO> getCollaborator(String id) {
        List<Collaboration> collaborations = collaborationRepository.findAllByBoardIdOrderByAddedOn(id);
        List<CollaboratorResponseDTO> collaboratorResponseDTOS = listMapper.mapList(collaborations, CollaboratorResponseDTO.class, mapper);

        Map<String, CollaboratorResponseDTO> collaboratorMap = collaboratorResponseDTOS.stream()
                .collect(Collectors.toMap(CollaboratorResponseDTO::getOid, Function.identity()));

        collaborations.forEach(collaboration -> {
            CollaboratorResponseDTO collaboratorResponseDTO = collaboratorMap.get(collaboration.getOid());
            if (collaboratorResponseDTO != null) {
                collaboratorResponseDTO.setName(collaboration.getBoardUser().getName());
                collaboratorResponseDTO.setEmail(collaboration.getBoardUser().getEmail());
            }
        });

        return collaboratorResponseDTOS;
    }

    public CollaboratorResponseDTO getCollaboratorById(String id, String oid) {
        Collaboration collaboration = collaborationRepository.findById(new CollaborationId(id, oid)).orElseThrow(() -> new NotFoundException("the specified collaborator does not exist"));
        CollaboratorResponseDTO collaboratorResponseDTO = mapper.map(collaboration, CollaboratorResponseDTO.class);
        collaboratorResponseDTO.setName(collaboration.getBoardUser().getName());
        collaboratorResponseDTO.setEmail(collaboration.getBoardUser().getEmail());
        return mapper.map(collaboration, CollaboratorResponseDTO.class);
    }

    @Transactional
    public CollaboratorResponseDTO addCollaborator(String id, SimpleCollaboratorDTO newCollab) {
        //Pessimistic Lock บล็อกการเข้าถึงจากคำขออื่น
        Board board = boardRepository.findById(id).orElseThrow(() -> new NotFoundException("The specified board does not exist"));
        CollaboratorResponseDTO collaboratorResponseDTO = mapper.map(newCollab, CollaboratorResponseDTO.class);

        User user = userRepository.findByEmail(newCollab.getEmail());
        if (user == null) {
            throw new NotFoundException("The specified user does not exist");
        }
        if (user.getOid().equals(SecurityUtil.getCurrentUserDetails().getOid())) {
            throw new ConflictException("Cannot add yourself as collaborator");
        }

        if (collaborationRepository.existsById(new CollaborationId(id, user.getOid()))) {
            throw new ConflictException("User already exists");
        }

        try {
            boardUserRepository.findById(user.getOid()).orElseGet(() -> {
                BoardUser newUser = new BoardUser();
                newUser.setId(user.getOid());
                newUser.setUsername(user.getUsername());
                newUser.setName(user.getName());
                newUser.setEmail(user.getEmail());
                return boardUserRepository.save(newUser);
            });

            Collaboration collaboration = new Collaboration();
            collaboration.setBoardId(id);
            collaboration.setOid(user.getOid());
            collaboration.setAccessRight(newCollab.getAccessRight());
            collaboration.setIsPending(TRUE);

            Collaboration newCol = collaborationRepository.save(collaboration);
            collaboratorResponseDTO.setOid(newCol.getOid());
            collaboratorResponseDTO.setName(user.getName());
            collaboratorResponseDTO.setIsPending(newCol.getIsPending());
            collaboratorResponseDTO.setAddedOn(newCol.getAddedOn());
            collaboratorResponseDTO.setBoardName(board.getName());
        } catch (DataIntegrityViolationException e) {
            throw new ConflictException("User already exists");
        } catch (Exception e) {
            throw new BadRequestException(e.getMessage());
        }

        return collaboratorResponseDTO;
    }


    @Transactional
    public CollaboratorResponseDTO updateCollaborator(String id, String oid, SimpleCollaboratorDTO newCollab) {
        Collaboration collaboration = collaborationRepository.findById(new CollaborationId(id, oid)).orElseThrow(() -> new NotFoundException("the specified collaborator does not exist"));
        System.out.println(newCollab.getAccessRight());
        collaboration.setAccessRight(newCollab.getAccessRight());
        try {

            Optional<BoardUser> user = boardUserRepository.findById(collaboration.getOid());
            CollaboratorResponseDTO collaboratorResponseDTO = mapper.map(collaboration, CollaboratorResponseDTO.class);

            if (user.isEmpty()) {
                collaboratorResponseDTO.setName("Unknown");
                collaboratorResponseDTO.setEmail("Unknown");
            } else {
                collaboratorResponseDTO.setName(user.get().getName());
                collaboratorResponseDTO.setEmail(user.get().getEmail());
            }

            return collaboratorResponseDTO;
        } catch (Exception e) {
            throw new BadRequestException("Cannot update collaborator");
        }
    }

    @Transactional
    public CollaboratorResponseDTO deleteCollaborator(String id, String oid) {
        Collaboration collaboration = collaborationRepository.findById(new CollaborationId(id, oid)).orElseThrow(() -> new NotFoundException("the specified collaborator does not exist"));
        try {
            collaborationRepository.delete(collaboration);
            return mapper.map(collaboration, CollaboratorResponseDTO.class);
        } catch (Exception e) {
            throw new BadRequestException("Cannot delete collaborator");
        }
    }

    @Transactional
    public CollaboratorResponseDTO acceptCollaborator(String id, String action) {
        if (SecurityUtil.getCurrentUserDetails() == null || action == null) {
            throw new BadRequestException("Cannot accept collaborator");
        }

        String oid = SecurityUtil.getCurrentUserDetails().getOid();
        Board board = boardRepository.findById(id).orElseThrow(() -> new NotFoundException("the specified board does not exist"));

        if (board.getOwner().getId().equals(oid)) {
            throw new ConflictException("Cannot accept collaborator as owner");
        }
        Collaboration collaboration = collaborationRepository.findById(new CollaborationId(id, oid)).orElseThrow(() -> new NotFoundException("the specified collaborator does not exist"));

        if (!collaboration.getIsPending()) {
            throw new ConflictException("Collaborator is not pending");
        }

        collaboration.setIsPending(FALSE);
        try {
            if (action.equals("accept")) {
                return mapper.map(collaborationRepository.save(collaboration), CollaboratorResponseDTO.class);
            } else {
                collaborationRepository.delete(collaboration);
                return mapper.map(collaboration, CollaboratorResponseDTO.class);
            }
        } catch (Exception e) {
            throw new BadRequestException("Cannot accept collaborator");
        }
    }


}
