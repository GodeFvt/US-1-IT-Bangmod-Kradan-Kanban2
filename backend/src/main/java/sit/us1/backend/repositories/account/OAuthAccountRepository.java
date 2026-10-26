package sit.us1.backend.repositories.account;

import org.springframework.data.jpa.repository.JpaRepository;
import sit.us1.backend.entities.account.OAuthAccount;
import sit.us1.backend.entities.account.OAuthProvider;

import java.util.Optional;

public interface OAuthAccountRepository extends JpaRepository<OAuthAccount, Long> {
    Optional<OAuthAccount> findByProviderAndProviderSubject(OAuthProvider provider, String providerSubject);
}
