package sit.us1.backend.filters;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import sit.us1.backend.entities.account.CustomUserDetails;
import sit.us1.backend.entities.taskboard.Collaboration;
import sit.us1.backend.exceptions.AccessDeniedException;
import sit.us1.backend.exceptions.NotFoundException;
import sit.us1.backend.exceptions.UnauthorizedException;
import sit.us1.backend.services.*;
import sit.us1.backend.utils.SecurityUtil;

import java.io.IOException;

@Component
public class BoardAccessFilter extends OncePerRequestFilter {

    @Autowired
    private BoardService boardService;
    @Autowired
    private CollaborationService collabService;
    @Autowired
    private TaskService taskService;
    @Autowired
    private StatusService statusService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws ServletException, IOException {
        String uri = request.getRequestURI();

        if (AuthWhitelistPaths.isWhitelisted(uri)) {
            chain.doFilter(request, response);
            return;
        }

        try {
            boolean isTokenValid = (boolean) request.getAttribute("isTokenValid");
            String tokenError = request.getAttribute("tokenError") != null ? (String) request.getAttribute("tokenError") : "JWT Token is required";

            if (uri.startsWith("/v3/boards/")) {
                handleBoardRequest(request, isTokenValid, tokenError);
            } else if (!isTokenValid) {
                throw new UnauthorizedException(tokenError);
            }

            chain.doFilter(request, response);

        } catch (Exception e) {
            JwtAuthFilter.handleException(response, e, request.getRequestURI());
        }
    }

    private void handleBoardRequest(HttpServletRequest request, boolean isTokenValid, String tokenError) throws UnauthorizedException, NotFoundException, AccessDeniedException {
        String method = request.getMethod();
        boolean isGetMethod = method.equals("GET");
        String uri = request.getRequestURI();
        CustomUserDetails user = SecurityUtil.getCurrentUserDetails();
        String boardId = uri.split("/")[3];
        boolean boardExists = boardService.boardExists(boardId);

        if (!boardExists) {
            if (user == null && !isTokenValid && !isGetMethod) {
                throw new UnauthorizedException("JWT Token is required");
            } else {
                throw new NotFoundException("Board not found");
            }
        }

        boolean isPublicBoard = boardService.isBoardPublic(boardId);
        boolean isOwner = user != null && boardService.isOwnerOfBoard(boardId, user.getOid());
        String[] uriParts = uri.split("/");
        int uriLength = uriParts.length;

        if (user != null) {
            boolean isCollab = false;
            boolean collabExists = collabService.collaboratorExists(user.getOid());

            if(collabExists){
                isCollab = collabService.isCollaborator(boardId, user.getOid());
            }

            if (uriLength > 5 && uriParts[4].equals("collabs") && uriParts[5].equals("invitations") && collabExists) {
                return;
            }

            if (!isOwner && !isTokenValid && !isGetMethod) {
                throw new UnauthorizedException(tokenError);
            } else if ((!isOwner && !isCollab) && (!isPublicBoard || !isGetMethod)) {
                throw new AccessDeniedException("You are not the owner of this board");
            } else if (!isGetMethod) {
                if(isCollab){
                    Collaboration collab = collabService.getCollaboration(boardId, user.getOid());
//                    if(uriLength >= 6 && !collabService.collaboratorExists(uriParts[5]) && uriParts[4].equals("collabs")){
//                        throw new NotFoundException("Collaborator not found");
//                    }
                    if (uriLength <= 4 || collab.getAccessRight().equals(Collaboration.Access.READ) && !uriParts[4].equals("collabs")) {
                        throw new AccessDeniedException("You do not have permission to modify this board");
                    } else if (uriParts[4].equals("collabs") && !method.equals("DELETE")) {
                        throw new AccessDeniedException("You do not have permission to modify collaborators");
                    } else if (uriParts[4].equals("collabs") && method.equals("DELETE")) {
                        String collabId = uriLength >= 6 ? uriParts[5] : null;
                        if (collabId == null) {
                            throw new NotFoundException("Collaborator not found");
                        } else if (!collabId.equals(user.getOid())) {
                            throw new AccessDeniedException("You do not have permission to modify collaborators");
                        }
                    }
                }else if (uriLength > 5 && uriParts[4].equals("collabs")) {
                    if (uriParts[5].equals(user.getOid())) {
                        throw new NotFoundException("You is owner of this board");
                    }
                }
            }  else if (!isTokenValid) {
                throw new UnauthorizedException(tokenError);
            }
        } else if (!isPublicBoard && isGetMethod) {
            throw new AccessDeniedException("Board is not public");
        } else if (!isPublicBoard || !isGetMethod) {
            throw new UnauthorizedException(tokenError);
        }
        if (uriLength >= 6 && uriParts[4].equals("statuses") && !uriParts[5].equals("limit") && !uriParts[5].equals("all") && !statusService.isStatusExist(boardId,Integer.parseInt(uriParts[5]))) {
            throw new NotFoundException("Status not found");
        }
        if (uriLength >= 6 && uriParts[4].equals("tasks")&& !uriParts[5].equals("count") && !taskService.isTaskExist(boardId,Integer.parseInt(uriParts[5]))) {
            throw new NotFoundException("Task not found");
        }
    }


}
