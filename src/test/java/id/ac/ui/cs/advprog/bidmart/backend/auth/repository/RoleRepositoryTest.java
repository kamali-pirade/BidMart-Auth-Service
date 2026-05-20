package id.ac.ui.cs.advprog.bidmart.backend.auth.repository;

import id.ac.ui.cs.advprog.bidmart.backend.auth.entity.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class RoleRepositoryTest {

    @Autowired
    private RoleRepository roleRepository;

    private Role role;
    private UUID roleId;

    @BeforeEach
    void setUp() {
        roleId = UUID.randomUUID();
        role = new Role();
        role.setName("BUYER");
        role.setPermissions("read,write");
    }

    @Test
    void testSaveAndRetrieveRole() {
        roleRepository.save(role);

        Optional<Role> found = roleRepository.findById(role.getId());
        assertTrue(found.isPresent());
        assertEquals("BUYER", found.get().getName());
    }

    @Test
    void testFindByNameIgnoreCase() {
        roleRepository.save(role);

        Optional<Role> found = roleRepository.findByNameIgnoreCase("buyer");
        assertTrue(found.isPresent());
        assertEquals("BUYER", found.get().getName());
    }

    @Test
    void testFindByNameIgnoreCaseUpperCase() {
        roleRepository.save(role);

        Optional<Role> found = roleRepository.findByNameIgnoreCase("BUYER");
        assertTrue(found.isPresent());
    }

    @Test
    void testFindByNameIgnoreCaseMixedCase() {
        roleRepository.save(role);

        Optional<Role> found = roleRepository.findByNameIgnoreCase("BuYeR");
        assertTrue(found.isPresent());
    }

    @Test
    void testFindByNameNotFound() {
        Optional<Role> found = roleRepository.findByNameIgnoreCase("NONEXISTENT");
        assertFalse(found.isPresent());
    }

    @Test
    void testDeleteRole() {
        roleRepository.save(role);
        UUID savedId = role.getId();
        assertTrue(roleRepository.existsById(savedId));

        roleRepository.deleteById(savedId);
        assertFalse(roleRepository.existsById(savedId));
    }

    @Test
    void testMultipleRoles() {
        Role role2 = new Role();
        role2.setName("SELLER");
        role2.setPermissions("read,write,delete");

        roleRepository.save(role);
        roleRepository.save(role2);

        Optional<Role> foundBuyer = roleRepository.findByNameIgnoreCase("BUYER");
        Optional<Role> foundSeller = roleRepository.findByNameIgnoreCase("SELLER");

        assertTrue(foundBuyer.isPresent());
        assertTrue(foundSeller.isPresent());
        assertEquals("BUYER", foundBuyer.get().getName());
        assertEquals("SELLER", foundSeller.get().getName());
    }
}
