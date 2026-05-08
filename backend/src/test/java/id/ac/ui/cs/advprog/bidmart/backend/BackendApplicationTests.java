package id.ac.ui.cs.advprog.bidmart.backend;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class BackendApplicationTests {

    @Test
    void contextLoads() {
        BackendApplication.main(new String[]{"--server.port=0"});
    }

    @Test
    void mainHonorsExistingSystemPropertyWhenLoadingDotenvDefaults() {
        System.setProperty("APP_BASE_URL", "http://already-set");
        try {
            BackendApplication.main(new String[]{"--server.port=0"});
        } finally {
            System.clearProperty("APP_BASE_URL");
        }
    }

}
