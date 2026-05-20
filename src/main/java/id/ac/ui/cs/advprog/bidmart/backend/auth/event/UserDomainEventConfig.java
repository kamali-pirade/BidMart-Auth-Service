package id.ac.ui.cs.advprog.bidmart.backend.auth.event;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UserDomainEventConfig {

    @Bean
    public UserDomainEventPublisher userDomainEventPublisher() {
        return UserDomainEventPublisher.noop();
    }
}
