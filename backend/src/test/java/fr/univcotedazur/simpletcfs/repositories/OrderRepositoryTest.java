package fr.univcotedazur.simpletcfs.repositories;

import fr.univcotedazur.simpletcfs.entities.*;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;

@DataJpaTest // Only run a test container with the JPA layer (only repositories are up)
// @DataJpaTest is "transactional rollback by default"
class OrderRepositoryTest {

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private OrderRepository orderRepository;

    private Long johnId;
    private Long orderId;

    @BeforeEach
    void setup() {
        Customer john = customerRepository.save(new Customer("john", "1234567890"));
        johnId = john.getId();
        Order createdOrder = new Order(john, new HashSet<>(List.of(new Item(Cookies.CHOCOLALALA, 2))), 20.4, "payReceiptIdOK");
        // The order is normally added to the customer in the Cashier component ->  is normal as it is a business
        // component, and linking the two objects is done only if everything goes well in the business process
        john.addOrder(createdOrder);
        orderRepository.saveAndFlush(createdOrder);
        orderId = createdOrder.getId();
    }

    @Test
    void testEmptyPriceAtOrderCreation() {
        Customer john = customerRepository.findById(johnId).get();
        Order orderWithZeroPrice = new Order(john, new HashSet<>(List.of(new Item(Cookies.CHOCOLALALA, 5))), 0, "payReceiptIdOK");
        john.addOrder(orderWithZeroPrice);
        Assertions.assertThrows(ConstraintViolationException.class, () -> orderRepository.saveAndFlush(orderWithZeroPrice));
        Order orderWithEmptyReceiptId = new Order(john, new HashSet<>(List.of(new Item(Cookies.CHOCOLALALA, 5))), 20.4, "");
        john.addOrder(orderWithEmptyReceiptId);
        Assertions.assertThrows(ConstraintViolationException.class, () -> orderRepository.saveAndFlush(orderWithEmptyReceiptId));
    }

    @Test
    void testOrderCreationWithLazyFetching() {
        Assertions.assertNotNull(orderId);
        Optional<Order> orderToGet = orderRepository.findById(orderId);
        Assertions.assertTrue(orderToGet.isPresent());
        Order foundOrder = orderToGet.get();
        // the order is found by its repository,
        // its items are loaded with it (Eager fetching)
        Assertions.assertEquals(1,foundOrder.getItems().size());
        // the customer attribute is not a collection, it is loaded with the order
        Assertions.assertEquals("john",foundOrder.getCustomer().getName());
        // the orders of the customer are loaded with the customer WITHIN a transaction
        Assertions.assertEquals(1, foundOrder.getCustomer().getOrders().size());
    }

    @Test
    void testCascadingRemove() {
        Optional<Order> orderToGet = orderRepository.findById(orderId);
        Assertions.assertTrue(orderToGet.isPresent());
        Order foundOrder = orderToGet.get();
        Customer customerToBeRemoved = foundOrder.getCustomer();
        customerRepository.delete(customerToBeRemoved);
        customerRepository.flush();
        // get the order again
        orderToGet = orderRepository.findById(orderId);
        Assertions.assertFalse(orderToGet.isPresent());
    }


    @Test
    void testOrderSpELquery() {
        Customer john = customerRepository.findCustomerByName("john").get();
        List<Order> validatedOrders = orderRepository.findOrdersForCustomerWithStatus(john, OrderStatus.VALIDATED,
                Sort.by(Sort.Direction.DESC,"id"));
        Assertions.assertEquals(1,validatedOrders.size());
        Assertions.assertEquals(0, orderRepository.findOrdersForCustomerWithStatus(john, OrderStatus.IN_PROGRESS,
                Sort.by(Sort.Direction.DESC,"id")).size());

        Order retrievedOrder = validatedOrders.get(0);
        Assertions.assertEquals(Cookies.CHOCOLALALA,retrievedOrder.getItems().stream().findFirst().get().getCookie());
        Assertions.assertEquals(2,retrievedOrder.getItems().stream().findFirst().get().getQuantity());
        Assertions.assertEquals(john,retrievedOrder.getCustomer());

        retrievedOrder.setStatus(OrderStatus.IN_PROGRESS);
        Assertions.assertEquals(1, orderRepository.findOrdersForCustomerWithStatus(john, OrderStatus.IN_PROGRESS,
                Sort.by(Sort.Direction.DESC,"id")).size());
    }

    @Test
    void testAllOrdersByPageable() {
        Customer john = customerRepository.findCustomerByName("john").get();
        Order secondOrder = new Order(john, new HashSet<>(List.of(
                new Item(Cookies.CHOCOLALALA,5),
                new Item(Cookies.DARK_TEMPTATION,2))), 48.6, "payReceiptIdOK");
        orderRepository.saveAndFlush(secondOrder);
        // Now the order set contains 2 orders
        int pageNumber = 0; // page numnbering starts at 0...
        int pageSize = 10; // number of items in a page to be returned
        Pageable pageable = PageRequest.of(pageNumber, pageSize);
        Page<Order> pageResult = orderRepository.findAll(pageable);
        Assertions.assertEquals(2, pageResult.getContent().size()); // stream is also available in the page
        Assertions.assertEquals(2, pageResult.getTotalElements());
        Assertions.assertEquals(1, pageResult.getTotalPages());
    }

}
