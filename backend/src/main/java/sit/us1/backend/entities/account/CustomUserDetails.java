package sit.us1.backend.entities.account;

import lombok.Getter;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.User;
import java.util.Collection;


@Getter
@Setter
public class CustomUserDetails extends User implements UserDetails {
    private String name;
    private Character oid;
    private String email;
    private String role;

    public CustomUserDetails(String username,String name, Character oid, String email,String password, String role, Collection<? extends GrantedAuthority> authorities) {
        super(username, password, authorities);
        this.name = name;
        this.oid = oid;
        this.email = email;
        this.role = role;
    }
}
