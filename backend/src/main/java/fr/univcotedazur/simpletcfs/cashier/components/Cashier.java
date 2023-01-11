package fr.univcotedazur.simpletcfs.cashier.components;

import fr.univcotedazur.simpletcfs.cashier.connectors.interfaces.Bank;
import fr.univcotedazur.simpletcfs.cashier.exceptions.PaymentException;
import fr.univcotedazur.simpletcfs.cashier.interfaces.Payment;
import fr.univcotedazur.simpletcfs.customer.entities.Customer;
import fr.univcotedazur.simpletcfs.kitchen.interfaces.OrderCooking;
import fr.univcotedazur.simpletcfs.order.entities.Order;
import fr.univcotedazur.simpletcfs.order.interfaces.OrderCreator;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Component
public class Cashier implements Payment {

    private final Bank bank;

    private final OrderCreator orderer;

    private final OrderCooking kitchen;

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
