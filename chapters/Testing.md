# On Testing

  * Author: Philippe Collet

We focus here on several kinds of tests that can be done in the Spring stack. It must be noted that some of them can be used to implement integration testing or end to end testing depending on which components are assembled, mocked, and even deployed.

## Basic Testing: the `Catalog` Component

We focus here on the implementation of a first very simple component that contains the catalog of cookie recipes. The implementation is really straightforward with only two methods, one to list all recipes, the other one to find recipes matching a given string.
As a result, writing the functiona parts of the tests for the two methods is rather simple (see [CatalogTest](../backend/src/test/java/fr/univcotedazur/simpletcfs/components/CatalogTest.java)):

```java
    @Test
    void listPreMadeRecipesTest() {
        Set<Cookies> premade = catalog.listPreMadeRecipes();
        assertEquals(3, premade.size());
    }

    @Test
    void exploreCatalogueTest() {
        assertEquals(0, catalog.exploreCatalogue("unknown").size());
        assertEquals(2, catalog.exploreCatalogue(".*CHOCO.*").size());
        assertEquals(1, catalog.exploreCatalogue(Cookies.DARK_TEMPTATION.name()).size());
    }
```

This code is purely functional, assuming a `catalogExplorator` (the interface, no one cares about the concrete implementation). However, as the Catalog implementation is going to be a component, its lifecycle is going to be handled by the Spring container. It is not your responsibility anymore to instantiate components when in Spring.

The test setup is also straightforward as the `Catalog`component has no required interface. You only need to annotate the Test class with `@SpringBootTest` so that everything is setup by the SpringBoot test container:

* A specific test container is started. By default, it will find all components like the main container.
* All other specific wirings with JUnit 5, Mockito, etc. are done by the Spring test container. If a framework is not directly supported, it is likely to provide an extension annotation that you will have to add on the class.

Then the only additional setup is to inject (with `@Autowired`) the component under test in the class. As a result, it really looks like an average Spring implementation of a component.

```java
@SpringBootTest
class CatalogTest {

    @Autowired
    Catalog catalog;
```


## Running different types of test with maven

By default, maven use its *surefire* plugin to run tests. This plugin is especially built for running unit tests, as it will diretly fail if any test fails. This is a good property for preventing the build to be made (the goal *package* will typically fail).
However, when you implement integration tests, you usually want to:

   * isolate them from unit tests (e.g. to run them only on a CI server),
   * use built packages (that have passed unit tests) to put some of them together to setup a context for some integration tests, and cleaning up this context if some tests fail.

The *failsafe* plugin is made for that! From the [FAQ](https://maven.apache.org/surefire/maven-failsafe-plugin/faq.html#surefire-v-failsafe):

   * *maven-surefire-plugin* is designed for running unit tests and if any of the tests fail then it will fail the build immediately.
   * *maven-failsafe-plugin* is designed for running integration tests, and decouples failing the build if there are test failures from actually running the tests.

First, we have to include both plugins:

```xml
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-surefire-plugin</artifactId>
            </plugin>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-failsafe-plugin</artifactId>
...
                <executions>
                    <execution>
                        <goals>
                            <goal>integration-test</goal>
                            <goal>verify</goal>
                        </goals>
                    </execution>
                </executions>
            </plugin>
```

It must be noted that *surefire* will, by default, find tests with the following names and run them during the `test` phase (i.e. just before `package`):  
   
   * `"**/Test*.java"` - includes all of its subdirectories and all Java filenames that start with "Test".
   * `"**/*Test.java"` - includes all of its subdirectories and all Java filenames that end with "Test".
   * `"**/*Tests.java"` - includes all of its subdirectories and all Java filenames that end with "Tests".
   * `"**/*TestCase.java"` - includes all of its subdirectories and all Java filenames that end with "TestCase".`


On its side, *failsafe* is integrated in the `verify`phase, and will run integration tests that follow, by default, the following patterns:

   * `"**/IT*.java"` - includes all of its subdirectories and all Java filenames that start with "IT".
   * `"**/*IT.java"` - includes all of its subdirectories and all Java filenames that end with "IT".
   * `"**/*ITCase.java"` - includes all of its subdirectories and all Java filenames that end with "ITCase".

With this setup, a classic packaging command:

    mvn clean package

will run unit tests, while a `verify` command:

    mvn clean verify
    
will first run unit tests through *surefire*, and then the integration tests through *failsafe*. In our case, it will run a test of the full backend through a controller and a set of Cucumber tests (see below for details).

If one wants to separate integration tests (e.g., in a CI) the following command will only run them:

    mvn clean verify '-Dtest=!*' -DfailIfNoTests=false

## Testing the `CartHandler` Component

Let us now focus on the implementation of a more complex component, dedicated to handle customer's carts. 
Some explanations on the `CartHandler` component implementation can be found in the [Business Components](BusinessComponents.md) chapter.

The previously implemented component should ensure the four following properties: (i) the cart of a given customer is empty by default, (ii) adding multiple items results in a cart containing such items, (iii) one can remove cookies from a cart and finally (iii) one can modify the already existing quantity for a given item. Considering a reference the different used interfaces, `cartModifier` for the `CartModifier`,  `cartProcessor`for the `CartProcessor`, `CustomerRegistration` to create customers, `CustomerRepository` to clean the repository (as this test is not transactional),
it is again quite simple to write some tests to cover the functionalities (see [CartHandlerTest](../backend/src/test/java/fr/univcotedazur/simpletcfs/components/CartHandlerTest.java)).

```java
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
```

We can then start to configure our test class just like in the `CatalogTest`. We annotate the class and we inject the interfaces:

```java
@SpringBootTest 
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
        johnId = 0L;
    }
