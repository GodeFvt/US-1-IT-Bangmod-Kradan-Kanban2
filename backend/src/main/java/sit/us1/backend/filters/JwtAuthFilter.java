package sit.us1.backend.filters;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.filter.OncePerRequestFilter;
import sit.us1.backend.entities.account.CustomUserDetails;
import sit.us1.backend.entities.taskboard.BoardUser;
import sit.us1.backend.exceptions.AccessDeniedException;
import sit.us1.backend.exceptions.ErrorResponse;
import sit.us1.backend.exceptions.NotFoundException;
import sit.us1.backend.exceptions.UnauthorizedException;
import sit.us1.backend.repositories.taskboard.BoardUserRepository;
import sit.us1.backend.services.JwtTokenUtil;
import sit.us1.backend.services.JwtUserDetailsService;


import java.io.IOException;
import java.util.Base64;
import java.util.List;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {
    @Autowired
    private JwtUserDetailsService jwtUserDetailsService;
    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Autowired
    BoardUserRepository boardUserRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws ServletException, IOException {
        try {
            final String requestTokenHeader = request.getHeader("Authorization");
            String username = null;
            String jwtToken = null;
            boolean isTokenValid = false;
            String tokenError = null;

            if (AuthWhitelistPaths.isWhitelisted(request.getRequestURI())) {
                chain.doFilter(request, response);
                return;
            }

            if (requestTokenHeader != null) {
                if (requestTokenHeader.startsWith("Bearer ")) {
                    jwtToken = requestTokenHeader.substring(7);
                    try {
                        if (isMicrosoftToken(jwtToken)) {
                             if(isTokenExpired(jwtToken)){
                                 throw new ExpiredJwtException(null, null, "JWT Token has expired");
                             }else {
                                 CustomUserDetails userDetails = MicrosoftTokenWithGraphAPI(jwtToken);
                                 BoardUser user = boardUserRepository.findById(userDetails.getOid()).orElse(null);
                                 if(user == null) {
                                     BoardUser newUser = new BoardUser();
                                     newUser.setId(userDetails.getOid());
                                     newUser.setUsername(userDetails.getUsername());
                                     newUser.setName(userDetails.getName());
                                     newUser.setEmail(userDetails.getEmail());
                                     boardUserRepository.save(newUser);
                                 }
                                    UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                                    usernamePasswordAuthenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                                    SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
                             }
                        } else {
                            username = jwtTokenUtil.getSubjectFromToken(jwtToken, false);
                        }
                        System.out.println("username: " + username);
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

            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = jwtUserDetailsService.loadUserByUsername(username);
                if (jwtTokenUtil.validateToken(jwtToken, (CustomUserDetails) userDetails, false)) {
                    UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                    usernamePasswordAuthenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
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

    private CustomUserDetails MicrosoftTokenWithGraphAPI(String token) {
        try {
            String graphApiUrl = "https://graph.microsoft.com/v1.0/me";
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + token);
            HttpEntity<String> entity = new HttpEntity<>("", headers);

            ResponseEntity<String> response = restTemplate.exchange(graphApiUrl, HttpMethod.GET, entity, String.class);
            if (response.getStatusCode() != HttpStatus.OK) {
                throw new UnauthorizedException("Invalid Microsoft Token");
            }
            String body = response.getBody();
            String username = new ObjectMapper().readTree(body).get("userPrincipalName").asText();
            String name = new ObjectMapper().readTree(body).get("displayName").asText();
            String oid = new ObjectMapper().readTree(body).get("id").asText();
            String email = new ObjectMapper().readTree(body).get("mail").asText();
            List<GrantedAuthority> authorities = List.of(() -> "ROLE_USER");
            return new CustomUserDetails(username, name, oid, email, "", null, authorities);
        } catch (Exception e) {
            return null;
        }
    }

    private boolean isMicrosoftToken(String token) {
        try {
            String payload = getPayloadFromToken(token);
            return payload.contains("\"iss\":\"https://sts.windows.net/");
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }

    private String getPayloadFromToken(String token) throws IllegalArgumentException {
        String[] parts = token.split("\\.");
        if (parts.length != 3) {
            throw new IllegalArgumentException("Invalid JWT token format");
        }
        return new String(Base64.getUrlDecoder().decode(parts[1]));
    }

    private boolean isTokenExpired(String token) {
        try {
            String payload = getPayloadFromToken(token);
            long exp = new ObjectMapper().readTree(payload).get("exp").asLong();
            long currentTime = System.currentTimeMillis() / 1000;
            return currentTime >= exp;
        } catch (Exception ex) {
            return true;
        }
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
