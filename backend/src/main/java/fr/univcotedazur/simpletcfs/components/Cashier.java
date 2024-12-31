package fr.univcotedazur.simpletcfs.components;

import fr.univcotedazur.simpletcfs.entities.Customer;
import fr.univcotedazur.simpletcfs.entities.Order;
import fr.univcotedazur.simpletcfs.exceptions.PaymentException;
import fr.univcotedazur.simpletcfs.interfaces.Bank;
import fr.univcotedazur.simpletcfs.interfaces.OrderCooking;
import fr.univcotedazur.simpletcfs.interfaces.OrderCreator;
import fr.univcotedazur.simpletcfs.interfaces.Payment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Component
public class Cashier implements Payment {

    private final Bank bank;

    private final OrderCreator orderer;

    private final OrderCooking kitchen;

    @Autowired
    public Cashier(Bank bank, OrderCreator orderer, OrderCooking orderCooking) {
        this.bank = bank;
        this.orderer = orderer;
        this.kitchen = orderCooking;
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public Order payOrderFromCart(Customer customer, double price) throws PaymentException {
        String paymentReceiptId  = bank.pay(customer, price).orElseThrow(() -> new PaymentException(customer.getName(), price));
        Order order = orderer.createOrder(customer, price, paymentReceiptId);
        return kitchen.processInKitchen(order);
    }

}
