package fr.univcotedazur.simpletcfs.components;

import fr.univcotedazur.simpletcfs.CustomerFinder;
import fr.univcotedazur.simpletcfs.CustomerRegistration;
import fr.univcotedazur.simpletcfs.entities.Customer;
import fr.univcotedazur.simpletcfs.exceptions.AlreadyExistingCustomerException;
import fr.univcotedazur.simpletcfs.repositories.CustomerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.validation.ConstraintViolationException;
import java.util.Optional;

@Component
public class CustomerRegistry implements CustomerRegistration, CustomerFinder {

    @Autowired
    private CustomerRepository customerRepository;

    @Override
    @Transactional
    public Customer register(String name, String creditCard)
            throws AlreadyExistingCustomerException, ConstraintViolationException {
        if(findByName(name).isPresent()) {
            throw new AlreadyExistingCustomerException(name);
        }
        Customer newcustomer = new Customer(name, creditCard);
        return customerRepository.save(newcustomer);
    }

    @Override
    public Optional<Customer> findByName(String name) {
        return customerRepository.findCustomerByName(name);
    }

    @Override
    public Optional<Customer> findById(Long id) {
        return customerRepository.findById(id);
    }

}
