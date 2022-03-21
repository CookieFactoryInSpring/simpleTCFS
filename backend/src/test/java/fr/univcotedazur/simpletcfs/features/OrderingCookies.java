package fr.univcotedazur.simpletcfs.features;

import fr.univcotedazur.simpletcfs.*;
import fr.univcotedazur.simpletcfs.entities.*;
import fr.univcotedazur.simpletcfs.exceptions.AlreadyExistingCustomerException;
import fr.univcotedazur.simpletcfs.exceptions.EmptyCartException;
import fr.univcotedazur.simpletcfs.exceptions.NegativeQuantityException;
import fr.univcotedazur.simpletcfs.exceptions.PaymentException;
import fr.univcotedazur.simpletcfs.repositories.CustomerRepository;
import fr.univcotedazur.simpletcfs.repositories.OrderRepository;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.transaction.annotation.Transactional;


import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.Mockito.when;

@CucumberContextConfiguration
@SpringBootTest
@Transactional // Make each step "transactional" but no rollback by default! (@Rollback does not work)
public class OrderingCookies {

    @Autowired
    private CartModifier cart;

    @Autowired
    private CartProcessor processor;

    @Autowired
    private CustomerRegistration registry;

    @Autowired
    private CustomerFinder finder;

    @Autowired // Bug in the Cucumber/Mockito/Spring coordination: needs to add @Autowired
    @MockBean
    private Bank bankMock;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private OrderRepository orderRepository;

    private String customerName;
    private Long orderId;

    @Before
    public void settingUpContext() throws PaymentException {
        when(bankMock.pay(any(Customer.class), anyDouble())).thenReturn(true);
    }

    @After // Necessary as no rollback can be automated on Cucumber steps
    public void cleaningUpContext() {
        customerRepository.deleteAll();
        orderRepository.deleteAll();
    }

    @Given("a customer named {string} with credit card {string}")
    public void aCustomerNamedWithCreditCard(String customerName, String creditCard) throws AlreadyExistingCustomerException {
        registry.register(customerName, creditCard);
    }

    @When("{string} asks for his cart contents")
    public void customerAsksForHisCartContents(String customerName) {
        this.customerName = customerName;
    }

    @Then("^there (?:is|are) (\\d+) items? inside the cart$") // Regular Expressions, not Cucumber expression
    // Note that you cannot mix Cucumber expression such as {int} with regular expressions
    public void thereAreItemsInsideTheCart(int nbItems) {
        Customer customer = finder.findByName(customerName).get();
        assertEquals(nbItems, customer.getCart().size());
    }

    @When("{string} orders {int} x {string}")
    public void customerOrders(String customerName, int howMany, String recipe) throws NegativeQuantityException {
        this.customerName = customerName;
        Customer customer = finder.findByName(customerName).get();
        Cookies cookie = Cookies.valueOf(recipe);
        cart.update(customer, new Item(cookie, howMany));
    }

    @And("the cart contains the following item: {int} x {string}")
    public void theCartContainsTheFollowingItem(int howMany, String recipe) {
        Customer customer = finder.findByName(customerName).get();
        Item expected = new Item(Cookies.valueOf(recipe), howMany);
        assertTrue(customer.getCart().contains(expected));
    }

    @And("{string} decides not to buy {int} x {string}")
    public void customerDecidesNotToBuy(String customerName, int howMany, String recipe) throws NegativeQuantityException {
        Customer customer = finder.findByName(customerName).get();
        Cookies cookie = Cookies.valueOf(recipe);
        cart.update(customer, new Item(cookie, -howMany));
    }

    @Then("the price of {string}'s cart is equals to {double}")
    public void thePriceOfSebSCartIsEqualsTo(String customerName, double expectedPrice) {
        Customer customer = finder.findByName(customerName).get();
        assertEquals(expectedPrice, processor.price(customer), 0.01);
    }

    @And("{string} validates the cart and pays through the bank")
    public void validatesTheCart(String customerName) throws EmptyCartException, PaymentException {
        Customer customer = finder.findByName(customerName).get();
        orderId = processor.validate(customer).getId();
    }

    @Then("the order amount is equals to {double}")
    public void theOrderAmountIsEqualsTo(double expectedPrice) {
        Order order = orderRepository.getById(orderId); // This shows that some business component handling Orders is missing
        assertEquals(expectedPrice, order.getPrice(), 0.01);
    }

    @Then("the order status is {string}")
    public void theOrderStatusIs(String state) {
        Order order = orderRepository.getById(orderId);
        assertEquals(OrderStatus.valueOf(state),order.getStatus());
    }

}
