package id.ac.ui.cs.advprog.bidmart.backend.auth.dto;

import jakarta.validation.constraints.NotNull;

import java.util.List;

public class UpdateUserPermissionsRequestDTO {
    @NotNull
    public List<String> permissions;
}
