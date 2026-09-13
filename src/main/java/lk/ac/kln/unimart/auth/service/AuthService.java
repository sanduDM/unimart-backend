package lk.ac.kln.unimart.auth.service;

import lk.ac.kln.unimart.auth.dto.AuthResponse;
import lk.ac.kln.unimart.auth.dto.LoginRequest;
import lk.ac.kln.unimart.auth.dto.RegisterRequest;
import lk.ac.kln.unimart.auth.entity.User;
import lk.ac.kln.unimart.auth.repository.UserRepository;
import lk.ac.kln.unimart.common.exception.ConflictException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
public class AuthService {

    private final UserRepository users;
    private final PasswordEncoder passwordEncoder;
    private final JwtEncoder jwtEncoder;

    public AuthService(UserRepository users, PasswordEncoder passwordEncoder, JwtEncoder jwtEncoder) {
        this.users = users;
        this.passwordEncoder = passwordEncoder;
        this.jwtEncoder = jwtEncoder;
    }

    public void register(RegisterRequest request) {
        if (users.findByUniversityEmail(request.universityEmail()).isPresent()) {
            throw new ConflictException("Email already registered");
        }

        User user = new User();
        user.setUniversityEmail(request.universityEmail());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setFullName(request.fullName());
        user.setRole("STUDENT");
        user.setEmailVerified(true);

        users.save(user);
    }

    public AuthResponse login(LoginRequest request) {
        User user = users.findByUniversityEmail(request.universityEmail())
                .orElseThrow(() -> new BadCredentialsException("Invalid credentials"));

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new BadCredentialsException("Invalid credentials");
        }

        Instant now = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("unimart")
                .issuedAt(now)
                .expiresAt(now.plus(1, ChronoUnit.HOURS))
                .subject(user.getUniversityEmail())
                .claim("role", user.getRole())
                .build();

        String token = jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
        return new AuthResponse(token);
    }
}