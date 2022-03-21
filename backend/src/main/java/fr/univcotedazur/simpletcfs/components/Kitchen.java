package fr.univcotedazur.simpletcfs.components;

import fr.univcotedazur.simpletcfs.OrderProcessing;
import fr.univcotedazur.simpletcfs.Tracker;
import fr.univcotedazur.simpletcfs.entities.Order;
import fr.univcotedazur.simpletcfs.entities.OrderStatus;
import fr.univcotedazur.simpletcfs.exceptions.UnknownOrderId;
import fr.univcotedazur.simpletcfs.repositories.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Component
@Transactional
public class Kitchen implements OrderProcessing, Tracker {

    @Autowired
    private OrderRepository orderRepository;

    @Override
    public void process(Order order) {
        order.setStatus(OrderStatus.IN_PROGRESS);
        orderRepository.save(order);
    }

    @Override
    public OrderStatus retrieveStatus(long orderId) throws UnknownOrderId {
        Optional<Order> order = orderRepository.findById(orderId);
        if (order.isEmpty())
            throw new UnknownOrderId(orderId);
        return  order.get().getStatus();
    }

}
