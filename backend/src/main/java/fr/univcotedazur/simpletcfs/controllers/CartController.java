package fr.univcotedazur.simpletcfs.controllers;

import fr.univcotedazur.simpletcfs.*;
import fr.univcotedazur.simpletcfs.controllers.dto.ErrorDTO;
import fr.univcotedazur.simpletcfs.entities.Customer;
import fr.univcotedazur.simpletcfs.entities.Item;
import fr.univcotedazur.simpletcfs.entities.Order;
import fr.univcotedazur.simpletcfs.exceptions.CustomerIdNotFoundException;
import fr.univcotedazur.simpletcfs.exceptions.EmptyCartException;
import fr.univcotedazur.simpletcfs.exceptions.NegativeQuantityException;
import fr.univcotedazur.simpletcfs.exceptions.PaymentException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;
import java.util.Set;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping(path = CustomerCareController.BASE_URI, produces = APPLICATION_JSON_VALUE)
// referencing the same BASE_URI as Customer care to extend it hierarchically
public class CartController {

    public static final String CART_URI = "/{customerId}/cart";

    @Autowired
    private CartModifier cart;

    @Autowired
    private CartProcessor processor;

    @Autowired
    private CustomerFinder finder;

    @ExceptionHandler({CustomerIdNotFoundException.class})
    public ResponseEntity<ErrorDTO> handleExceptions(CustomerIdNotFoundException e)  {
        ErrorDTO errorDTO = new ErrorDTO();
        errorDTO.setError("Customer not found");
        errorDTO.setDetails(e.getId() + " is not a valid customer Id");
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorDTO);
    }

    @ExceptionHandler({EmptyCartException.class})
    public ResponseEntity<ErrorDTO> handleExceptions(EmptyCartException e)  {
        ErrorDTO errorDTO = new ErrorDTO();
        errorDTO.setError("Cart is empty");
        errorDTO.setDetails("from Customer " + e.getName());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorDTO);
    }

    @ExceptionHandler({NegativeQuantityException.class})
    public ResponseEntity<ErrorDTO> handleExceptions(NegativeQuantityException e)  {
        ErrorDTO errorDTO = new ErrorDTO();
        errorDTO.setError("Attempting to update the cookie quantity to a negative value");
        errorDTO.setDetails("from Customer " + e.getName() + "with cookie " + e.getCookie() +
                " leading to quantity " + e.getPotentialQuantity());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorDTO);
    }

    @ExceptionHandler({PaymentException.class})
    public ResponseEntity<ErrorDTO> handleExceptions(PaymentException e)  {
        ErrorDTO errorDTO = new ErrorDTO();
        errorDTO.setError("Payment was rejected");
        errorDTO.setDetails("from Customer " + e.getName() + " for amount " + e.getAmount());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorDTO);
    }

    @PostMapping(path = CART_URI, consumes = APPLICATION_JSON_VALUE)
    public ResponseEntity<Item> updateCustomerCart(@PathVariable("customerId") Long customerId, @RequestBody Item it) throws CustomerIdNotFoundException, NegativeQuantityException {
        int newQuantity = cart.update(retrieveCustomer(customerId),it);
        return ResponseEntity.ok(new Item(it.getCookie(),newQuantity));
    }

    @GetMapping(CART_URI)
    public ResponseEntity<Set<Item>> getCustomerCartContents(@PathVariable("customerId") Long customerId) throws CustomerIdNotFoundException {
        return ResponseEntity.ok(processor.contents(retrieveCustomer(customerId)));
    }

    @PostMapping(path = CART_URI+"/validate")
    public ResponseEntity<String> validate(@PathVariable("customerId") Long customerId) throws CustomerIdNotFoundException, EmptyCartException, PaymentException {
        Order order = processor.validate(retrieveCustomer(customerId));
        return ResponseEntity.ok().body("Order " + order.getId() + " (amount " + order.getPrice() +
                ") is validated");
    }

    private Customer retrieveCustomer(Long customerId) throws CustomerIdNotFoundException {
        Optional<Customer> custopt = finder.findById(customerId);
        if (custopt.isEmpty()) {
            throw new CustomerIdNotFoundException(customerId);
        }
        return custopt.get();
    }

}
