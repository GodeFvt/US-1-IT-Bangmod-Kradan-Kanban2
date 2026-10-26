package sit.us1.backend.entities.account;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "oauth_accounts", uniqueConstraints = {
        @UniqueConstraint(name = "uq_oauth_provider_subject", columnNames = {"provider", "provider_subject"})
})
public class OAuthAccount {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_oid", nullable = false, length = 40)
    private String userOid;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OAuthProvider provider;

    @Column(name = "provider_subject", nullable = false, length = 255)
    private String providerSubject;

    @Column(length = 254)
    private String email;

    @Column(name = "created_on", insertable = false, updatable = false)
    private ZonedDateTime createdOn;

    @Column(name = "updated_on", insertable = false, updatable = false)
    private ZonedDateTime updatedOn;
}
