package fr.univcotedazur.simpletcfs.components;

import fr.univcotedazur.simpletcfs.entities.Cookies;
import fr.univcotedazur.simpletcfs.entities.Customer;
import fr.univcotedazur.simpletcfs.entities.Item;
import fr.univcotedazur.simpletcfs.exceptions.AlreadyExistingCustomerException;
import fr.univcotedazur.simpletcfs.exceptions.CustomerIdNotFoundException;
import fr.univcotedazur.simpletcfs.exceptions.EmptyCartException;
import fr.univcotedazur.simpletcfs.exceptions.NegativeQuantityException;
import fr.univcotedazur.simpletcfs.interfaces.CartModifier;
import fr.univcotedazur.simpletcfs.interfaces.CartProcessor;
import fr.univcotedazur.simpletcfs.interfaces.CustomerRegistration;
import fr.univcotedazur.simpletcfs.repositories.CustomerRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest // you can make test non transactional to be sure that transactions are properly handled in
        // controller methods (if you are actually testing controller methods!)
// @Transactional
// @Commit // default @Transactional is ROLLBACK (no need for the @AfterEach
class CartHandlerTest {

    @Autowired
    private CartModifier cartModifier;

    @Autowired
    private CartProcessor cartProcessor;

    @Autowired
    private CustomerRegistration customerRegistration;

    @Autowired
    private CustomerRepository customerRepository;

    private Long johnId;

    @BeforeEach
    void setUp() throws AlreadyExistingCustomerException {
        johnId = customerRegistration.register("John", "1234567890").getId();
    }

    @AfterEach
    void cleaningUp()  {
        Optional<Customer> toDispose = customerRepository.findCustomerByName("John");
        toDispose.ifPresent(customer -> customerRepository.delete(customer));
        johnId = null;
    }

    @Test
    void emptyCartByDefault() throws CustomerIdNotFoundException {
        assertEquals(0, cartModifier.cartContent(johnId).size());
    }

    @Test
    void addItems() throws NegativeQuantityException, CustomerIdNotFoundException {
        Item itemResult = cartModifier.update(johnId, new Item(Cookies.CHOCOLALALA, 2));
        assertEquals(new Item(Cookies.CHOCOLALALA, 2), itemResult);
        cartModifier.update(johnId, new Item(Cookies.DARK_TEMPTATION, 3));
        Set<Item> oracle = Set.of(new Item(Cookies.CHOCOLALALA, 2), new Item(Cookies.DARK_TEMPTATION, 3));
        assertEquals(oracle, cartModifier.cartContent(johnId));
    }

    @Test
    void removeItems() throws NegativeQuantityException, CustomerIdNotFoundException {
        cartModifier.update(johnId, new Item(Cookies.CHOCOLALALA, 2));
        Item itemResult = cartModifier.update(johnId, new Item(Cookies.CHOCOLALALA, -2));
        assertEquals(new Item(Cookies.CHOCOLALALA, 0), itemResult);
        assertEquals(0, cartModifier.cartContent(johnId).size());
        cartModifier.update(johnId, new Item(Cookies.CHOCOLALALA, 6));
        cartModifier.update(johnId, new Item(Cookies.CHOCOLALALA, -5));
        Set<Item> oracle = Set.of(new Item(Cookies.CHOCOLALALA, 1));
        assertEquals(oracle, cartModifier.cartContent(johnId));
    }

    @Test
    void removeTooMuchItems() throws NegativeQuantityException, CustomerIdNotFoundException {
        cartModifier.update(johnId, new Item(Cookies.CHOCOLALALA, 2));
        cartModifier.update(johnId, new Item(Cookies.DARK_TEMPTATION, 3));
        Assertions.assertThrows(NegativeQuantityException.class, () -> cartModifier.update(johnId, new Item(Cookies.CHOCOLALALA, -3)));
        Set<Item> oracle = Set.of(new Item(Cookies.CHOCOLALALA, 2), new Item(Cookies.DARK_TEMPTATION, 3));
        assertEquals(oracle, cartModifier.cartContent(johnId));
    }

    @Test
    void modifyQuantities() throws NegativeQuantityException, CustomerIdNotFoundException {
        cartModifier.update(johnId, new Item(Cookies.CHOCOLALALA, 2));
        cartModifier.update(johnId, new Item(Cookies.DARK_TEMPTATION, 3));
        cartModifier.update(johnId, new Item(Cookies.CHOCOLALALA, 3));
        Set<Item> oracle = Set.of(new Item(Cookies.CHOCOLALALA, 5), new Item(Cookies.DARK_TEMPTATION, 3));
        assertEquals(oracle, cartModifier.cartContent(johnId));
    }

    @Test
    void getTheRightPrice() throws NegativeQuantityException, CustomerIdNotFoundException {
        cartModifier.update(johnId, new Item(Cookies.CHOCOLALALA, 2));
        cartModifier.update(johnId, new Item(Cookies.DARK_TEMPTATION, 3));
        cartModifier.update(johnId, new Item(Cookies.CHOCOLALALA, 3));
        assertEquals(12.20, cartProcessor.cartPrice(johnId), 0.01);
    }

    @Test
    void cannotProcessEmptyCart() throws Exception {
        assertEquals(0, cartModifier.cartContent(johnId).size());
        Assertions.assertThrows(EmptyCartException.class, () -> cartProcessor.validate(johnId));
    }


}
