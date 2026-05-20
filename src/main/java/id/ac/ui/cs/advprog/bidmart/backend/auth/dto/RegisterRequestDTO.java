package id.ac.ui.cs.advprog.bidmart.backend.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class RegisterRequestDTO {
    @NotBlank
    @Email
    public String email;

    @NotBlank
    @Size(min = 8, max = 50)
    public String password;

    @NotBlank
    @Size(max = 100)
    public String displayName;

    @Pattern(regexp = "(?i)BUYER|SELLER", message = "Role must be BUYER or SELLER")
    public String role;
}
