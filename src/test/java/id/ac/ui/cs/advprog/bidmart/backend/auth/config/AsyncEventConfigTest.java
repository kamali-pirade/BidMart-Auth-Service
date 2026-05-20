package id.ac.ui.cs.advprog.bidmart.backend.auth.config;

import org.junit.jupiter.api.Test;
import org.springframework.context.event.ApplicationEventMulticaster;
import org.springframework.context.event.SimpleApplicationEventMulticaster;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.util.ErrorHandler;

import java.util.concurrent.Executor;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class AsyncEventConfigTest {

    @Test
    void createsAsyncApplicationEventMulticaster() {
        AsyncEventConfig config = new AsyncEventConfig();
        Executor executor = config.applicationEventExecutor();
        ErrorHandler errorHandler = config.applicationEventErrorHandler();

        ApplicationEventMulticaster multicaster = config.applicationEventMulticaster(executor, errorHandler);

        assertInstanceOf(SimpleApplicationEventMulticaster.class, multicaster);
        assertDoesNotThrow(() -> errorHandler.handleError(new RuntimeException("boom")));
        ((ThreadPoolTaskExecutor) executor).shutdown();
    }
}
