# Business Components

  * Author: Philippe Collet

We focus here on the implementation of a first component, dedicated to handle customer's carts, enables to show the main mechanisms of the business component layer.

## Provided Interfaces

The component is very basic, still we apply interface segregation with two interfaces:

  * `CartModifier`: operations to modify a given customer's cart, like adding or removing cookies, and to retrieve the contents of the cart;

```java
public interface CartModifier {

    Item update(Long customerId, Item it) throws NegativeQuantityException, CustomerIdNotFoundException;

    Set<Item> cartContent(Long customerId) throws CustomerIdNotFoundException;
}
```

  * `CartProcessor`: operations for computing the cart's price and validating it to process the associated order;

```java
public interface CartProcessor {

    double cartPrice(Long customerId) throws CustomerIdNotFoundException;

    Order validate(Long customerId) throws PaymentException, EmptyCartException, CustomerIdNotFoundException;
}
```

Nothing special is needed on these interfaces. They are plain Java. They are simply going to be implemented by the component to be provided by it.

## Business Component and Required Interfaces

The `CartHandler` component is a Java class that implements both interfaces while being annotated with `@Service`. 

```java
@Service
public class CartHandler implements CartModifier, CartProcessor {
```

This annotation has the same semantics in Spring as `@Component` but it is often used to define the service layer of a component-based system. In our case, the service layer of the business part is more or less defined by components that takes or returns `Id` from the controller layer. Some other components will be completely hidden in the business part, such as `Cashier`. Some others, like `Orderer` implements both interfaces that references ids while some other references objects.
The controller part is implemented by `RestController` components (see the dedicated chapter).
All annotations (`@Service`, `@Component`, and `@RestController`) enable the Spring container to create all found components (by default as singleton) when initializing the web app.

Required interfaces for implementing customers' carts are `Payment` to process the cart for payment and `CustomerFinder`. Both interfaces are used in the declaration for two attributes of the component.
The `@Autowired` annotation is placed on the constructor that initializes both attributes. This annotation will enable the Spring container to inject the reference to the single component implementing this interface when initializing the container. If any `@Autowired` attribute cannot be injected, the Spring container will raise an exception and stop, before any functional calls through interfaces can be triggered.

```java
    private final Payment payment;

    private final CustomerFinder customerFinder;

    @Autowired
    public CartHandler(Payment payment, CustomerFinder customerFinder) {
        this.payment = payment;
        this.customerFinder = customerFinder;
    }
```

Note that for component implementation (not testing), it is preferable to inject at the constructor level rather than at the attribute level, so that all dependencies are well initialized at construction time of the component.


## Business Logic

The `update` method implementation is checking the consistency of the request, e.g., that the customerId references an known custimer, that someone is not removing too much cookies from the cart, and is throwing an exception if needed. At the end, it notably update the `customer`object that contains the set of items representing the cart. As the method is annotated with `@Transactional`, it creates or joins an existing transaction and the customer object will be automatically updated at the end of the method (by the generation of the proper SQL code), or rollback in case of exception.

```java
@Transactional
    public Item update(Long customerId, Item item) throws NegativeQuantityException, CustomerIdNotFoundException {
    ...
```

One interesting implementation is the `validate` method. After checking for the customer corresponding to the id, it checks that the cart is not empty (or throws EmptyCartException), then delegates the payment to the `cashier` through the required interface, gets an `Order` object from it (or a PaymentException), clears the content of the cart and return the `Order`. Here the `Payment` interface enables the logic in this component to be restricted to its own responsibility: *I check for the cart, someone else handles payment and I get back a created Order I just have to forward back.* I don't care about which component is actually serving the `Payment`.

**This is really the essence of component-based software.**

```java
    @Override
    @Transactional
    public Order validate(Long customerId) throws PaymentException, EmptyCartException, CustomerIdNotFoundException {
        Customer customer = customerFinder.retrieveCustomer(customerId);
        if (customer.getCart().isEmpty())
            throw new EmptyCartException(customer.getName());
        Order newOrder = payment.payOrderFromCart(customer, cartPriceFromCustomer(customer));
        customer.clearCart();
        return newOrder;
    }
```

## Another Component: CustomerRegistry

The component `CustomerRegistry` is used by `CartHandler` to find a customer from its id (through the `CustomerFinder` interface), and is also responsible for creating a customer (through its `CustomerRegistration` interface).
To implement both interfaces, the component is potentially looking for objects in its data source, or creating a new one in the data source. This is abstracted throug the `Repository` interface, here extended for the `Customer` (see the Persistence chapter for details).

```java
@Service
public class CustomerRegistry implements CustomerRegistration, CustomerFinder {

    private final CustomerRepository customerRepository;
```

This is illustrated by the usage of `findBy` and `save` method from the `CustomerRepository` interface.

```java
    @Override
    @Transactional(readOnly = true)
    public Optional<Customer> findById(Long id) {
        return customerRepository.findById(id);
    }

    @Override
    @Transactional
    public Customer register(String name, String creditCard)
            throws AlreadyExistingCustomerException {
        if (findByName(name).isPresent())
            throw new AlreadyExistingCustomerException(name);
        Customer newcustomer = new Customer(name, creditCard);
        return customerRepository.save(newcustomer);
    }
```


