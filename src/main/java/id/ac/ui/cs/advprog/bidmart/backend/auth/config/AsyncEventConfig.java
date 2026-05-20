package id.ac.ui.cs.advprog.bidmart.backend.auth.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ApplicationEventMulticaster;
import org.springframework.context.event.SimpleApplicationEventMulticaster;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.util.ErrorHandler;

import java.util.concurrent.Executor;

@Configuration
@EnableAsync
public class AsyncEventConfig {

    private static final Logger log = LoggerFactory.getLogger(AsyncEventConfig.class);

    @Bean(name = AbstractApplicationContext.APPLICATION_EVENT_MULTICASTER_BEAN_NAME)
    public ApplicationEventMulticaster applicationEventMulticaster(Executor applicationEventExecutor,
                                                                   ErrorHandler applicationEventErrorHandler) {
        SimpleApplicationEventMulticaster multicaster = new SimpleApplicationEventMulticaster();
        multicaster.setTaskExecutor(applicationEventExecutor);
        multicaster.setErrorHandler(applicationEventErrorHandler);
        return multicaster;
    }

    @Bean
    public Executor applicationEventExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setThreadNamePrefix("bidmart-event-");
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(4);
        executor.setQueueCapacity(100);
        executor.initialize();
        return executor;
    }

    @Bean
    public ErrorHandler applicationEventErrorHandler() {
        return throwable -> log.error("Async application event listener failed", throwable);
    }
}
