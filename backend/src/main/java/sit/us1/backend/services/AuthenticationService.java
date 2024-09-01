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
import sit.us1.backend.exceptions.BadRequestException;
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
                System.out.println(boardUserRepository.save(user));
            }
        } catch (Exception e) {
            throw new BadRequestException("Cannot create user");
        }

        return jwtTokenUtil.generateToken(userDetails);
    }

}
