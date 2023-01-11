package fr.univcotedazur.simpletcfs.kitchen.components;

import fr.univcotedazur.simpletcfs.customer.entities.Cookies;
import fr.univcotedazur.simpletcfs.customer.entities.Customer;
import fr.univcotedazur.simpletcfs.customer.entities.Item;
import fr.univcotedazur.simpletcfs.customer.interfaces.CustomerRegistration;
import fr.univcotedazur.simpletcfs.order.entities.Order;
import fr.univcotedazur.simpletcfs.order.entities.OrderStatus;
import fr.univcotedazur.simpletcfs.order.interfaces.OrderFinder;
import fr.univcotedazur.simpletcfs.order.repositories.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@Transactional
class KitchenTest {

    @Autowired
    private CustomerRegistration registry;

    @Autowired
    private OrderFinder orderFinder;

    @Autowired
    private Kitchen kitchen;

    @Autowired
    private OrderRepository orderRepository;

    private Order order;

    @BeforeEach
    void setUpContext() throws Exception {
        Set<Item> items = new HashSet<>();
        items.add(new Item(Cookies.CHOCOLALALA, 3));
        items.add(new Item(Cookies.DARK_TEMPTATION, 2));
        Customer john = registry.register("john", "1234-896983");
        Order newOrder = new Order(john, items, (3 * Cookies.CHOCOLALALA.getPrice()) + (2 * Cookies.DARK_TEMPTATION.getPrice()), "payReceiptIdOK");
        order = orderRepository.save(newOrder);
        john.addOrder(order);
    }

    @Test
    void processCommand() throws Exception {
        assertEquals(OrderStatus.VALIDATED, order.getStatus());
        kitchen.processInKitchen(order);
        assertEquals(OrderStatus.IN_PROGRESS, orderFinder.retrieveOrderStatus(order.getId()));
    }

}
