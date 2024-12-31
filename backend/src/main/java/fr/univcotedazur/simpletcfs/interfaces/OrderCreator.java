package fr.univcotedazur.simpletcfs.interfaces;

import fr.univcotedazur.simpletcfs.entities.Customer;
import fr.univcotedazur.simpletcfs.entities.Order;


public interface OrderCreator {

        Order createOrder(Customer customer, double price, String payReceiptId);

}
