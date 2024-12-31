package fr.univcotedazur.simpletcfs.repositories;

import fr.univcotedazur.simpletcfs.entities.Customer;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;

@DataJpaTest // Only run a test container with the JPA layer (only repositories are up)
// @DataJpaTest is "transactional rollback by default
class CustomerRepositoryTest {

    @Autowired
    private CustomerRepository customerRepository;

    @Test
    void testIdGenerationAndUnicity() {
        Customer john = new Customer("john", "1234567890");
        Assertions.assertNull(john.getId());
        customerRepository.saveAndFlush(john); // save in the persistent context and force saving in the DB (thus ensuring validation by Hibernate)
        Assertions.assertNotNull(john.getId());
        Assertions.assertThrows(DataIntegrityViolationException.class, () -> customerRepository.saveAndFlush(new Customer("john", "1234567890")));
        Assertions.assertThrows(DataIntegrityViolationException.class, () -> customerRepository.saveAndFlush(new Customer("john", "12345678902")));
    }

    @Test
    void testFindCustomerByName() {
        Customer john = new Customer("john", "1234567890");
        customerRepository.saveAndFlush(john);
        Assertions.assertEquals(customerRepository.findCustomerByName("john").get(),john);
    }

    @Test
    void testBlankName() {
        Assertions.assertThrows(ConstraintViolationException.class, () -> customerRepository.saveAndFlush(new Customer("", "1234567890")));
        Assertions.assertThrows(ConstraintViolationException.class, () -> customerRepository.saveAndFlush(new Customer("    ", "1234567890")));
    }

    @Test
    void testCreditCardPattern() {
        Assertions.assertThrows(ConstraintViolationException.class, () -> customerRepository.saveAndFlush(new Customer("badguy", "")));
        Assertions.assertThrows(ConstraintViolationException.class, () -> customerRepository.saveAndFlush(new Customer("badguy", "creditCard")));
        Assertions.assertThrows(ConstraintViolationException.class, () -> customerRepository.saveAndFlush(new Customer("badguy", "123456789")));
    }
}
