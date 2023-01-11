package fr.univcotedazur.simpletcfs.customer.repositories;

import fr.univcotedazur.simpletcfs.customer.entities.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {

    Optional<Customer> findCustomerByName(String name);

}
