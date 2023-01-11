package fr.univcotedazur.simpletcfs.order.interfaces;

import fr.univcotedazur.simpletcfs.customer.entities.Customer;
import fr.univcotedazur.simpletcfs.order.entities.Order;


public interface OrderCreator {

        Order createOrder(Customer customer, double price, String payReceiptId);

}