```

## Mocking

In the previous test, the `CartHandler` component was tested through its two provided interfaces, but it has also required interfaces. Actually, the Spring test container was behaving like the normal one, looking for dependencies (`@Autowired`) recursively. So the `Cashier` component was created, injected through its interface `Payment` inside `CartHandler`, so on for the `BankProxy` created and connected to `Cashier` and for the `Orderer` as well.

Now let us test the `Cashier`component, which provides the `Payment` interface with a single method `Order payOrder(Customer customer, Set<Item> items) throws PaymentException;`. It looks easy, we should write a test to get the Order if the payment is going well, and another one in case the payment is rejected with the method throwing `PaymentException` (see [CashierTest](../backend/src/test/java/fr/univcotedazur/simpletcfs/components/CashierTest.java)).

```java
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
```

The main issue here is that the `Cashier` reuses the `BankProxy`, which itself is calling the external bank system. It is clearly a use case for mocking. Here the easiest way to write the test is to mock the required interface (and the component that should have been implementing this interface). In our case, this is the `Bank` interface, so it will be declared as an attribute with the `@MockitoBean` annotation instead of the `@Autowired`. `@MockitoBean` calls Mockito in a well-integrated way together with the Spring test container. 
Watch out that since we bump up SpringBoot version to 3.4+, we do not use `@MockBean` anymore, which is deprecated, but `@MockitoBean` instead.

```java
@SpringBootTest
@Transactional 
@Commit 
class CashierTest {

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private Payment cashier;

    @MockitoBean
    private Bank bankMock;
```

Consequently, it enables one to write a test setup with Mockito directives (e.g. `when`and `thenReturn`) other the mocked interface. In our case, the mock is a bit smart, accepting (with true) the payment if the payer is John, and rejecting it if the payer is Pat.

It is important to note that as the test is `@Transactional` but with a `@Commit` behavior (committing between tests whereas the default behavior is to rollback), we have to remove everything in a `@AfterEach` method. In the general case, a `@Transactional` test would be preferred, this is just provided as an example.

```java
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
```

## BDD in Spring

Behavioral-Driven Development (BDD) bridges the gap between scenarios, which could be very close, in the Gherkin syntax, to acceptance criteria, and tests. This enables to mechanize tests that follows use cases or acceptance criteria from a user story.

We consider here several tests so that we have a setup that can handle many of them in a proper way.

### Setting-up Cucumber

The _de facto_ standard to implements BDD in the Java ecosystem is the [Cucumber](https://cucumber.io/) framework. It bounds a requirements engineering language ([Gherkin](https://cucumber.io/docs/gherkin/)] to JUnit tests, using plain regular expressions.

The setup is done through a bom (bill of materials in maven pom).

We just have to add the following dependencies in the POM file of the backend:

```xml
  <properties>
		...
		<cucumber.version>7.20.1</cucumber.version>
  </properties>

