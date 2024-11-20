package sit.us1.backend.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import sit.us1.backend.entities.account.CustomUserDetails;
import sit.us1.backend.entities.account.MsUser;
import sit.us1.backend.entities.account.User;
import sit.us1.backend.entities.taskboard.BoardUser;
import sit.us1.backend.repositories.account.UserRepository;
import sit.us1.backend.repositories.taskboard.BoardUserRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class JwtUserDetailsService implements UserDetailsService {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private BoardUserRepository boardUserRepository;

    @Override
    public UserDetails loadUserByUsername(String userName) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(userName);
        if (user == null) {
            throw new InternalAuthenticationServiceException("Username or Password is incorrect.");
        }
        List<GrantedAuthority> authorities = new ArrayList<>();
        GrantedAuthority grantedAuthority = new GrantedAuthority() {
            @Override
            public String getAuthority() {
                return user.getRole().toString();
            }
        };
        authorities.add(grantedAuthority);
        return  new CustomUserDetails(userName,user.getName(), user.getOid(),user.getEmail() ,user.getPassword(), user.getRole(), authorities);
    }

    public CustomUserDetails loadUserByOid(String oid) throws UsernameNotFoundException {
        Optional<User> user = userRepository.findById(oid);
        if (user.isEmpty()) {
            throw new InternalAuthenticationServiceException("Token is invalid.");
        }
        List<GrantedAuthority> authorities = new ArrayList<>();
        GrantedAuthority grantedAuthority = new GrantedAuthority() {
            @Override
            public String getAuthority() {
                return user.get().getRole().toString();
            }
        };
        authorities.add(grantedAuthority);
        return new CustomUserDetails(user.get().getUsername(),user.get().getName(), user.get().getOid(),user.get().getEmail() ,user.get().getPassword(), user.get().getRole(), authorities);
    }

    public CustomUserDetails getUserDetailsMS(MsUser msUser) {
        BoardUser user = boardUserRepository.findById(msUser.getOid()).orElse(null);
        CustomUserDetails userItbkk_Shared = loadUserByOid(msUser.getOid());
        BoardUser newUser = new BoardUser();
        if (user == null) {
            if (userItbkk_Shared != null) {
                newUser.setId(userItbkk_Shared.getOid());
                newUser.setUsername(userItbkk_Shared.getUsername());
                newUser.setName(userItbkk_Shared.getName());
                newUser.setEmail(userItbkk_Shared.getEmail());
            } else {
                newUser.setId(msUser.getOid());
                newUser.setUsername(msUser.getName());
                newUser.setName(msUser.getName());
                newUser.setEmail(msUser.getEmail());
            }
            try {
                boardUserRepository.save(newUser);
            } catch (Exception e) {
                throw new InternalAuthenticationServiceException("Cannot create user.");
            }
        }
        return userItbkk_Shared != null ? userItbkk_Shared : new CustomUserDetails(msUser.getName(), msUser.getName(), msUser.getOid(), msUser.getEmail(), "", null, List.of(() -> "ROLE_USER"));
    }
}

