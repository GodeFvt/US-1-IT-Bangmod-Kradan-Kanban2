package sit.us1.backend.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.impl.DefaultClaims;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.stereotype.Component;
import sit.us1.backend.entities.account.CustomUserDetails;
import sit.us1.backend.entities.account.MsUser;
import sit.us1.backend.properties.JwtProperties;

import java.io.Serializable;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Component
public class JwtTokenUtil implements Serializable {

    private final String SECRET_KEY;
    private final String SECRET_KEY_REFRESH;
    private final long jwtExpiration;
    private final long refreshExpiration;
    private final String issuer;
    private final String issuerMS;

    private final SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.HS256;

    public JwtTokenUtil(JwtProperties jwtProperties) {
        this.SECRET_KEY = jwtProperties.getSecret();
        this.SECRET_KEY_REFRESH = jwtProperties.getSecretRefresh();
        this.jwtExpiration = (long) (jwtProperties.getMaxTokenIntervalHour() * 60 * 60 * 1000);
        this.refreshExpiration = (long) (jwtProperties.getMaxRefreshTokenIntervalHour() * 60 * 60 * 1000);
        this.issuer = jwtProperties.getIssuer();
        this.issuerMS = jwtProperties.getIssuerMS();
    }

    public String getSubjectFromToken(String token, boolean isRefreshToken) {
        return getClaimFromToken(token, Claims::getSubject, isRefreshToken);
    }

    public Date getExpirationDateFromToken(String token, boolean isRefreshToken) {
        return getClaimFromToken(token, Claims::getExpiration, isRefreshToken);
    }

    public <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver, boolean isRefreshToken) {
        final Claims claims = getAllClaimsFromToken(token, isRefreshToken);
        return claimsResolver.apply(claims);
    }

    public Claims getAllClaimsFromToken(String token, boolean isRefreshToken) {
        String key = isRefreshToken ? SECRET_KEY_REFRESH : SECRET_KEY;
        return Jwts.parser().setSigningKey(key).parseClaimsJws(token).getBody();
    }

    private Boolean isTokenExpired(String token, boolean isRefreshToken) {
        final Date expiration = getExpirationDateFromToken(token, isRefreshToken);
        return expiration.before(new Date());
    }

    public String generateToken(CustomUserDetails userDetails, boolean isRefreshToken) {
        String subject = isRefreshToken ? userDetails.getOid() : userDetails.getUsername();
        Map<String, Object> claims = new HashMap<>();
        if (isRefreshToken) {
            claims.put("oid", userDetails.getOid());
        } else {
            claims.put("name", userDetails.getName());
            claims.put("oid", userDetails.getOid());
            claims.put("email", userDetails.getEmail());
            claims.put("role", userDetails.getRole().toString());
        }
        return doGenerateToken(claims, subject, isRefreshToken);
    }

    private String doGenerateToken(Map<String, Object> claims, String subject, boolean isRefreshToken) {
        long expirationTime = isRefreshToken ? refreshExpiration : jwtExpiration;
        String key = isRefreshToken ? SECRET_KEY_REFRESH : SECRET_KEY;

        return Jwts.builder().setHeaderParam("typ", "JWT")
                .setClaims(claims)
                .setSubject(subject)
                .setIssuer(issuer)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expirationTime))
                .signWith(signatureAlgorithm, key).compact();
    }

    public Boolean validateToken(String token, CustomUserDetails userDetails, boolean isRefreshToken) {
        final String subject = getSubjectFromToken(token, isRefreshToken);
        return (subject.equals(isRefreshToken ? userDetails.getOid() : userDetails.getUsername()) && !isTokenExpired(token, isRefreshToken));
    }

    public Boolean isMicrosoftToken(String token) throws JsonProcessingException {
        isTokenExpired(token);
        try {
            NimbusJwtDecoder nimbusJwtDecoder = NimbusJwtDecoder.withIssuerLocation(issuerMS).build();
            Jwt jwt = nimbusJwtDecoder.decode(token);
            return true;
        } catch (JwtException | IllegalArgumentException |ExpiredJwtException e) {
            return false;
        }
    }

    public MsUser getMsUserFromToken(String token) {
        Claims claims = getClaimsFromMicrosoftToken(token);
        if (claims == null) {
            return null;
        }
        return new MsUser(
                claims.get("oid", String.class),
                claims.get("name", String.class),
                claims.get("preferred_username", String.class)
        );
    }

    private String getPayloadFromToken(String token) throws IllegalArgumentException {
        String[] parts = token.split("\\.");
        if (parts.length != 3) {
            throw new IllegalArgumentException("Invalid JWT token format");
        }
        return new String(Base64.getUrlDecoder().decode(parts[1]));
    }

    private void isTokenExpired(String token) throws JsonProcessingException {
            String payload = getPayloadFromToken(token);
            long exp = new ObjectMapper().readTree(payload).get("exp").asLong();
            long currentTime = System.currentTimeMillis() / 1000;
            if (currentTime >= exp){
                throw new ExpiredJwtException(null, null, "JWT Token has expired");
            }
    }

    private Claims getClaimsFromMicrosoftToken(String token) {
        try {
            NimbusJwtDecoder nimbusJwtDecoder = NimbusJwtDecoder.withIssuerLocation("https://login.microsoftonline.com/79845616-9df0-43e0-8842-e300feb2642a/v2.0").build();
            Jwt jwt = nimbusJwtDecoder.decode(token);
            return new DefaultClaims(jwt.getClaims());
        } catch (JwtException | IllegalArgumentException ex) {
            return null;
        }
    }
}

