package fr.univcotedazur.simpletcfs.interfaces;

import fr.univcotedazur.simpletcfs.entities.Order;
import fr.univcotedazur.simpletcfs.exceptions.CustomerIdNotFoundException;
import fr.univcotedazur.simpletcfs.exceptions.EmptyCartException;
import fr.univcotedazur.simpletcfs.exceptions.PaymentException;

public interface CartProcessor {

    double cartPrice(Long customerId) throws CustomerIdNotFoundException;

    Order validate(Long customerId) throws PaymentException, EmptyCartException, CustomerIdNotFoundException;

}
