package sit.us1.backend.services;

import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sit.us1.backend.entities.account.*;
import sit.us1.backend.repositories.account.OAuthAccountRepository;
import sit.us1.backend.repositories.account.UserRepository;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
public class GoogleOAuthService {
    private final UserRepository userRepository;
    private final OAuthAccountRepository oauthAccountRepository;

    public GoogleOAuthService(UserRepository userRepository, OAuthAccountRepository oauthAccountRepository) {
        this.userRepository = userRepository;
        this.oauthAccountRepository = oauthAccountRepository;
    }

    @Transactional
    public CustomUserDetails login(String subject, String email, String displayName) {
        if (subject == null || subject.isBlank() || email == null || email.isBlank()) {
            throw new InternalAuthenticationServiceException("Google account does not provide the required identity claims.");
        }

        User user = oauthAccountRepository
                .findByProviderAndProviderSubject(OAuthProvider.GOOGLE, subject)
                .flatMap(account -> userRepository.findById(account.getUserOid()))
                .orElseGet(() -> linkOrCreateUser(subject, email, displayName));

        List<GrantedAuthority> authorities = List.of(() -> user.getRole().toString());
        return new CustomUserDetails(
                user.getUsername(), user.getName(), user.getOid(), user.getEmail(), "", user.getRole(), authorities
        );
    }

    private User linkOrCreateUser(String subject, String email, String displayName) {
        User user = userRepository.findByEmail(email);
        if (user == null) {
            user = new User();
            user.setOid(UUID.randomUUID().toString());
            user.setEmail(email);
            user.setName(displayName == null || displayName.isBlank() ? email : displayName);
            user.setUsername(uniqueUsername(email));
            user.setRole(Role.STUDENT);
            user = userRepository.save(user);
        }

        if (oauthAccountRepository.findByUserOidAndProvider(user.getOid(), OAuthProvider.GOOGLE).isEmpty()) {
            OAuthAccount account = new OAuthAccount();
            account.setUserOid(user.getOid());
            account.setProvider(OAuthProvider.GOOGLE);
            account.setProviderSubject(subject);
            account.setEmail(email);
            oauthAccountRepository.save(account);
        }
        return user;
    }

    private String uniqueUsername(String email) {
        int separator = email.indexOf('@');
        String base = email.substring(0, separator > 0 ? separator : email.length())
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9._-]", "");
        if (base.isBlank()) base = "google-user";
        base = base.substring(0, Math.min(base.length(), 42));

        String candidate = base;
        int suffix = 1;
        while (userRepository.existsByUsername(candidate)) {
            String number = "-" + suffix++;
            candidate = base.substring(0, Math.min(base.length(), 50 - number.length())) + number;
        }
        return candidate;
    }
}
