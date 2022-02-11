package fr.univcotedazur.simpletcfs.components;

import fr.univcotedazur.simpletcfs.Bank;
import fr.univcotedazur.simpletcfs.Payment;
import fr.univcotedazur.simpletcfs.entities.Cookies;
import fr.univcotedazur.simpletcfs.entities.Customer;
import fr.univcotedazur.simpletcfs.entities.Item;
import fr.univcotedazur.simpletcfs.entities.Order;
import fr.univcotedazur.simpletcfs.exceptions.PaymentException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Matchers.anyDouble;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

@SpringBootTest
class CashierTest {

    @Autowired
    private InMemoryDatabase memory;

    @Autowired
    private Payment cashier;

    @MockBean
    private Bank bankMock;

    // Test context
    private Set<Item> items;
    Customer john;
    Customer pat;

    @BeforeEach
    public void setUpContext() throws Exception {
        memory.flush();
        items = new HashSet<>();
        items.add(new Item(Cookies.CHOCOLALALA, 3));
        items.add(new Item(Cookies.DARK_TEMPTATION, 2));
        // Customers
        john = new Customer("john", "1234-896983");  // ends with the secret YES Card number
        pat  = new Customer("pat", "1234-567890");   // should be rejected by the payment service
        // Mocking the bank proxy
        when(bankMock.pay(eq(john), anyDouble())).thenReturn(true);
        when(bankMock.pay(eq(pat),  anyDouble())).thenReturn(false);
    }

    @Test
    public void processToPayment() throws Exception {
        // paying order
        Order order = cashier.payOrder(john, items);
        assertNotNull(order);
        assertEquals(john, order.getCustomer());
        assertEquals(items, order.getItems());
        double price = (3 * Cookies.CHOCOLALALA.getPrice()) + (2 * Cookies.DARK_TEMPTATION.getPrice());
        assertEquals(price, order.getPrice(), 0.0);
        assertEquals(2,order.getItems().size());
        assertEquals(1, john.getOrders().size());
        assertEquals(order, john.getOrders().iterator().next());
    }

    @Test
    public void identifyPaymentError() {
        Assertions.assertThrows( PaymentException.class, () -> {
            cashier.payOrder(pat, items);
        });
    }
}