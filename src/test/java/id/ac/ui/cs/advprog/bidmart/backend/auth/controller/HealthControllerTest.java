package id.ac.ui.cs.advprog.bidmart.backend.auth.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(MockitoExtension.class)
class HealthControllerTest {

    @InjectMocks
    private HealthController healthController;

    @Test
    void testHealthEndpoint_Success() {
        ResponseEntity<String> response = healthController.health();

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("OK", response.getBody());
    }

    @Test
    void testHealthEndpoint_ReturnsOkStatus() {
        ResponseEntity<String> response = healthController.health();
        assertEquals(200, response.getStatusCode().value());
    }

    @Test
    void testHealthEndpoint_ReturnsOkMessage() {
        ResponseEntity<String> response = healthController.health();
        assertEquals("OK", response.getBody());
    }

    @Test
    void testHealthEndpoint_CalledMultipleTimes() {
        // Test that health check can be called multiple times
        ResponseEntity<String> response1 = healthController.health();
        ResponseEntity<String> response2 = healthController.health();
        ResponseEntity<String> response3 = healthController.health();

        assertEquals(HttpStatus.OK, response1.getStatusCode());
        assertEquals(HttpStatus.OK, response2.getStatusCode());
        assertEquals(HttpStatus.OK, response3.getStatusCode());

        assertEquals("OK", response1.getBody());
        assertEquals("OK", response2.getBody());
        assertEquals("OK", response3.getBody());
    }
}
