package lk.ac.kln.unimart.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank @Email String universityEmail,
        @NotBlank @Size(min = 8, max = 100) String password,
        @NotBlank String fullName
) {}