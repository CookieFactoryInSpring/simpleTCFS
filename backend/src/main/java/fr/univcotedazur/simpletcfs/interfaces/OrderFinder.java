package fr.univcotedazur.simpletcfs.interfaces;

import fr.univcotedazur.simpletcfs.entities.Order;
import fr.univcotedazur.simpletcfs.entities.OrderStatus;
import fr.univcotedazur.simpletcfs.exceptions.OrderIdNotFoundException;

import java.util.List;
import java.util.Optional;

public interface OrderFinder {

    Optional<Order> findById(Long id);

    List<Order> findAll();

    Order retrieveOrder(Long orderId) throws OrderIdNotFoundException;

    OrderStatus retrieveOrderStatus(Long orderId) throws OrderIdNotFoundException;

}