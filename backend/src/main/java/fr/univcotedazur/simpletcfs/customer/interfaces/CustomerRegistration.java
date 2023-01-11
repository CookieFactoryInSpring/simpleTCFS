package fr.univcotedazur.simpletcfs.customer.interfaces;

import fr.univcotedazur.simpletcfs.customer.entities.Customer;
import fr.univcotedazur.simpletcfs.customer.exceptions.AlreadyExistingCustomerException;

public interface CustomerRegistration {

    Customer register(String name, String creditCard)
            throws AlreadyExistingCustomerException;
}
