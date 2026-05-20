package id.ac.ui.cs.advprog.bidmart.backend.auth.dto;

import jakarta.validation.constraints.NotBlank;

public class InternalUpdateUserStatusRequestDTO {
    @NotBlank
    public String status;
}