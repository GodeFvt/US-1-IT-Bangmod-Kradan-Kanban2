package sit.us1.backend.entities.taskboard;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import sit.us1.backend.entities.account.Role;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "users")
public class BoardUser {
    @Id
    @Column(name = "oid")
    private String id;
    @Column(name = "name")
    private String name;
    @JsonIgnore
    @Column(name = "username")
    private String username;
    @JsonIgnore
    @Column(name = "email")
    private String email;
    @JsonIgnore
    @Enumerated(EnumType.STRING)
    @Column(name="role")
    private Role role;
}
