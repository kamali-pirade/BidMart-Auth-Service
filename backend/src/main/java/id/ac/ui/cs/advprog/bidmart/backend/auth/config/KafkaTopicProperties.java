package id.ac.ui.cs.advprog.bidmart.backend.auth.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "kafka.topic")
public class KafkaTopicProperties {
    private String userSuspended = "user-suspended";
    private String userRoleChanged = "user-role-changed";

    public String getUserSuspended() {
        return userSuspended;
    }

    public void setUserSuspended(String userSuspended) {
        this.userSuspended = userSuspended;
    }

    public String getUserRoleChanged() {
        return userRoleChanged;
    }

    public void setUserRoleChanged(String userRoleChanged) {
        this.userRoleChanged = userRoleChanged;
    }
}
