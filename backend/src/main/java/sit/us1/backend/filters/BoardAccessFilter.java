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
        try {
            if (request.getRequestURI().startsWith("/v3/boards/")) {
                boolean isGetMethod = request.getMethod().equals("GET");

                CustomUserDetails user = SecurityUtil.getCurrentUserDetails();
                String boardId = request.getRequestURI().split("/")[3];
                boolean boardExists = boardService.boardExists(boardId);

                if (!boardExists) {
                    if (user == null){
                        throw new UnauthorizedException("JWT Token is required");
                    }else{
                        throw new NotFoundException("Board not found");
                    }
                }

                boolean isPublicBoard = boardService.isBoardPublic(boardId);
                boolean isOwner = user != null && boardService.isOwnerOfBoard(boardId, user.getOid());

                if (user != null) {
                    if (!isOwner && !isPublicBoard) {
                        throw new AccessDeniedException("You are not the owner of this board");
                    } else if (!isOwner && isPublicBoard && !isGetMethod) {
                        throw new AccessDeniedException("You are not the owner of this board");
                    }
                } else if (user == null && boardExists && !isPublicBoard && isGetMethod) {
                    throw new AccessDeniedException("Board is not public");
                } else if (user == null && boardExists && isPublicBoard && !isGetMethod) {
                    throw new UnauthorizedException("JWT Token is required");
                } else if (user == null && !isPublicBoard) {
                    throw new UnauthorizedException("JWT Token is required");
                }
            }

            chain.doFilter(request, response);

        } catch (UnauthorizedException e) {
            handleException(response, HttpStatus.UNAUTHORIZED, e.getMessage(), request.getRequestURI());
        } catch (NotFoundException e) {
            handleException(response, HttpStatus.NOT_FOUND, e.getMessage(), request.getRequestURI());
        } catch (AccessDeniedException e) {
            handleException(response, HttpStatus.FORBIDDEN, e.getMessage(), request.getRequestURI());
        } catch (Exception e) {
            handleException(response, HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred", request.getRequestURI());
        }
    }

    private void handleException(HttpServletResponse response, HttpStatus status, String message, String uri) throws IOException {
        response.setStatus(status.value());
        response.setContentType("application/json");
        ErrorResponse errorResponse = new ErrorResponse(status.value(), message, uri);
        response.getWriter().write(new ObjectMapper().writeValueAsString(errorResponse));
    }
}