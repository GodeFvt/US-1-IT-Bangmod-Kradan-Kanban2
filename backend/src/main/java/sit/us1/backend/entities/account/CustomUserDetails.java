package sit.us1.backend.entities.account;

import lombok.Getter;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;


@Getter
@Setter
public class CustomUserDetails implements UserDetails {
    private String username;
    private String name;
    private Character oid;
    private String email;
    private String password;
    private String role;
    private Collection<? extends GrantedAuthority> authorities;

    public CustomUserDetails(String username,String name, Character oid, String email,String password, String role, Collection<? extends GrantedAuthority> authorities) {
        this.username = username;
        this.name = name;
        this.oid = oid;
        this.email = email;
        this.password = password;
        this.role = role;
        this.authorities = authorities;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
