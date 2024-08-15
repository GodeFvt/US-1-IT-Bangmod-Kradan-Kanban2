package sit.us1.backend.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import sit.us1.backend.entities.account.AuthUser;
import sit.us1.backend.entities.account.CustomUserDetails;
import sit.us1.backend.entities.account.User;
import sit.us1.backend.repositories.account.UserRepository;

import java.util.ArrayList;
import java.util.List;

@Service
public class JwtUserDetailsService implements UserDetailsService {
    @Autowired
    private UserRepository customerRepository;

    @Override
    public UserDetails loadUserByUsername(String userName) throws UsernameNotFoundException {
        User user = customerRepository.findByUsername(userName);
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, userName + " does not exist !!");
        }
        List<GrantedAuthority> roles = new ArrayList<>();
        GrantedAuthority grantedAuthority = new GrantedAuthority() {
            @Override
            public String getAuthority() {
                return user.getRole().toString();
            }
        };
        roles.add(grantedAuthority);
        UserDetails userDetails = new CustomUserDetails(userName,user.getName(), user.getOid(),user.getEmail() ,user.getPassword(), user.getRole().toString(), roles);
        return userDetails;
    }
}

