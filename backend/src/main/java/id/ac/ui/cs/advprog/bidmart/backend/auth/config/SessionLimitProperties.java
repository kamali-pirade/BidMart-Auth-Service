package id.ac.ui.cs.advprog.bidmart.backend.auth.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "auth")
public class SessionLimitProperties {
    public enum SessionLimitPolicy {
        REVOKE_OLDEST_SESSION,
        REJECT_NEW_LOGIN
    }

    private int maxConcurrentSessions = 3;
    private SessionLimitPolicy sessionLimitPolicy = SessionLimitPolicy.REVOKE_OLDEST_SESSION;

    public int getMaxConcurrentSessions() {
        return maxConcurrentSessions;
    }

    public void setMaxConcurrentSessions(int maxConcurrentSessions) {
        this.maxConcurrentSessions = maxConcurrentSessions;
    }

    public SessionLimitPolicy getSessionLimitPolicy() {
        return sessionLimitPolicy;
    }

    public void setSessionLimitPolicy(SessionLimitPolicy sessionLimitPolicy) {
        this.sessionLimitPolicy = sessionLimitPolicy;
    }
}
