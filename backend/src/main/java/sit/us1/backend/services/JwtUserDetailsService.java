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
        List<GrantedAuthority> authorities = getAuthorities(user.getRole().toString());
        return new CustomUserDetails(userName,user.getName(), user.getOid(),user.getEmail() ,user.getPassword(), user.getRole(), authorities);
    }

    public CustomUserDetails loadUserByOid(String oid) throws UsernameNotFoundException {
        Optional<User> user = userRepository.findById(oid);
        if (user.isEmpty()) {
            throw new InternalAuthenticationServiceException("Token is invalid.");
        }
        List<GrantedAuthority> authorities = getAuthorities(user.get().getRole().toString());
        return new CustomUserDetails(user.get().getUsername(),user.get().getName(), user.get().getOid(),user.get().getEmail() ,user.get().getPassword(), user.get().getRole(), authorities);
    }

    public CustomUserDetails getUserDetailsMS(MsUser msUser) {
        BoardUser user = boardUserRepository.findById(msUser.getOid()).orElse(null);
        User userItbkk_Shared = userRepository.findById(msUser.getOid()).orElse(null);
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
        return getCustomUserDetails(msUser, userItbkk_Shared);
    }

    private CustomUserDetails getCustomUserDetails(MsUser msUser, User userItbkk_Shared) {
        CustomUserDetails userDetails;
        List<GrantedAuthority> authorities;
        if (userItbkk_Shared != null ){
            authorities = getAuthorities(userItbkk_Shared.getRole().toString());
            userDetails = new CustomUserDetails(userItbkk_Shared.getUsername(), userItbkk_Shared.getName(), userItbkk_Shared.getOid(), userItbkk_Shared.getEmail(), userItbkk_Shared.getPassword(), userItbkk_Shared.getRole(),authorities);
        } else {
            authorities = getAuthorities("USER");
            userDetails = new CustomUserDetails(msUser.getName(), msUser.getName(), msUser.getOid(), msUser.getEmail(), "", null, authorities);
        }
        return userDetails;
    }

    private List<GrantedAuthority> getAuthorities(String role) {
        List<GrantedAuthority> authorities = new ArrayList<>();
        GrantedAuthority grantedAuthority = new GrantedAuthority() {
            @Override
            public String getAuthority() {
                return role;
            }
        };
        authorities.add(grantedAuthority);
        return authorities;
    }
}

