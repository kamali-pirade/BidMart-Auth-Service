package id.ac.ui.cs.advprog.bidmart.backend.repository;

import id.ac.ui.cs.advprog.bidmart.backend.model.PageView;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class CounterRepositoryTest {

    @Autowired
    private CounterRepository counterRepository;

    private PageView pageView;

    @BeforeEach
    void setUp() {
        pageView = new PageView();
        pageView.setId("test_page");
        pageView.setCount(5);
    }

    @Test
    void testSaveAndRetrievePageView() {
        counterRepository.save(pageView);

        Optional<PageView> found = counterRepository.findById("test_page");
        assertTrue(found.isPresent());
        assertEquals("test_page", found.get().getId());
        assertEquals(5, found.get().getCount());
    }

    @Test
    void testFindByIdNotFound() {
        Optional<PageView> found = counterRepository.findById("nonexistent");
        assertFalse(found.isPresent());
    }

    @Test
    void testDeletePageView() {
        counterRepository.save(pageView);
        assertTrue(counterRepository.existsById("test_page"));

        counterRepository.deleteById("test_page");
        assertFalse(counterRepository.existsById("test_page"));
    }

    @Test
    void testUpdatePageViewCounter() {
        counterRepository.save(pageView);

        PageView found = counterRepository.findById("test_page").get();
        found.setCount(10);
        counterRepository.save(found);

        PageView updated = counterRepository.findById("test_page").get();
        assertEquals(10, updated.getCount());
    }

    @Test
    void testMultiplePageViews() {
        PageView pageView2 = new PageView();
        pageView2.setId("another_page");
        pageView2.setCount(20);

        counterRepository.save(pageView);
        counterRepository.save(pageView2);

        Optional<PageView> found1 = counterRepository.findById("test_page");
        Optional<PageView> found2 = counterRepository.findById("another_page");

        assertTrue(found1.isPresent());
        assertTrue(found2.isPresent());
        assertEquals(5, found1.get().getCount());
        assertEquals(20, found2.get().getCount());
    }

    @Test
    void testCountAllPageViews() {
        PageView pageView2 = new PageView();
        pageView2.setId("page_2");
        pageView2.setCount(15);

        counterRepository.save(pageView);
        counterRepository.save(pageView2);

        long count = counterRepository.count();
        assertEquals(2, count);
    }

    @Test
    void testIncrementCounter() {
        counterRepository.save(pageView);

        PageView found = counterRepository.findById("test_page").get();
        found.setCount(found.getCount() + 1);
        counterRepository.save(found);

        PageView incremented = counterRepository.findById("test_page").get();
        assertEquals(6, incremented.getCount());
    }
}
