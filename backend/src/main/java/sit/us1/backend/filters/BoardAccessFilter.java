package sit.us1.backend.filters;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import sit.us1.backend.entities.account.CustomUserDetails;
import sit.us1.backend.entities.taskboard.Collaboration;
import sit.us1.backend.exceptions.AccessDeniedException;
import sit.us1.backend.exceptions.ErrorResponse;
import sit.us1.backend.exceptions.NotFoundException;
import sit.us1.backend.exceptions.UnauthorizedException;
import sit.us1.backend.services.BoardService;
import sit.us1.backend.services.JwtTokenUtil;
import sit.us1.backend.services.SecurityUtil;

import java.io.IOException;

@Component
public class BoardAccessFilter extends OncePerRequestFilter {

    @Autowired
    private BoardService boardService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws ServletException, IOException {
        String uri = request.getRequestURI();
        if (uri.equals("/login") || uri.equals("/token")) {
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

        }  catch (Exception e) {
            JwtAuthFilter.handleException(response, e, request.getRequestURI());
        }
    }

    private void handleBoardRequest(HttpServletRequest request, boolean isTokenValid, String tokenError) throws UnauthorizedException, NotFoundException, AccessDeniedException {
        boolean isGetMethod = request.getMethod().equals("GET");
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

        if (user != null) {
            boolean isCollab = boardService.isCollaborator(boardId,user.getOid());
            if (!isOwner && !isTokenValid && !isGetMethod) {
                throw new UnauthorizedException(tokenError);
            } else if ((!isOwner && !isCollab) && (!isPublicBoard || !isGetMethod )) {
                throw new AccessDeniedException("You are not the owner of this board");
            } else if (isCollab && !isGetMethod) {
                Collaboration collab = boardService.getCollaboration(boardId, user.getOid());
                int uriLength = uri.split("/").length;
                if (collab.getAccess().equals(Collaboration.Access.READ) || uriLength <= 4) {
                    throw new AccessDeniedException("You do not have permission to modify this board");
                }else if (uri.split("/")[4].equals("collabs") && !request.getMethod().equals("DELETE")) {
                    throw new AccessDeniedException("You do not have permission to modify collaborators");
                }
            }
            else if (!isTokenValid) {
                throw new UnauthorizedException(tokenError);
            }
        } else if (!isPublicBoard && isGetMethod) {
            throw new AccessDeniedException("Board is not public");
        } else if (!isPublicBoard || !isGetMethod) {
            throw new UnauthorizedException(tokenError);
        }
    }


}
