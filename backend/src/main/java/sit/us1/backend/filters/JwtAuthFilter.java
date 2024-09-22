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
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import sit.us1.backend.entities.account.CustomUserDetails;
import sit.us1.backend.exceptions.ErrorResponse;
import sit.us1.backend.exceptions.NotFoundException;
import sit.us1.backend.exceptions.UnauthorizedException;
import sit.us1.backend.services.BoardService;
import sit.us1.backend.services.JwtTokenUtil;
import sit.us1.backend.services.JwtUserDetailsService;


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
            if (request.getRequestURI().startsWith("/v3/boards/") && request.getMethod().equals("GET")) {
                String boardId = request.getRequestURI().split("/")[3];
                if (boardService.boardExists(boardId)) {
                    chain.doFilter(request, response);
                    return;
                } else {
                    throw new NotFoundException("Board not found");
                }
            }
            if (requestTokenHeader == null) {
                throw new UnauthorizedException("JWT Token is required");
            }
            chain.doFilter(request, response);
        } catch (UnauthorizedException e) {
            handleException(response, HttpStatus.UNAUTHORIZED, e.getMessage(), request.getRequestURI());
        } catch (NotFoundException e) {
            handleException(response, HttpStatus.NOT_FOUND, e.getMessage(), request.getRequestURI());
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

