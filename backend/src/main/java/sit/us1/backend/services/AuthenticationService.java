package sit.us1.backend.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import sit.us1.backend.dtos.JwtDTO.JwtRequestUser;
import sit.us1.backend.entities.account.CustomUserDetails;
import sit.us1.backend.entities.taskboard.BoardUser;
import sit.us1.backend.repositories.taskboard.BoardUserRepository;

@Service
public class AuthenticationService {
    @Autowired
    JwtTokenUtil jwtTokenUtil;
    @Autowired
    AuthenticationManager authenticationManager;
    @Autowired
    BoardUserRepository boardUserRepository;
    @Autowired
    private JwtUserDetailsService jwtUserDetailsService;

    public String login(JwtRequestUser jwtRequestUser) {
        String username = jwtRequestUser.getUserName();
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(username, jwtRequestUser.getPassword());
        Authentication authentication = authenticationManager.authenticate(authenticationToken);
        if (!authentication.isAuthenticated()) {
            throw new UsernameNotFoundException("Username or Password is incorrect.");
        }
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        if (boardUserRepository.findByUsername(username) == null) {
            BoardUser user = new BoardUser();
            user.setId(userDetails.getOid());
            user.setUsername(userDetails.getUsername());
            user.setName(userDetails.getName());
            user.setEmail(userDetails.getEmail());
            user.setRole(userDetails.getRole());
            boardUserRepository.save(user);
        }
        return jwtTokenUtil.generateToken(userDetails);
    }

}
