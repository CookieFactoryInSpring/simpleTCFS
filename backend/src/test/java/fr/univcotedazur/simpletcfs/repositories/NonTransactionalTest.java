package fr.univcotedazur.simpletcfs.repositories;

import fr.univcotedazur.simpletcfs.entities.Cookies;
import fr.univcotedazur.simpletcfs.entities.Customer;
import fr.univcotedazur.simpletcfs.entities.Item;
import fr.univcotedazur.simpletcfs.entities.Order;
import fr.univcotedazur.simpletcfs.interfaces.Bank;
import fr.univcotedazur.simpletcfs.interfaces.OrderModifier;
import fr.univcotedazur.simpletcfs.interfaces.Payment;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class NonTransactionalTest { // To show errors that happen when we are not in a transaction

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private Payment payment;

    @Autowired
    private OrderModifier orderModifier;

    @Autowired
    private Bank bank;

    private Long orderId;

    @BeforeEach
    @Transactional // setup and tearDown are transactional to make the initialization and cleaning right
    void setup() {
        Customer john = new Customer("john", "1234567890");
        Customer savedJohn= customerRepository.save(john);
        assertEquals(0, savedJohn.getOrders().size());
        Order createdOrder = new Order(savedJohn, new HashSet<>(Arrays.asList(new Item(Cookies.CHOCOLALALA,2))), 20.4, "payReceiptIdOK");
        // The order is normally added to the customer in the Cashier component ->  is normal as it is a business
        // component, and linking the two objects is done only if everything goes well in the business process
        savedJohn.addOrder(createdOrder);
        orderId = orderRepository.save(createdOrder).getId();
    }

    @AfterEach
    @Transactional
    public void cleaningUp()  {
        customerRepository.deleteAll();
        orderRepository.deleteAll();
    }

    @Test // THIS METHOD IS NOT TRANSACTIONAL
    void testExceptionWithLazyFetching() {
        Assertions.assertNotNull(orderId);
        Optional<Order> orderToGet = orderRepository.findById(orderId);
        assertTrue(orderToGet.isPresent());
        Order foundOrder = orderToGet.get();
        // the order is found by its repository,
        // the customer attribute is not a collection, it is loaded with the order
        assertEquals("john",foundOrder.getCustomer().getName());
        // its items are loaded when we access them IN A TRANSACTION (@ElementCollection on items is lazy)
        // BUT WE ARE NOT INSIDE A TRANSACTION
        Assertions.assertThrows( org.hibernate.LazyInitializationException.class, () -> {
            assertEquals(1, foundOrder.getItems().size());
        });
    }

    @Test // THIS METHOD IS NOT TRANSACTIONAL
    void testMandatoryTransaction() {
        Customer johnOutOfTransaction = customerRepository.findCustomerByName("john").get();
        Assertions.assertThrows( org.springframework.transaction.IllegalTransactionStateException.class, () -> {
            payment.payOrderFromCart(johnOutOfTransaction, 20.4);
        });
        Assertions.assertThrows( org.springframework.transaction.IllegalTransactionStateException.class, () -> {
            bank.pay(johnOutOfTransaction, 20.4);
        });
        Order orderOutoOfTransaction = orderRepository.findById(orderId).get();
        Assertions.assertThrows( org.springframework.transaction.IllegalTransactionStateException.class, () -> {
            orderModifier.orderIsNowInProgress(orderOutoOfTransaction);
        });
    }


}
