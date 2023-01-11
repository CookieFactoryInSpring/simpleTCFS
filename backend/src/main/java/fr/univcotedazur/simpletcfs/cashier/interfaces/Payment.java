package fr.univcotedazur.simpletcfs.cashier.interfaces;

import fr.univcotedazur.simpletcfs.cashier.exceptions.PaymentException;
import fr.univcotedazur.simpletcfs.customer.entities.Customer;
import fr.univcotedazur.simpletcfs.order.entities.Order;

public interface Payment {

    Order payOrderFromCart(Customer customer, double price) throws PaymentException;

}
