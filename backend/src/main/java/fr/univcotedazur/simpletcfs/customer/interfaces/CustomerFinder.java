package fr.univcotedazur.simpletcfs.customer.interfaces;

import fr.univcotedazur.simpletcfs.customer.entities.Customer;
import fr.univcotedazur.simpletcfs.customer.exceptions.CustomerIdNotFoundException;

import java.util.List;
import java.util.Optional;

public interface CustomerFinder {

    Optional<Customer> findByName(String name);

    Optional<Customer> findById(Long id);

    Customer retrieveCustomer(Long customerId) throws CustomerIdNotFoundException;

    List<Customer> findAll();

}
