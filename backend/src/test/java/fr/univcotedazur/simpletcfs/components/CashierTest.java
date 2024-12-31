package fr.univcotedazur.simpletcfs.components;

import fr.univcotedazur.simpletcfs.entities.*;
import fr.univcotedazur.simpletcfs.exceptions.PaymentException;
import fr.univcotedazur.simpletcfs.interfaces.Bank;
import fr.univcotedazur.simpletcfs.interfaces.Payment;
import fr.univcotedazur.simpletcfs.repositories.CustomerRepository;
import fr.univcotedazur.simpletcfs.repositories.OrderRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Commit;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@SpringBootTest
@Transactional // default behavior : rollback DB operations after each test (even if it fails)
@Commit // test-specific annotation to change default behaviour to Commit on all tests (could be applied on a method
        // This annotation obliges us to clean the DB (removing the 2 customers) but it is only here for illustration
        // The "rollback" policy should be privileged unless some specific testing context appears
class CashierTest {

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private Payment cashier;

    @MockitoBean
    private Bank bankMock;

    // Test context
    private Set<Item> items;
    private Customer john;
    private Customer pat;

    @BeforeEach
    void setUpContext() {
        items = new HashSet<>();
        items.add(new Item(Cookies.CHOCOLALALA, 3));
        items.add(new Item(Cookies.DARK_TEMPTATION, 2));
        // Customers
        john = new Customer("john", "1234896983");  // ends with the secret YES Card number
        john.setCart(items);
        customerRepository.save(john);
        pat  = new Customer("pat", "1234567890");   // should be rejected by the payment service
        pat.setCart(items);
        customerRepository.save(pat);
        // Mocking the bank proxy
        when(bankMock.pay(eq(john),  anyDouble())).thenReturn(Optional.of("playReceiptOKId"));
        when(bankMock.pay(eq(pat),  anyDouble())).thenReturn(Optional.empty());
    }

    @AfterEach
    void cleanUpContext() {
        orderRepository.deleteAll();
        customerRepository.deleteAll();
    }

    @Test
    void processToPayment() throws Exception {
        double price = (3 * Cookies.CHOCOLALALA.getPrice()) + (2 * Cookies.DARK_TEMPTATION.getPrice());
        // paying order
        Order order = cashier.payOrderFromCart(john, price);
        assertNotNull(order);
        assertEquals(john, order.getCustomer());
        assertEquals(items, order.getItems());
        assertEquals(price, order.getPrice(), 0.0);
        assertEquals(2,order.getItems().size());
        assertEquals(OrderStatus.IN_PROGRESS, order.getStatus());
        Set<Order> johnOrders = john.getOrders();
        assertEquals(1, johnOrders.size());
        assertEquals(order, johnOrders.iterator().next());
    }

    @Test
    void identifyPaymentError() {
        Assertions.assertThrows( PaymentException.class, () -> cashier.payOrderFromCart(pat, 44.2));
    }

}
