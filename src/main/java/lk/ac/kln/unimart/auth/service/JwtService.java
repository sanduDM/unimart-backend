package lk.ac.kln.unimart.auth.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
public class JwtService {

    private final JwtEncoder jwtEncoder;
    private final JwtDecoder jwtDecoder;
    private final long accessMinutes;

    public JwtService(
            JwtEncoder jwtEncoder,
            JwtDecoder jwtDecoder,
            @Value("${jwt.access-minutes:15}") long accessMinutes
    ) {
        this.jwtEncoder = jwtEncoder;
        this.jwtDecoder = jwtDecoder;
        this.accessMinutes = accessMinutes;
    }

    public String generateToken(Long userId, String universityEmail, String role) {
        Instant now = Instant.now();
        Instant expiry = now.plus(accessMinutes, ChronoUnit.MINUTES);

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("unimart-backend")
                .issuedAt(now)
                .expiresAt(expiry)
                .subject(String.valueOf(userId))
                .claim("email", universityEmail)
                .claim("role", role)
                .build();

        return jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
    }

    public long getAccessMinutes() {
        return accessMinutes;
    }

    public Jwt decode(String token) {
        return jwtDecoder.decode(token);
    }
}
