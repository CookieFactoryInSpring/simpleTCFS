package fr.univcotedazur.simpletcfs.customer.interfaces;

import fr.univcotedazur.simpletcfs.cashier.exceptions.PaymentException;
import fr.univcotedazur.simpletcfs.customer.exceptions.CustomerIdNotFoundException;
import fr.univcotedazur.simpletcfs.customer.exceptions.EmptyCartException;
import fr.univcotedazur.simpletcfs.order.entities.Order;

public interface CartProcessor {

    double cartPrice(Long customerId) throws CustomerIdNotFoundException;

    Order validate(Long customerId) throws PaymentException, EmptyCartException, CustomerIdNotFoundException;

}
