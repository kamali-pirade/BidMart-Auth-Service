package id.ac.ui.cs.advprog.bidmart.backend.auth.event;

import id.ac.ui.cs.advprog.bidmart.common.event.UserRoleChangedEvent;
import id.ac.ui.cs.advprog.bidmart.common.event.UserSuspendedEvent;

public interface UserDomainEventPublisher {
    void publishUserSuspended(UserSuspendedEvent event);

    void publishUserRoleChanged(UserRoleChangedEvent event);

    static UserDomainEventPublisher noop() {
        return new UserDomainEventPublisher() {
            @Override
            public void publishUserSuspended(UserSuspendedEvent event) {
                // no-op for legacy tests
            }

            @Override
            public void publishUserRoleChanged(UserRoleChangedEvent event) {
                // no-op for legacy tests
            }
        };
    }
}
