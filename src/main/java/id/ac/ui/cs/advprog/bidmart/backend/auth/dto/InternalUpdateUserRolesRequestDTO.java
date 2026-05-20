package id.ac.ui.cs.advprog.bidmart.backend.auth.dto;

import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public class InternalUpdateUserRolesRequestDTO {
    @NotEmpty
    public List<String> roles;

    public List<String> permissions;
}
