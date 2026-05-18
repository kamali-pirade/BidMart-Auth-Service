package id.ac.ui.cs.advprog.bidmart.backend.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

public class InternalCreateUserRequestDTO {
    @NotBlank
    @Email
    public String email;

    @NotBlank
    @Size(min = 8)
    public String password;

    @NotBlank
    @Size(max = 100)
    public String displayName;

    public String avatarUrl;

    public Boolean emailVerified;

    public List<String> roles;

    public List<String> permissions;

    public String status;
}
