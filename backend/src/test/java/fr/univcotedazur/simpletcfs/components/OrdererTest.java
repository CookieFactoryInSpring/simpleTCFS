package fr.univcotedazur.simpletcfs.components;

import fr.univcotedazur.simpletcfs.entities.*;
import fr.univcotedazur.simpletcfs.exceptions.OrderIdNotFoundException;
import fr.univcotedazur.simpletcfs.interfaces.CustomerRegistration;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Transactional
class OrdererTest {

    @Autowired
    private CustomerRegistration registry;

    @Autowired
    private Orderer orderer;

    private Long orderId;

    @BeforeEach
    void setUpContext() throws Exception {
        Set<Item> items = new HashSet<>();
        items.add(new Item(Cookies.CHOCOLALALA, 3));
        items.add(new Item(Cookies.DARK_TEMPTATION, 2));
        Customer john = registry.register("john", "1234896983");
        john.setCart(items);
        orderId = orderer.createOrder(john, (3 * Cookies.CHOCOLALALA.getPrice()) + (2 * Cookies.DARK_TEMPTATION.getPrice()), "payReceiptIdOK").getId();
    }

    @Test
    void orderFinding() {
        assertTrue(orderer.findById(orderId).isPresent());
        assertTrue(orderer.findById(324L).isEmpty());
        Assertions.assertThrows(OrderIdNotFoundException.class, () -> orderer.retrieveOrder(324L));
    }

    @Test
    void orderCreation() throws Exception {
        Order order = orderer.retrieveOrder(orderId);
        Customer john = order.getCustomer();
        assertEquals(orderId, order.getId());
        assertEquals(OrderStatus.VALIDATED, order.getStatus());
        assertEquals(2, order.getItems().size());
        assertEquals(3, order.getItems().stream().filter(item -> item.getCookie().equals(Cookies.CHOCOLALALA)).findFirst().get().getQuantity());
        assertEquals(2, order.getItems().stream().filter(item -> item.getCookie().equals(Cookies.DARK_TEMPTATION)).findFirst().get().getQuantity());
        assertEquals(3 * Cookies.CHOCOLALALA.getPrice() + 2 * Cookies.DARK_TEMPTATION.getPrice(), order.getPrice());
        assertEquals("payReceiptIdOK", order.getPayReceiptId());
        assertEquals("john", john.getName());
        // John's cart is not empty, we can reuse it to create another order
        orderer.createOrder(john, 12, "payReceiptIdOK");
        assertEquals(2, orderer.findAll().size());
    }

    @Test
    void orderModifier() throws Exception {
        Order order = orderer.retrieveOrder(orderId);
        assertEquals(OrderStatus.VALIDATED, order.getStatus());
        orderer.orderIsNowInProgress(order);
        assertEquals(OrderStatus.IN_PROGRESS, order.getStatus());
        orderer.orderIsNowReady(order);
        assertEquals(OrderStatus.READY, order.getStatus());
    }

}
