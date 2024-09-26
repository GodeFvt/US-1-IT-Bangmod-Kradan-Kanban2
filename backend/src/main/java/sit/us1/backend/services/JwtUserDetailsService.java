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
import sit.us1.backend.entities.account.User;
import sit.us1.backend.repositories.account.UserRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class JwtUserDetailsService implements UserDetailsService {
    @Autowired
    private UserRepository userRepository;

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
        System.out.println("oid: " + oid);
        Optional<User> user = userRepository.findById(oid);
        if (!user.isPresent()) {
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
}

