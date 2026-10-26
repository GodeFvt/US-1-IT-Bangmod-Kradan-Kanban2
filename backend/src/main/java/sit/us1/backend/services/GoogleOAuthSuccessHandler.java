package sit.us1.backend.services;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;
import sit.us1.backend.dtos.JwtDTO.JwtTokenResponseDTO;
import sit.us1.backend.entities.account.CustomUserDetails;
import sit.us1.backend.utils.JwtTokenUtil;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Component
public class GoogleOAuthSuccessHandler implements AuthenticationSuccessHandler {
    private final GoogleOAuthService googleOAuthService;
    private final JwtTokenUtil jwtTokenUtil;
    private final String frontendRedirectUri;

    public GoogleOAuthSuccessHandler(
            GoogleOAuthService googleOAuthService,
            JwtTokenUtil jwtTokenUtil,
            @Value("${app.oauth2.frontend-redirect-uri}") String frontendRedirectUri
    ) {
        this.googleOAuthService = googleOAuthService;
        this.jwtTokenUtil = jwtTokenUtil;
        this.frontendRedirectUri = frontendRedirectUri;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        OAuth2User googleUser = (OAuth2User) authentication.getPrincipal();
        CustomUserDetails user = googleOAuthService.login(
                googleUser.getAttribute("sub"),
                googleUser.getAttribute("email"),
                googleUser.getAttribute("name")
        );

        String accessToken = jwtTokenUtil.generateToken(user, false);
        String refreshToken = jwtTokenUtil.generateToken(user, true);
        String fragment = UriComponentsBuilder.newInstance()
                .queryParam("access_token", accessToken)
                .queryParam("refresh_token", refreshToken)
                .build()
                .encode(StandardCharsets.UTF_8)
                .toUriString();
        response.sendRedirect(frontendRedirectUri + "#" + fragment.substring(1));
    }
}
