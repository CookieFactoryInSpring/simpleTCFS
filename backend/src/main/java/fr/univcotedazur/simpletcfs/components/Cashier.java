package fr.univcotedazur.simpletcfs.components;

import fr.univcotedazur.simpletcfs.Bank;
import fr.univcotedazur.simpletcfs.OrderProcessing;
import fr.univcotedazur.simpletcfs.Payment;
import fr.univcotedazur.simpletcfs.entities.Customer;
import fr.univcotedazur.simpletcfs.entities.Item;
import fr.univcotedazur.simpletcfs.entities.Order;
import fr.univcotedazur.simpletcfs.exceptions.PaymentException;
import fr.univcotedazur.simpletcfs.repositories.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;

@Component
@Transactional
public class Cashier implements Payment {

    @Autowired
    private Bank bankProxy;

    @Autowired
    private OrderProcessing kitchen;

    @Autowired
    private OrderRepository orderRepository;

    @Override
    public Order payOrder(Customer customer, Set<Item> items) throws PaymentException {

        Order order = new Order(customer, new HashSet<>(items));
        double price = order.getPrice();

        boolean status = false;
        status = bankProxy.pay(customer, price);
        if (!status) {
            throw new PaymentException(customer.getName(), price);
        }

        customer.add(order);
        order = orderRepository.save(order);
        kitchen.process(order);

        return order;
    }

}
