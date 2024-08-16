package sit.us1.backend.controllers;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import sit.us1.backend.dtos.JwtDTO.JwtRequestUser;
import sit.us1.backend.dtos.JwtDTO.JwtTokenResponseDTO;
import sit.us1.backend.entities.account.CustomUserDetails;
import sit.us1.backend.services.JwtTokenUtil;
import sit.us1.backend.services.JwtUserDetailsService;

@CrossOrigin(origins = {"http://localhost:5173", "http://ip23us1.sit.kmutt.ac.th", "http://intproj23.sit.kmutt.ac.th"})
@RestController
//@RequestMapping("/authentications")
public class AuthenticationController {
    @Autowired
    JwtUserDetailsService jwtUserDetailsService;
    @Autowired
    JwtTokenUtil jwtTokenUtil;
    @Autowired
    AuthenticationManager authenticationManager;
    @PostMapping("/login")
    public ResponseEntity<JwtTokenResponseDTO> login(@RequestBody @Valid JwtRequestUser jwtRequestUser) {
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(jwtRequestUser.getUserName(), jwtRequestUser.getPassword());
        Authentication authentication = authenticationManager.authenticate(authenticationToken);
        if (!authentication.isAuthenticated()) {
            throw new UsernameNotFoundException("Invalid user or password");
        }
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        String token = jwtTokenUtil.generateToken(userDetails);
        JwtTokenResponseDTO jwtTokenResponseDTO = new JwtTokenResponseDTO();
        jwtTokenResponseDTO.setAccess_token(token);
        return ResponseEntity.ok(jwtTokenResponseDTO);
    }

    @GetMapping("/validate-token")
    public ResponseEntity<Object> validateToken(@RequestHeader("Authorization") String requestTokenHeader) {
        Claims claims = null;
        String jwtToken = null;
        if (requestTokenHeader != null) {
            jwtToken = requestTokenHeader;
            try {
                claims = jwtTokenUtil.getAllClaimsFromToken(jwtToken);
            } catch (IllegalArgumentException e) {
                System.out.println("Unable to get JWT Token");
            } catch (ExpiredJwtException e) {
                System.out.println("JWT Token has expired");
            }
        } else {
            throw new ResponseStatusException(HttpStatus.EXPECTATION_FAILED, "JWT Token is null");
        }
        return ResponseEntity.ok(claims);
    }
}
