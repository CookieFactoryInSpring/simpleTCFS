package fr.univcotedazur.simpletcfs.cucumber.ordering;

import fr.univcotedazur.simpletcfs.entities.*;
import fr.univcotedazur.simpletcfs.exceptions.*;
import fr.univcotedazur.simpletcfs.interfaces.*;
import fr.univcotedazur.simpletcfs.repositories.CustomerRepository;
import fr.univcotedazur.simpletcfs.repositories.OrderRepository;
import io.cucumber.java.Before;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.Mockito.when;

@Transactional
public class OrderingCookies {

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private CartModifier cartModifier;

    @Autowired
    private CartProcessor cartProcessor;

    @Autowired
    private CustomerRegistration customerRegistration;

    @Autowired
    private CustomerFinder customerFinder;

    @Autowired // Spring/Cucumber bug workaround: autowired the mock declared in the Config class
    private Bank bankMock;

    private Long customerId;
    private Long orderId;

    @Before
    public void settingUpContext() throws PaymentException {
        customerRepository.deleteAll();
        orderRepository.deleteAll();
        when(bankMock.pay(any(Customer.class), anyDouble())).thenReturn(Optional.of("payReceiptIdOK"));
    }

    @Given("a customer named {string} with credit card {string}")
    public void aCustomerNamedWithCreditCard(String customerName, String creditCard) throws AlreadyExistingCustomerException {
        customerRegistration.register(customerName, creditCard);
    }

    @When("{string} asks for his cart contents")
    public void customerAsksForHisCartContents(String customerName) {
            this.customerId = customerFinder.findByName(customerName).get().getId();
    }

    @Then("^there (?:is|are) (\\d+) items? inside the cart$") // Regular Expressions, not Cucumber expression
    // Note that you cannot mix Cucumber expression such as {int} with regular expressions
    public void thereAreItemsInsideTheCart(int nbItems) throws CustomerIdNotFoundException {
        assertEquals(nbItems, cartModifier.cartContent(customerId).size());
    }

    @When("{string} orders {int} x {string}")
    public void customerOrders(String customerName, int howMany, String recipe) throws NegativeQuantityException, CustomerIdNotFoundException {
        this.customerId = customerFinder.findByName(customerName).get().getId();
        Cookies cookie = Cookies.valueOf(recipe);
        cartModifier.update(customerId, new Item(cookie, howMany));
    }

    @And("the cart contains the following item: {int} x {string}")
    public void theCartContainsTheFollowingItem(int howMany, String recipe) throws CustomerIdNotFoundException {
        Item expected = new Item(Cookies.valueOf(recipe), howMany);
        assertTrue(cartModifier.cartContent(this.customerId).contains(expected));
    }

    @And("{string} decides not to buy {int} x {string}")
    public void customerDecidesNotToBuy(String customerName, int howMany, String recipe) throws NegativeQuantityException, CustomerIdNotFoundException {
        this.customerId = customerFinder.findByName(customerName).get().getId();
        Cookies cookie = Cookies.valueOf(recipe);
        cartModifier.update(this.customerId, new Item(cookie, -howMany));
    }

    @Then("the price of {string}'s cart is equals to {double}")
    public void thePriceOfSebSCartIsEqualsTo(String customerName, double expectedPrice) throws CustomerIdNotFoundException {
        this.customerId = customerFinder.findByName(customerName).get().getId();
        assertEquals(expectedPrice, cartProcessor.cartPrice(customerId), 0.01);
    }

    @And("{string} validates the cart and pays through the bank")
    public void validatesTheCart(String customerName) throws EmptyCartException, PaymentException, CustomerIdNotFoundException {
        this.customerId = customerFinder.findByName(customerName).get().getId();
        orderId = cartProcessor.validate(customerId).getId();
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
