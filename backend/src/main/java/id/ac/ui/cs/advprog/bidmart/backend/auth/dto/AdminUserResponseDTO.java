package id.ac.ui.cs.advprog.bidmart.backend.auth.dto;

import id.ac.ui.cs.advprog.bidmart.backend.auth.entity.User;
import id.ac.ui.cs.advprog.bidmart.backend.auth.entity.UserStatus;

import java.util.List;
import java.util.UUID;

public class AdminUserResponseDTO {
    public UUID id;
    public String email;
    public String displayName;
    public List<String> roles;
    public String status;
    public boolean suspended;
    public boolean enabled;
    public boolean emailVerified;

    public static AdminUserResponseDTO from(User user) {
        AdminUserResponseDTO dto = new AdminUserResponseDTO();

        dto.id = user.getId();
        dto.email = user.getEmail();
        dto.displayName = user.getDisplayName();
        dto.roles = user.getRolesList();
        dto.status = user.getStatus().name();
        dto.suspended = user.getStatus() == UserStatus.SUSPENDED;
        dto.enabled = user.getStatus() == UserStatus.ACTIVE;
        dto.emailVerified = user.isEmailVerified();

        return dto;
    }
}