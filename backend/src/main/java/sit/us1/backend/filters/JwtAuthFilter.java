package sit.us1.backend.filters;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.*;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import sit.us1.backend.entities.account.CustomUserDetails;
import sit.us1.backend.entities.account.MsUser;
import sit.us1.backend.exceptions.AccessDeniedException;
import sit.us1.backend.exceptions.ErrorResponse;
import sit.us1.backend.exceptions.NotFoundException;
import sit.us1.backend.exceptions.UnauthorizedException;
import sit.us1.backend.utils.JwtTokenUtil;
import sit.us1.backend.services.JwtUserDetailsService;


import java.io.IOException;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {
    @Autowired
    private JwtUserDetailsService jwtUserDetailsService;
    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws ServletException, IOException {
        try {
            final String requestTokenHeader = request.getHeader("Authorization");
            String username = null;
            String jwtToken = null;
            boolean isTokenValid = false;
            String tokenError = null;
            MsUser msUser = null;

            if (AuthWhitelistPaths.isWhitelisted(request.getRequestURI())) {
                chain.doFilter(request, response);
                return;
            }

            if (requestTokenHeader != null) {
                if (requestTokenHeader.startsWith("Bearer ")) {
                    jwtToken = requestTokenHeader.substring(7);
                    try {
                        if (!jwtTokenUtil.isTokenItbkk(jwtToken)) {
                            msUser = jwtTokenUtil.getMsUserFromToken(jwtToken);
                        } else {
                            username = jwtTokenUtil.getSubjectFromToken(jwtToken, false);
                        }
                        isTokenValid = true;
                    } catch (IllegalArgumentException e) {
                        tokenError = "Unable to get JWT Token";
                    } catch (ExpiredJwtException e) {
                        tokenError = "JWT Token has expired";
                    } catch (JwtException e) {
                        tokenError = "JWT Token is invalid";
                    }
                } else {
                    tokenError = "JWT Token does not begin with Bearer String";
                }
            }

            request.setAttribute("isTokenValid", isTokenValid);
            request.setAttribute("tokenError", tokenError);

            if (SecurityContextHolder.getContext().getAuthentication() == null) {
                if (username != null) {
                    UserDetails userDetails = jwtUserDetailsService.loadUserByUsername(username);
                    if (jwtTokenUtil.validateToken(jwtToken, (CustomUserDetails) userDetails, false)) {
                        authenticationToken(userDetails, request);
                    }
                } else if (msUser != null) {
                    CustomUserDetails userDetails = jwtUserDetailsService.getUserDetailsMS(msUser);
                    authenticationToken(userDetails, request);
                }
            }

            if (requestTokenHeader == null && request.getRequestURI().equals("/v3/boards")) {
                throw new UnauthorizedException("JWT Token is required");
            }

            chain.doFilter(request, response);

        } catch (Exception e) {
            handleException(response, e, request.getRequestURI());
        }
    }

    private void authenticationToken(UserDetails userDetails, HttpServletRequest request) {
        UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        usernamePasswordAuthenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
    }

    public static void handleException(HttpServletResponse response, Exception e, String uri) throws IOException {
        HttpStatus status;
        String message;

        if (e instanceof UnauthorizedException) {
            status = HttpStatus.UNAUTHORIZED;
            message = e.getMessage();
        } else if (e instanceof NotFoundException) {
            status = HttpStatus.NOT_FOUND;
            message = e.getMessage();
        } else if (e instanceof AccessDeniedException) {
            status = HttpStatus.FORBIDDEN;
            message = e.getMessage();
        } else {
            status = HttpStatus.UNAUTHORIZED;
            message = e.getMessage();
        }

        response.setStatus(status.value());
        response.setContentType("application/json");
        ErrorResponse errorResponse = new ErrorResponse(status.value(), message, uri);
        response.getWriter().write(new ObjectMapper().writeValueAsString(errorResponse));
    }
}
