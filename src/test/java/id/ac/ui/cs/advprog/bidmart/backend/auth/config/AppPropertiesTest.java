package id.ac.ui.cs.advprog.bidmart.backend.auth.config;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AppPropertiesTest {

    @Test
    void baseUrlGetterReturnsAssignedValue() {
        AppProperties properties = new AppProperties();
        properties.setBaseUrl("http://localhost:8080");
        properties.setFrontendUrl("http://localhost:3000");

        assertEquals("http://localhost:8080", properties.getBaseUrl());
        assertEquals("http://localhost:3000", properties.getFrontendUrl());
    }
}
