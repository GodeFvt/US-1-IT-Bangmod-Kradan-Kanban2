package sit.us1.backend.entities.account;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "user_credentials")
public class UserCredential {
    @Id
    @Column(name = "user_oid", length = 40)
    private String userOid;

    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;
}
