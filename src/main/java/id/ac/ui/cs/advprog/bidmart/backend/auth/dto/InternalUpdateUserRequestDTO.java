package id.ac.ui.cs.advprog.bidmart.backend.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

public class InternalUpdateUserRequestDTO {
    @Email
    public String email;

    @Size(max = 100)
    public String displayName;

    public String avatarUrl;

    public Boolean emailVerified;
}