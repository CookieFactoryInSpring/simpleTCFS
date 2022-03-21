package fr.univcotedazur.simpletcfs;

import fr.univcotedazur.simpletcfs.entities.Customer;

import java.util.Optional;

public interface CustomerFinder {

    Optional<Customer> findByName(String name);

    Optional<Customer> findById(Long id);

}
