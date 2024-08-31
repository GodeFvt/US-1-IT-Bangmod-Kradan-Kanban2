package sit.us1.backend.controllers;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import sit.us1.backend.dtos.JwtDTO.JwtRequestUser;
import sit.us1.backend.dtos.JwtDTO.JwtTokenResponseDTO;
import sit.us1.backend.entities.account.CustomUserDetails;
import sit.us1.backend.exceptions.UnauthorizedException;
import sit.us1.backend.services.AuthenticationService;
import sit.us1.backend.services.JwtTokenUtil;
import sit.us1.backend.services.JwtUserDetailsService;
import sit.us1.backend.services.SecurityUtil;

@CrossOrigin(origins = {"http://localhost:5173", "http://ip23us1.sit.kmutt.ac.th", "http://intproj23.sit.kmutt.ac.th"})
@RestController
//@RequestMapping("/authentications")
public class AuthenticationController {
    @Autowired
    JwtTokenUtil jwtTokenUtil;
    @Autowired
    AuthenticationService authenticationService;

    @PostMapping("/login")
    public ResponseEntity<JwtTokenResponseDTO> login(@RequestBody @Valid JwtRequestUser jwtRequestUser) {
        return ResponseEntity.ok(new JwtTokenResponseDTO(authenticationService.login(jwtRequestUser)));
    }

    @GetMapping("/validate-token")
    public ResponseEntity<Object> validateToken(@RequestHeader("Authorization") String requestTokenHeader) {
        Claims claims = null;
        if (requestTokenHeader != null && requestTokenHeader.startsWith("Bearer ")) {
            String jwtToken = requestTokenHeader.substring(7);
            try {
               claims = jwtTokenUtil.getAllClaimsFromToken(jwtToken);
            } catch (IllegalArgumentException e) {
                throw new UnauthorizedException("Unable to get JWT Token");
            } catch (ExpiredJwtException e) {
                throw new UnauthorizedException("JWT Token has expired");
            }
        } else {
            throw new UnauthorizedException("JWT Token does not begin with Bearer String");
        }
        return ResponseEntity.ok(claims);
    }
}
