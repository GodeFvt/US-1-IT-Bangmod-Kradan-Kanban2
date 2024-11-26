package sit.us1.backend.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import sit.us1.backend.dtos.boardsDTO.CollaboratorResponseDTO;
import sit.us1.backend.dtos.boardsDTO.SimpleCollaboratorDTO;
import sit.us1.backend.entities.account.MsUser;
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
import sit.us1.backend.utils.SecurityUtil;

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
        Board board = boardRepository.findById(id).orElseThrow(() -> new NotFoundException("The specified board does not exist"));
        CollaboratorResponseDTO collaboratorResponseDTO = mapper.map(newCollab, CollaboratorResponseDTO.class);
        MsUser msUser = null;
        User ItBkkuser;
        if (newCollab.getAccessToken() != null && !newCollab.getAccessToken().isEmpty()) {
            msUser = getMsUserByEmail(newCollab.getEmail(), newCollab.getAccessToken());
        }

        ItBkkuser = userRepository.findByEmail(newCollab.getEmail());

        if (ItBkkuser == null && msUser == null) {
            throw new NotFoundException("The specified user does not exist");
        }

        collaboratorResponseDTO.setOid(ItBkkuser != null ? ItBkkuser.getOid() : msUser.getOid());
        collaboratorResponseDTO.setName(ItBkkuser != null ?  ItBkkuser.getName(): msUser.getName());
        collaboratorResponseDTO.setEmail(ItBkkuser != null ? ItBkkuser.getEmail() : msUser.getEmail() );

        if (collaboratorResponseDTO.getOid().equals(SecurityUtil.getCurrentUserDetails().getOid())) {
            throw new ConflictException("Cannot add yourself as collaborator");
        }

        if (collaborationRepository.existsById(new CollaborationId(id, collaboratorResponseDTO.getOid()))) {
            throw new ConflictException("User already exists");
        }
        try {
            Optional<BoardUser> boardUser = boardUserRepository.findById(collaboratorResponseDTO.getOid());

            if (boardUser.isEmpty()) {
                BoardUser newBoardUser = new BoardUser();
                newBoardUser.setId(collaboratorResponseDTO.getOid());
                newBoardUser.setName(collaboratorResponseDTO.getName());
                newBoardUser.setUsername(ItBkkuser != null ? ItBkkuser.getUsername() : msUser.getName());
                newBoardUser.setEmail(collaboratorResponseDTO.getEmail());
                boardUserRepository.save(newBoardUser);
            }

            Collaboration collaboration = new Collaboration();
            collaboration.setBoardId(id);
            collaboration.setOid(collaboratorResponseDTO.getOid());
            collaboration.setAccessRight(newCollab.getAccessRight());
            collaboration.setIsPending(TRUE);

            Collaboration newCol = collaborationRepository.save(collaboration);
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


    private MsUser getMsUserByEmail(String email, String token) {
        if (token == null || token.isEmpty()) {
            return null;
        }
        try {
            String graphApiUrl = "https://graph.microsoft.com/v1.0/users/" + email;
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + token);
            HttpEntity<String> entity = new HttpEntity<>("", headers);
            ResponseEntity<String> response = restTemplate.exchange(graphApiUrl, HttpMethod.GET, entity, String.class);
            ObjectMapper mapper = new ObjectMapper();

            Map<String, Object> map = mapper.readValue(response.getBody(), Map.class);
            MsUser msUser = new MsUser();
            msUser.setOid((String) map.get("id"));
            msUser.setName((String) map.get("displayName"));
            msUser.setEmail((String) map.get("mail"));
            return msUser;
        } catch (Exception e) {
            return null;
        }
    }


}
