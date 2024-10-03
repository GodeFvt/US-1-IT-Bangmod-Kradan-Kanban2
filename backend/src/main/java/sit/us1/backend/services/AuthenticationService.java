package sit.us1.backend.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import sit.us1.backend.dtos.JwtDTO.JwtRequestUser;
import sit.us1.backend.dtos.JwtDTO.JwtTokenResponseDTO;
import sit.us1.backend.entities.account.CustomUserDetails;
import sit.us1.backend.entities.taskboard.BoardUser;
import sit.us1.backend.exceptions.BadRequestException;
import sit.us1.backend.exceptions.ErrorResponse;
import sit.us1.backend.exceptions.UnauthorizedException;
import sit.us1.backend.repositories.taskboard.BoardUserRepository;

import java.io.IOException;

@Service
public class AuthenticationService {
    @Autowired
    JwtTokenUtil jwtTokenUtil;
    @Autowired
    JwtUserDetailsService jwtUserDetailsService;
    @Autowired
    AuthenticationManager authenticationManager;
    @Autowired
    BoardUserRepository boardUserRepository;


    public JwtTokenResponseDTO login(JwtRequestUser jwtRequestUser) {
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(jwtRequestUser.getUserName(), jwtRequestUser.getPassword());
        Authentication authentication = authenticationManager.authenticate(authenticationToken);
        if (!authentication.isAuthenticated()) {
            throw new UsernameNotFoundException("Username or Password is incorrect.");
        }
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        try {
            if (boardUserRepository.findById(userDetails.getOid()).isEmpty()) {
                BoardUser user = new BoardUser();
                user.setId(userDetails.getOid());
                user.setUsername(userDetails.getUsername());
                boardUserRepository.save(user);
            }
        } catch (Exception e) {
            throw new BadRequestException("Cannot create user");
        }
        return new JwtTokenResponseDTO(jwtTokenUtil.generateToken(userDetails, false), jwtTokenUtil.generateToken(userDetails, true));
    }

    public void refreshToken(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws IOException {
        final String requestTokenHeader = request.getHeader("Authorization");
        final String refreshToken;
        final String userOid;
        if (requestTokenHeader == null || !requestTokenHeader.startsWith("Bearer ")) {
            throw new UnauthorizedException("Refresh Token does not begin with Bearer String or is null");
        } else {
            refreshToken = requestTokenHeader.substring(7);
            try {
                userOid = jwtTokenUtil.getSubjectFromToken(refreshToken, true);
            } catch (IllegalArgumentException e) {
                throw new UnauthorizedException("Unable to get Refresh Token");
            } catch (ExpiredJwtException e) {
                throw new UnauthorizedException("Refresh Token has expired");
            } catch (JwtException e) {
                throw new UnauthorizedException("Refresh Token is invalid");
            }
        }
        if (userOid != null) {
            CustomUserDetails userDetails = jwtUserDetailsService.loadUserByOid(userOid);
            if (jwtTokenUtil.validateToken(refreshToken, userDetails, true)) {
                String accessToken = jwtTokenUtil.generateToken(userDetails, false);
                JwtTokenResponseDTO authResponse = new JwtTokenResponseDTO(accessToken, null);
                response.setContentType("application/json");
                new ObjectMapper().writeValue(response.getOutputStream(), authResponse);
            } else {
                throw new UnauthorizedException("Invalid refresh token");
            }
        } else {
            throw new UnauthorizedException("Invalid refresh token");
        }
    }


}
