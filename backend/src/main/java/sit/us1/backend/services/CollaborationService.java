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
import sit.us1.backend.exceptions.UnavailableException;
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

    public List<CollaboratorResponseDTO> getCollaborator(String id) {
        List<Collaboration> collaborations = collaborationRepository.findAllByBoardIdOrderByAddedOn(id);
        List<SimpleCollaboratorDTO> simpleCollaboratorDTOS = new ArrayList<>();
        collaborations.forEach(collaboration -> {
            String oid = collaboration.getOid();
            Optional<User> user = userRepository.findById(oid);
            if (user.isEmpty()) {
                simpleCollaboratorDTOS.add(new SimpleCollaboratorDTO(oid, "Unknown", "Unknown", collaboration.getAccessRight().toString(), collaboration.getIsPending(), collaboration.getAddedOn()));
            } else {
                simpleCollaboratorDTOS.add(new SimpleCollaboratorDTO(oid, user.get().getName(), user.get().getEmail(), collaboration.getAccessRight().toString(), collaboration.getIsPending(), collaboration.getAddedOn()));
            }
        });

        return listMapper.mapList(simpleCollaboratorDTOS, CollaboratorResponseDTO.class, mapper);
    }

    public CollaboratorResponseDTO getCollaboratorById(String id, String oid) {
        Collaboration collaboration = collaborationRepository.findById(new CollaborationId(id, oid)).orElseThrow(() -> new NotFoundException("the specified collaborator does not exist"));
        Optional<User> user = userRepository.findById(collaboration.getOid());
        if (user.isEmpty()) {
//            return new SimpleCollaboratorDTO(oid, "Unknown", "Unknown", collaboration.getAccessRight().toString(),collaboration.getIsPending(), collaboration.getAddedOn());
            return new CollaboratorResponseDTO(oid, "Unknown", "Unknown", collaboration.getAccessRight().toString(), collaboration.getIsPending(), collaboration.getAddedOn(), null);
        }
//        return new SimpleCollaboratorDTO(oid, user.get().getName(), user.get().getEmail(), collaboration.getAccessRight().toString(),collaboration.getIsPending(), collaboration.getAddedOn());
        return new CollaboratorResponseDTO(oid, user.get().getName(), user.get().getEmail(), collaboration.getAccessRight().toString(), collaboration.getIsPending(), collaboration.getAddedOn(), null);
    }

    @Transactional
    public CollaboratorResponseDTO addCollaborator(String id, SimpleCollaboratorDTO newCollab) {
        //Pessimistic Lock บล็อกการเข้าถึงจากคำขออื่น
        Board board = boardRepository.findByIdWithLock(id)
                .orElseThrow(() -> new NotFoundException("The specified board does not exist"));
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

        } catch (DataIntegrityViolationException e) {
            throw new ConflictException("User already exists");
        } catch (Exception e) {
            throw new BadRequestException(e.getMessage());
        }

        try {
            emailService.sendInvitationEmail(SecurityUtil.getCurrentUserDetails().getName(), user.getEmail(), board.getName(), newCollab.getAccessRight(), id);
            collaboratorResponseDTO.setEmailStatus("Email sent successfully");
        } catch (Exception e) {
            collaboratorResponseDTO.setEmailStatus("Failed to send email");
        }

        return collaboratorResponseDTO;
    }


    @Transactional
    public CollaboratorResponseDTO updateCollaborator(String id, String oid, SimpleCollaboratorDTO newCollab) {
        Collaboration collaboration = collaborationRepository.findById(new CollaborationId(id, oid)).orElseThrow(() -> new NotFoundException("the specified collaborator does not exist"));
        collaboration.setAccessRight(newCollab.getAccessRight());
        try {

            Optional<User> user = userRepository.findById(collaboration.getOid());
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
