package fr.univcotedazur.simpletcfs.components;

import fr.univcotedazur.simpletcfs.CustomerRegistration;
import fr.univcotedazur.simpletcfs.OrderProcessing;
import fr.univcotedazur.simpletcfs.Tracker;
import fr.univcotedazur.simpletcfs.entities.*;
import fr.univcotedazur.simpletcfs.exceptions.AlreadyExistingCustomerException;
import fr.univcotedazur.simpletcfs.repositories.CustomerRepository;
import fr.univcotedazur.simpletcfs.repositories.OrderRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class KitchenTest {

    @Autowired
    private CustomerRegistration registry;

    @Autowired
    private OrderProcessing processor;

    @Autowired
    private Tracker tracker;

    @Autowired
    CustomerRepository customerRepository;

    @Autowired
    OrderRepository orderRepository;

    private Set<Item> items;
    private Customer pat;
    private Order inProgress;

    @BeforeEach
    public void setUpContext() throws AlreadyExistingCustomerException {
        items = new HashSet<>();
        items.add(new Item(Cookies.CHOCOLALALA, 3));
        items.add(new Item(Cookies.DARK_TEMPTATION, 2));
        pat = registry.register("pat", "1234567890");
    }

    @AfterEach
    public void cleaningUp()  {
        Optional<Customer> customerToDispose = customerRepository.findCustomerByName("pat");
        if (customerToDispose.isPresent()) {
            customerRepository.delete(customerToDispose.get());
        }
        pat = null;
        orderRepository.deleteAll(); // easier way to wipe a repo out
    }

    @Test
    void processCommand() throws Exception {
        Order inProgress = new Order(pat, items);
        processor.process(inProgress);
        assertEquals(OrderStatus.IN_PROGRESS, tracker.retrieveStatus(inProgress.getId()));
    }

}