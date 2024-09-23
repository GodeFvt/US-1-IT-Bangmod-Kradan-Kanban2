package sit.us1.backend.filters;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import sit.us1.backend.entities.account.CustomUserDetails;
import sit.us1.backend.exceptions.AccessDeniedException;
import sit.us1.backend.exceptions.ErrorResponse;
import sit.us1.backend.exceptions.NotFoundException;
import sit.us1.backend.exceptions.UnauthorizedException;
import sit.us1.backend.services.BoardService;
import sit.us1.backend.services.JwtTokenUtil;
import sit.us1.backend.services.JwtUserDetailsService;
import sit.us1.backend.services.SecurityUtil;


import java.io.IOException;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {
    @Autowired
    private JwtUserDetailsService jwtUserDetailsService;
    @Autowired
    private JwtTokenUtil jwtTokenUtil;
    @Autowired
    private BoardService boardService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws ServletException, IOException {
        final String requestTokenHeader = request.getHeader("Authorization");
        String username = null;
        String jwtToken = null;
        try {
            if (request.getRequestURI().equals("/login") || request.getRequestURI().equals("/token")) {
                chain.doFilter(request, response);
                return;
            }
            if (requestTokenHeader != null) {
                if (requestTokenHeader.startsWith("Bearer ")) {
                    jwtToken = requestTokenHeader.substring(7);
                    try {
                        username = jwtTokenUtil.getSubjectFromToken(jwtToken, false);
                    } catch (IllegalArgumentException e) {
                        throw new UnauthorizedException("Unable to get JWT Token");
                    } catch (ExpiredJwtException e) {
                        throw new UnauthorizedException("JWT Token has expired");
                    } catch (JwtException e) {
                        throw new UnauthorizedException("JWT Token is invalid");
                    }
                } else {
                    throw new UnauthorizedException("JWT Token does not begin with Bearer String");
                }
            }
//            else {
//                throw new UnauthorizedException("JWT Token is required");
//            }
            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = jwtUserDetailsService.loadUserByUsername(username);
                if (jwtTokenUtil.validateToken(jwtToken, (CustomUserDetails) userDetails, false)) {
                    UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                    usernamePasswordAuthenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
                }
            }


            boolean isGetMethod = request.getMethod().equals("GET");

            if (request.getRequestURI().startsWith("/v3/boards/")) {
                CustomUserDetails user = SecurityUtil.getCurrentUserDetails();
                String boardId = request.getRequestURI().split("/")[3];
                boolean boardExists = boardService.boardExists(boardId);
                boolean isPublicBoard = boardService.isBoardPublic(boardId);
                boolean isOwner = user != null && boardService.isOwnerOfBoard(boardId, user.getOid());

                if (!boardExists) {
                    if (isGetMethod) {
                        throw new NotFoundException("Board not found");
                    } else if (requestTokenHeader == null) {
                        throw new UnauthorizedException("JWT Token is required");
                    }
                }

                if (user != null) {
                    if (!isOwner && !isPublicBoard) {
                        throw new AccessDeniedException("You are not the owner of this board");
                    } else if (!isOwner && isPublicBoard && !isGetMethod) {
                        throw new AccessDeniedException("You are not the owner of this board");
                    }
                } else if (user == null && boardExists && !isPublicBoard && isGetMethod) {
                    throw new AccessDeniedException("Board is not public");
                }

                if (requestTokenHeader == null && !isPublicBoard) {
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

//            if (request.getRequestURI().startsWith("/v3/boards/") && request.getMethod().equals("GET")) {
//                String boardId = request.getRequestURI().split("/")[3];
//                if (boardService.boardExists(boardId)) {
//                    chain.doFilter(request, response);
//                    return;
//                } else {
//                    throw new NotFoundException("Board not found");
//                }
//            }

//            CustomUserDetails user = SecurityUtil.getCurrentUserDetails();
//            String boardId = request.getRequestURI().split("/")[3];
//            if (request.getRequestURI().startsWith("/v3/boards/")) {
//                System.out.println("board");
//                if (!boardService.boardExists(boardId) && request.getMethod().equals("GET")) {
//                    System.out.println("111");
//                    throw new NotFoundException("Board not found");
//                } else {
//                    if (requestTokenHeader == null && !request.getMethod().equals("GET")) {
//                        System.out.println("111ddd");
//                        throw new UnauthorizedException("JWT Token is required");
//                    } else if (requestTokenHeader != null && !boardService.boardExists(boardId)) {
//                        throw new NotFoundException("Board not found");
//                    }
//                }
//
//                if (user != null) {
//                    if (!boardService.isOwnerOfBoard(boardId, user.getOid()) && !boardService.isBoardPublic(boardId)) {
//                        System.out.println("222");
//                        throw new AccessDeniedException("You are not the owner of this board");
//                    }
//                    if (!boardService.isOwnerOfBoard(boardId, user.getOid()) && boardService.isBoardPublic(boardId) && !request.getMethod().equals("GET")) {
//                        System.out.println("333");
//                        throw new AccessDeniedException("You are not the owner of this board");
//                    }
//                }
//                else if (user == null && boardService.boardExists(boardId) && !boardService.isBoardPublic(boardId) && request.getMethod().equals("GET")) {
//                    System.out.println("444");
//                    throw new AccessDeniedException("Board is not public");
//                }
//
//            }
//
//            if (requestTokenHeader == null && !boardService.isBoardPublic(boardId)) {
//                throw new UnauthorizedException("JWT Token is required");
//            }
//            chain.doFilter(request, response);