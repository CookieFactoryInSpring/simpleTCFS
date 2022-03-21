package fr.univcotedazur.simpletcfs.repositories;

import fr.univcotedazur.simpletcfs.entities.Customer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import javax.validation.ConstraintViolationException;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest // Only run a test container with the JPA layer (only repositories are up)
class CustomerRepositoryTest {

    @Autowired
    CustomerRepository customerRepository;

    @Test
    void testIdGeneration() {
        Customer john = new Customer("john", "1234567890");
        customerRepository.saveAndFlush(john); // save in the persistent context and force saving in the DB (thus ensuring validation by Hibernate)
        Assertions.assertNotNull(john.getId());
    }

    @Test
    void testFindCustomerByName() {
        Customer john = new Customer("john", "1234567890");
        customerRepository.saveAndFlush(john);
        Assertions.assertEquals(customerRepository.findCustomerByName("john").get(),john);
    }

    @Test
    void testBlankName() {
        Assertions.assertThrows(ConstraintViolationException.class, () -> {
            customerRepository.saveAndFlush(new Customer("", "1234567890"));
        });
        Assertions.assertThrows(ConstraintViolationException.class, () -> {
            customerRepository.saveAndFlush(new Customer("    ", "1234567890"));
        });
    }

    @Test
    void testCreditCardPattern() {
        Assertions.assertThrows(ConstraintViolationException.class, () -> {
            customerRepository.saveAndFlush(new Customer("badguy", ""));
        });
        Assertions.assertThrows(ConstraintViolationException.class, () -> {
            customerRepository.saveAndFlush(new Customer("badguy", "creditCard"));
        });
        Assertions.assertThrows(ConstraintViolationException.class, () -> {
            customerRepository.saveAndFlush(new Customer("badguy", "123456789"));
        });
    }
}