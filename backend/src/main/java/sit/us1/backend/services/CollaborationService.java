package sit.us1.backend.services;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sit.us1.backend.dtos.boardsDTO.SimpleCollaboratorDTO;
import sit.us1.backend.entities.account.User;
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

    public boolean isCollaborator(String boardId, String oid) {
        return collaborationRepository.existsById(new CollaborationId(boardId, oid));
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
                simpleCollaboratorDTOS.add(new SimpleCollaboratorDTO(oid, "Unknown", "Unknown", collaboration.getAccess().toString(), collaboration.getAddedOn()));
            }else {
                simpleCollaboratorDTOS.add(new SimpleCollaboratorDTO(oid, user.get().getUsername(), user.get().getEmail(), collaboration.getAccess().toString(), collaboration.getAddedOn()));
            }
        });

        return simpleCollaboratorDTOS;
    }

    public SimpleCollaboratorDTO getCollaboratorById(String id, String oid) {
        Collaboration collaboration = collaborationRepository.findById(new CollaborationId(id, oid)).orElseThrow(() -> new NotFoundException("the specified collaborator does not exist"));
        Optional<User> user = userRepository.findById(collaboration.getOid());
        if (user.isEmpty()) {
            return new SimpleCollaboratorDTO(oid, "Unknown", "Unknown", collaboration.getAccess().toString(), collaboration.getAddedOn());
        }
        return new SimpleCollaboratorDTO(oid, user.get().getUsername(), user.get().getEmail(), collaboration.getAccess().toString(), collaboration.getAddedOn());
    }

    @Transactional
    public SimpleCollaboratorDTO addCollaborator(String id, SimpleCollaboratorDTO newCollab) {
        boardRepository.findById(id).orElseThrow(() -> new NotFoundException("the specified board does not exist"));
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
                boardUserRepository.save(newUser);
            }

            Collaboration collaboration = new Collaboration();
            collaboration.setBoardId(id);
            collaboration.setOid(user.getOid());
            collaboration.setAccess(newCollab.getAccess());

            Collaboration newCol = collaborationRepository.save(collaboration);
            newCollab.setOid(newCol.getOid());
            newCollab.setName(user.getUsername());
            newCollab.setAddedOn(newCol.getAddedOn());
            return newCollab;
        } catch (Exception e) {
            throw new BadRequestException("Cannot add collaborator");
        }
    }

    @Transactional
    public SimpleCollaboratorDTO updateCollaborator(String id,String oid, SimpleCollaboratorDTO newCollab) {
        Collaboration collaboration = collaborationRepository.findById(new CollaborationId(id, oid)).orElseThrow(() -> new NotFoundException("the specified collaborator does not exist"));
        collaboration.setAccess(newCollab.getAccess());
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
}