	<dependencyManagement>
		<dependencies>
			<dependency>
				<groupId>io.cucumber</groupId>
				<artifactId>cucumber-bom</artifactId>
				<version>${cucumber.version}</version>
				<type>pom</type>
				<scope>import</scope>
			</dependency>
		</dependencies>
	</dependencyManagement>
	
	...
		<dependency>
			<groupId>io.cucumber</groupId>
			<artifactId>cucumber-java</artifactId>
		</dependency>
		<dependency>
			<groupId>io.cucumber</groupId>
			<artifactId>cucumber-junit-platform-engine</artifactId>
			<scope>test</scope>
		</dependency>
		<dependency>
			<groupId>io.cucumber</groupId>
			<artifactId>cucumber-spring</artifactId>
			<scope>test</scope>
		</dependency>
		<dependency>
			<groupId>org.junit.platform</groupId>
			<artifactId>junit-platform-suite</artifactId>
			<scope>test</scope>
		</dependency>
```

Note that we are not using any more the *cucumber-junit* artifact, but the *cucumber-junit-platform-engine* with the *junit-platform-suite* that enables a full support for JUnit 5 and its `@Suite@` annotation for the setup (see below).

### Modelling use cases or scenarios as Features

We consider first the use case "Ordering cookies" that is more or less the one used for testing the CartHandler:

  1. Considering a customer that exists in the system.
  2. The customer add some cookies to her cart.
  3. The cart is updated (and remove duplicates, if any).
  4. The cart is validated, paid (and the order is created).

The Use case _Ordering cookies_ is modelled as a `Feature`, and described using ([Gherkin](https://cucumber.io/docs/gherkin/)], a requirement language based on the _Given, When, Then_ paradigm. We create a file named `OrderingCookies.feature`, where we describe an instance of this very scenario:

```gherkin
Feature: Ordering Cookies

  This feature supports the way a Customer can order cookies through a cart

  Background:
    Given a customer named "Maurice" with credit card "1234896983"
    
  Scenario: Modifying the number of cookies inside an order
    When "Maurice" orders 2 x "CHOCOLALALA"
    And "Maurice" orders 3 x "DARK_TEMPTATION"
    And "Maurice" orders 3 x "CHOCOLALALA"
    And "Maurice" asks for his cart contents
    Then there are 2 items inside the cart
    And the cart contains the following item: 5 x "CHOCOLALALA"
    And the cart contains the following item: 3 x "DARK_TEMPTATION"
```

A `Scenario` contains several steps. A `Given` one represents the context of the scenario, a `When` one the interaction with the SuT (_system under test_) and a `Then` is an assertion expected from the SuT. The `Background` section is a sub-scenario that is common to all the others, and executed before their contents.

To implement the behaviour of each steps, we can rely on a testing frameork, e.g., JUnit. We create a test class named `OrderingCookies`, where each step is implemented as a method. The matching that binds a step to a test method is reified as classical regular expressions (e.g. `(\\d+) for an integer`) or as specific Cucumber expression (e.g. {string} for a string between double quotes).  Method parameters correspond to each matched expression, one after another.

*Note that for easing the configuration process, the feature file and the implementation step class are placed in the same hierarchy, one inside `resources` and the other inside `test/java`.

Setting up or cleaning the context is possible through specific Cucumber annotation (e.g. `@BeforeAll`, `@Before`, `@BeforeStep`, `@After`...). Be careful as most of them have the same name as JUnit ones, but they must be imported from the `io.cucumber.java` package.

```java
public class OrderingCookies {

...
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
...
```


### Cucumber-Junit-Spring setup and execution at scale

In Java, the Cucumber framework relies on JUnit, and some specific setup is also necessary. One additional class with enable the configuration of the JUnit 5 runner with a Cucumber specific plugin, and some options (typically the location of the feature files) can be specified:

```java
@Suite
@IncludeEngines("cucumber")
@SelectClasspathResource("features/ordering")
@ConfigurationParameter(key = PLUGIN_PROPERTY_NAME, value = "pretty")
@ConfigurationParameter(key = GLUE_PROPERTY_NAME, value = "fr.univcotedazur.simpletcfs.cucumber.ordering")
public class OrderingCucumberRunnerIT { // IT suffix on test classes make them "Integration Test" run by "verify" goal in maven (see pom.xml)
}
```

Two issues must be handled to finish the setup and make it work easily with many features and some Spring mocking:

First, for each runner (see above), you need to have one and only configuration class annotated with

```java
@CucumberContextConfiguration
@SpringBootTest
```

If this is setup is placed on the class implementing the steps, there should only be one such class for the runner. Otherwise, you should create one configuration class, like `OrderingCucumberConfig`

```java
@CucumberContextConfiguration
@SpringBootTest
public class OrderingCucumberConfig {

