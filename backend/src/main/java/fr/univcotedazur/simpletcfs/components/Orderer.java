package fr.univcotedazur.simpletcfs.components;

import fr.univcotedazur.simpletcfs.entities.Customer;
import fr.univcotedazur.simpletcfs.entities.Order;
import fr.univcotedazur.simpletcfs.entities.OrderStatus;
import fr.univcotedazur.simpletcfs.exceptions.OrderIdNotFoundException;
import fr.univcotedazur.simpletcfs.interfaces.OrderCreator;
import fr.univcotedazur.simpletcfs.interfaces.OrderFinder;
import fr.univcotedazur.simpletcfs.interfaces.OrderModifier;
import fr.univcotedazur.simpletcfs.repositories.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class Orderer implements OrderCreator, OrderFinder, OrderModifier {

    private final OrderRepository orderRepository;

    @Autowired
    public Orderer(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY) // must be called within a transaction
    public Order createOrder(Customer customer, double price, String payReceiptId) {
        return orderRepository.save(new Order(customer, customer.getCart(), price, payReceiptId));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Order> findById(Long id) {
        return orderRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Order> findAll() {
        return orderRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Order retrieveOrder(Long orderId) throws OrderIdNotFoundException {
        return findById(orderId).orElseThrow(() -> new OrderIdNotFoundException(orderId));
    }

    @Override
    @Transactional(readOnly = true)
    public OrderStatus retrieveOrderStatus(Long orderId) throws OrderIdNotFoundException {
        return retrieveOrder(orderId).getStatus();
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public Order orderIsNowInProgress(Order order) {
        order.setStatus(OrderStatus.IN_PROGRESS);
        return order;
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public Order orderIsNowReady(Order order) {
        order.setStatus(OrderStatus.READY);
        return order;
    }


}
