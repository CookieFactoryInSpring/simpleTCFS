package fr.univcotedazur.simpletcfs.cashier.connectors.interfaces;

import fr.univcotedazur.simpletcfs.customer.entities.Customer;

import java.util.Optional;

public interface Bank {

    Optional<String> pay(Customer customer, double value);
}