    @MockitoBean // Spring/Cucumber bug workaround: declare the mock here, and autowire+setup it in the step classes
    private Bank bankMock;

}
```
Note that all steps definition classes have then no annotations at the class level (see both stepdefs classes in the `cucumber/ordering` package, and check the two separate runner, for ordering and for catalog BDD testing that are provided to show how to separate the configuration.

*Spring + Cucumber 7+ specifics:* Second, if you need to use mocks (with `@MockitoBean`) in the steps definition classes, you need to create them in the configuration class (like in `OrderingCucumberConfig` above) and to `@Autowired` the same mock in the stepdefs classes:

```java
public class OrderingCookies {

...

    @Autowired // Spring/Cucumber bug workaround: autowired the mock declared in the Config class
    private Bank bankMock;
    
...

    @Before
    public void settingUpContext() throws PaymentException {
        customerRepository.deleteAll();
        orderRepository.deleteAll();
        when(bankMock.pay(any(Customer.class), anyDouble())).thenReturn(Optional.of("payReceiptIdOK"));
    }
```

## Testing a RestController in isolation

As a RestController is supposed to only handle interoperability and delegate messages to the business components, a first approach is to unit test it. In Spring, it is possible to start only the Spring MVC container in test mode, with a REST controller being configured and no other Spring components nor the server itsef.
The `@WebMvcTest` annotation does this by disabling the full auto-configuration mode.
The class passed as parameter set up the controller to be created in this testing environment (here `RecipeController`).
On the configuration side, one must note that we add here the `@AutoConfigureWebClient` to avoid error about a missing `RestTemplateBuilder`. This may happen due to conflict between auto-configuration and test configuration in Spring.

Then it is quite easy to reuse the mock support (with `@MockitoBean`, as shown in the [Mocking](#mocking) section. For the RecipeController, we have to mock the `CatalogExplorator` interface and return a set of Cookies. One can note that in our case, we only return a set of 2 recipes while the real implementation has three of them (to show that we are indeed mocking the recipes here).

The last step consists in injection a component of type `MockMvc` and in using it to perform a call to hit the API, thus the controller under test, and then verify the status response codes and response content. 
This is done through `MockMvcRequestBuilders`, `MockMvcResultMatchers`, and `MockMvcResultHandlers`, statically imported in our case.
we thus perfom a `get` and we verify the `status()` and the content of the JSON payload through `jsonPath` methods.

```java
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RecipeController.class) 
@AutoConfigureWebClient
public class RecipeWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CatalogExplorator mockedCat; // the real Catalog component is not created, we have to mock it

