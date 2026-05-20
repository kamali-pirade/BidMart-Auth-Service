package id.ac.ui.cs.advprog.bidmart.backend.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PageViewTest {

    private PageView pageView;

    @BeforeEach
    void setUp() {
        pageView = new PageView();
        pageView.setId("test_page");
        pageView.setCount(100);
    }

    @Test
    void testPageViewCreation() {
        assertNotNull(pageView);
        assertEquals("test_page", pageView.getId());
        assertEquals(100, pageView.getCount());
    }

    @Test
    void testPageViewSettersAndGetters() {
        pageView.setId("about_page");
        pageView.setCount(50);

        assertEquals("about_page", pageView.getId());
        assertEquals(50, pageView.getCount());
    }

    @Test
    void testPageViewWithZeroCounter() {
        PageView pv = new PageView();
        pv.setId("new_page");
        pv.setCount(0);

        assertEquals(0, pv.getCount());
    }

    @Test
    void testPageViewWithLargeCounter() {
        PageView pv = new PageView();
        pv.setId("popular_page");
        pv.setCount(1000000);

        assertEquals(1000000, pv.getCount());
    }

    @Test
    void testPageViewIncrement() {
        int initialCounter = pageView.getCount();
        pageView.setCount(initialCounter + 1);

        assertEquals(initialCounter + 1, pageView.getCount());
    }

    @Test
    void testMultiplePageViews() {
        PageView pv1 = new PageView();
        pv1.setId("page1");
        pv1.setCount(10);

        PageView pv2 = new PageView();
        pv2.setId("page2");
        pv2.setCount(20);

        assertNotEquals(pv1.getId(), pv2.getId());
        assertNotEquals(pv1.getCount(), pv2.getCount());
    }

    @Test
    void testPageViewNullId() {
        PageView pv = new PageView();
        pv.setId(null);

        assertNull(pv.getId());
    }

    @Test
    void testPageViewDefaultId() {
        PageView pv = new PageView();
        assertEquals("main_page", pv.getId());
    }

    @Test
    void testPageViewDefaultCount() {
        PageView pv = new PageView();
        assertEquals(0, pv.getCount());
    }
}
