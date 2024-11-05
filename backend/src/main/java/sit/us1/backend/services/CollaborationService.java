package sit.us1.backend.services;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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
    private EmailService emailService;
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

    public List<SimpleCollaboratorDTO> getCollaborator(String id) {
        List<Collaboration> collaborations = collaborationRepository.findAllByBoardIdOrderByAddedOn(id);
        List<SimpleCollaboratorDTO> simpleCollaboratorDTOS = new ArrayList<>();
        collaborations.forEach(collaboration -> {
            String oid = collaboration.getOid();
            Optional<User> user = userRepository.findById(oid);
            if (user.isEmpty()) {
                simpleCollaboratorDTOS.add(new SimpleCollaboratorDTO(oid, "Unknown", "Unknown", collaboration.getAccessRight().toString(), collaboration.getIsPending(),collaboration.getAddedOn()));
            }else {
                simpleCollaboratorDTOS.add(new SimpleCollaboratorDTO(oid, user.get().getName(), user.get().getEmail(), collaboration.getAccessRight().toString(),collaboration.getIsPending(), collaboration.getAddedOn()));
            }
        });

        return simpleCollaboratorDTOS;
    }

    public SimpleCollaboratorDTO getCollaboratorById(String id, String oid) {
        Collaboration collaboration = collaborationRepository.findById(new CollaborationId(id, oid)).orElseThrow(() -> new NotFoundException("the specified collaborator does not exist"));
        Optional<User> user = userRepository.findById(collaboration.getOid());
        if (user.isEmpty()) {
            return new SimpleCollaboratorDTO(oid, "Unknown", "Unknown", collaboration.getAccessRight().toString(),collaboration.getIsPending(), collaboration.getAddedOn());
        }
        return new SimpleCollaboratorDTO(oid, user.get().getName(), user.get().getEmail(), collaboration.getAccessRight().toString(),collaboration.getIsPending(), collaboration.getAddedOn());
    }

    @Transactional
    public SimpleCollaboratorDTO addCollaborator(String id, SimpleCollaboratorDTO newCollab) {
        Board board =   boardRepository.findById(id).orElseThrow(() -> new NotFoundException("the specified board does not exist"));
        User user = userRepository.findByEmail(newCollab.getEmail());
        if (user == null) {
            throw new NotFoundException("the specified user does not exist");
        }
        if (user.getOid().equals(SecurityUtil.getCurrentUserDetails().getOid())) {
            throw new ConflictException("Cannot add yourself as collaborator");
        }

        if (collaborationRepository.existsById(new CollaborationId(id, user.getOid()))) {
            throw new ConflictException("User already exists");
        }

        try {
            Optional<BoardUser> boardUser = boardUserRepository.findById(user.getOid());
            if (boardUser.isEmpty()) {
                BoardUser newUser = new BoardUser();
                newUser.setId(user.getOid());
                newUser.setUsername(user.getUsername());
                newUser.setName(user.getName());
                boardUserRepository.save(newUser);
            }

            Collaboration collaboration = new Collaboration();
            collaboration.setBoardId(id);
            collaboration.setOid(user.getOid());
            collaboration.setAccessRight(newCollab.getAccessRight());
            collaboration.setIsPending(TRUE);

            Collaboration newCol = collaborationRepository.save(collaboration);
            newCollab.setOid(newCol.getOid());
            newCollab.setName(user.getName());
            newCollab.setIsPending(newCol.getIsPending());
            newCollab.setAddedOn(newCol.getAddedOn());
            emailService.sendInvitationEmail(SecurityUtil.getCurrentUserDetails().getName(), user.getEmail(), board.getName(), newCollab.getAccessRight(), id);
            return newCollab;
        } catch (Exception e) {
            throw new BadRequestException(e.getMessage());
        }
    }

    @Transactional
    public SimpleCollaboratorDTO updateCollaborator(String id,String oid, SimpleCollaboratorDTO newCollab) {
        Collaboration collaboration = collaborationRepository.findById(new CollaborationId(id, oid)).orElseThrow(() -> new NotFoundException("the specified collaborator does not exist"));
        collaboration.setAccessRight(newCollab.getAccessRight());
        try {
            return mapper.map(collaborationRepository.save(collaboration), SimpleCollaboratorDTO.class);
        } catch (Exception e) {
            throw new BadRequestException("Cannot update collaborator");
        }
    }

    @Transactional
    public SimpleCollaboratorDTO deleteCollaborator(String id, String oid) {
        Collaboration collaboration = collaborationRepository.findById(new CollaborationId(id, oid)).orElseThrow(() -> new NotFoundException("the specified collaborator does not exist"));
        try {
            collaborationRepository.delete(collaboration);
            return mapper.map(collaboration, SimpleCollaboratorDTO.class);
        } catch (Exception e) {
            throw new BadRequestException("Cannot delete collaborator");
        }
    }

    @Transactional
    public SimpleCollaboratorDTO acceptCollaborator(String id, String action) {
        if(SecurityUtil.getCurrentUserDetails() == null || action == null){
            throw new BadRequestException("Cannot accept collaborator");
        }

        String oid = SecurityUtil.getCurrentUserDetails().getOid();
        Board board = boardRepository.findById(id).orElseThrow(() -> new NotFoundException("the specified board does not exist"));

        if(board.getOwner().getId().equals(oid)){
            throw new ConflictException("Cannot accept collaborator as owner");
        }
        Collaboration collaboration = collaborationRepository.findById(new CollaborationId(id, oid)).orElseThrow(() -> new NotFoundException("the specified collaborator does not exist"));

        if(!collaboration.getIsPending()){
            throw new ConflictException("Collaborator is not pending");
        }

        collaboration.setIsPending(FALSE);
        try {
            if(action.equals("accept")) {
                return mapper.map(collaborationRepository.save(collaboration), SimpleCollaboratorDTO.class);
            }else{
                collaborationRepository.delete(collaboration);
                return mapper.map(collaboration, SimpleCollaboratorDTO.class);
            }
        } catch (Exception e) {
            throw new BadRequestException("Cannot accept collaborator");
        }
    }


}