    @Test
    void recipesRestTest() throws Exception {
        when(mockedCat.listPreMadeRecipes())
                .thenReturn(Set.of(Cookies.CHOCOLALALA,Cookies.DARK_TEMPTATION)); // only 2 of the 3 enum values

        mockMvc.perform(get(RecipeController.BASE_URI)
                        .contentType(APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$", hasSize(2)))
                    .andExpect(jsonPath("$", hasItem("CHOCOLALALA")))
                    .andExpect(jsonPath("$", hasItem("DARK_TEMPTATION")));
        }

}
```

## Testing a RestController with the full backend

If we can test a REST controller is isolation, we can also setup a full backend with the MVC container and the business container up. We can then implement a kind of integration test.
To do so, we declare a classic `@SpringBootTest` and add a `@AutoConfigureMockMvc` so that the whole backend is started.

The configuration of the test environement is completed by setting a value to the `webEnvironment` variable passed to `@SpringBootTest`. It can take several values:

   * `WebEnvironment.RANDOM_PORT` starts an embedded server with a random port, which is useful to avoir conflict in test environments while being closer to the real application deployment.
   * `WebEnvironment.DEFINED_PORT` starts an embedded server with a fixed port, usually used in some specific constraints are to be applied on ports.
   * `WebEnvironment.MOCK` is the default. It loads a web application context and provides a mock web environment. It does not load a real http server, just mocks the entire web server behavior. You gain isolation but it is weaker in terms of integration.
   * `WebEnvironment.NONE` loads the business part but does not provide any web environment (mocked or not).

Then the test can be written using the same principle as with the isolated test, injecting a `MockMvc`, performing a call to the controller (here `get`), and checking the result. Here the check ensures that the JSON contains the 3 recipes of the real implementation.

One must note that the naming of the test class, ending with *IT*, makes it match with the patterns of the *failsafe* plugin we use [to run integration tests](#running-different-types-of-test-with-maven).


```java
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc 
public class RecipeWebAutoConfigureIT {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void recipesFullStackTest() throws Exception {
        mockMvc.perform(get(RecipeController.BASE_URI)
                        .contentType(APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$", hasSize(3)))
                    .andExpect(jsonPath("$", hasItem("CHOCOLALALA")))
                    .andExpect(jsonPath("$", hasItem("DARK_TEMPTATION")))
                    .andExpect(jsonPath("$", hasItem("SOO_CHOCOLATE")));
        }

}
```


## Testing a REST WebClient

It is also possible to test the WebClient (from Spring Weflux) that we use for calling REST routes (from the cli to the backend, and from the backend to the bank). 

To do so, we have to use a `MockWebServer` provided by the *okhttp3* library (see dependency in the `pom.xml` files) as it is the supported way for the whole WebFlux stack. As you can see below in the `BankProxyTest` we create a static mock server that we initialize and start in the `@BeforeAll` method. It will be shutdown at the end all the tests of this class (cf. `@AfterAll` method). Before each test, an appropriate method create a new `BankProxy` component to be tested, providing the URL of the mock server as a parameter to set it up.

```java
class BankProxyTest {

    private static MockWebServer mockWebServer;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private BankProxy bankProxy;

    @BeforeAll
    static void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();
    }

    @AfterAll
    static void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    @BeforeEach
    void init() {
        bankProxy = new BankProxy(mockWebServer.url("/").toString());
    }
```
Then, in each test, one can enqueue `MockReponse()` that will be returned by the mock server during the test. In the case of a successful payment, we return the right body (with the help of an `objectMapper` as the *okhhtp3* library only supports String to set it up), the right status code and the right header. After the call to the `pay` method on the bankProxy under test, we can use assert methods as usual.

```java
    @Test
    void payWithSuccess() throws Exception {
        // Given
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(HttpStatus.CREATED.value())
                .setBody(objectMapper.writeValueAsString(new PaymentReceiptDTO("654321", 100.0)))
                .addHeader("Content-Type", "application/json"));
        // When
        Optional<String> payReceiptId = bankProxy.pay(new Customer("nameIsNotImportant", "1234567890"), 100.0);
        // Then (Junit style as we are handling an Optional object, not a Mono/Flux reactor object)
        assertTrue(payReceiptId.isPresent());
        assertEquals("654321", payReceiptId.get());
    }
```

The class contains all the tests to cover all exceptional cases, for example if the bank would return a 404 error code:

```java
    @Test
    void payOn404shouldRaiseAnException() {
        // Given
        Customer customer = new Customer("nameIsNotImportant", "1234567890");
        mockWebServer.enqueue(new MockResponse().setResponseCode(HttpStatus.NOT_FOUND.value()));
        // When
        assertThrows(WebClientResponseException.class, () -> bankProxy.pay(customer, 100.0));
    }
```

With this dedicated testing framework we can also get a `RecordedRequest` object that contains the last call done on the mock server. This allows for checking the form of the request if needed. Below, we show the test of the recipe command call in the *cli*:

```java
@Test
    void recipesSetTest() throws Exception {
        // Given
        mockWebServer.enqueue(new MockResponse()
                .setBody("[\"CHOCOLALALA\",\"DARK_TEMPTATION\",\"SOO_CHOCOLATE\"]")
                .addHeader("Content-Type", "application/json"));

        // When-Then
        assertEquals(EnumSet.allOf(CookieEnum.class), client.recipes());

        // Verify the request was made to the correct endpoint
        RecordedRequest recordedRequest = mockWebServer.takeRequest();
        assertEquals("/recipes", recordedRequest.getPath());
        assertEquals("GET", recordedRequest.getMethod());
```
