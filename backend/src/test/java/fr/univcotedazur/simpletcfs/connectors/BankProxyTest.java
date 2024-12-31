package fr.univcotedazur.simpletcfs.connectors;

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.univcotedazur.simpletcfs.connectors.externaldto.PaymentReceiptDTO;
import fr.univcotedazur.simpletcfs.entities.Customer;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.io.IOException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

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
    static void tearDown() {
        try {
            mockWebServer.shutdown();
        } catch (IOException e) {
            // Open bug: The timeout tests may keep the MockWebServer thread running, see : https://github.com/square/okhttp/issues/6976#issuecomment-1006434057
            Thread.getAllStackTraces().keySet().stream().filter(t -> t.getName().startsWith("MockWebServer /127.0.0.1")).forEach(it -> {
                System.err.println("MockWebServer thread found despite being shutdown, shutting down :" + it.getName());
                it.interrupt();
            });
        }
    }

    @BeforeEach
    void init() {
        bankProxy = new BankProxy(mockWebServer.url("/").toString());
    }

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

    @Test
    void payWithWrongStatusReturned() throws Exception {
        // Given
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(HttpStatus.OK.value()) // should be CREATED
                .setBody(objectMapper.writeValueAsString(new PaymentReceiptDTO("654321", 100.0)))
                .addHeader("Content-Type", "application/json"));
        // When
        Optional<String> payReceiptId = bankProxy.pay(new Customer("nameIsNotImportant", "1234567890"), 100.0);
        // Then (Junit style as we are handling an Optional object, not a Mono/Flux reactor object)
        assertTrue(payReceiptId.isEmpty());
    }

    @Test
    void payWithEmptyBody() {
        // Given
        mockWebServer.enqueue(new MockResponse().setResponseCode(HttpStatus.CREATED.value()));
        // When
        Optional<String> payReceiptId = bankProxy.pay(new Customer("nameIsNotImportant", "1234567890"), 100.0);
        // Then (Junit style as we are handling an Optional object, not a Mono/Flux reactor object)
        assertTrue(payReceiptId.isEmpty());
    }

    @Test
    void payRejected() {
        // Given
        mockWebServer.enqueue(new MockResponse().setResponseCode(HttpStatus.BAD_REQUEST.value()));
        // When
        Optional<String> payReceiptId = bankProxy.pay(new Customer("nameIsNotImportant", "1234567890"), 100.0);
        // Then (Junit style as we are handling an Optional object, not a Mono/Flux reactor object)
        assertTrue(payReceiptId.isEmpty());
    }

    @Test
    void payOn404shouldRaiseAnException() {
        // Given
        Customer customer = new Customer("nameIsNotImportant", "1234567890");
        mockWebServer.enqueue(new MockResponse().setResponseCode(HttpStatus.NOT_FOUND.value()));
        // When
        assertThrows(WebClientResponseException.class, () -> bankProxy.pay(customer, 100.0));
    }

    @Test
    void payOn500shouldRaiseAnException() {
        // Given
        Customer customer = new Customer("nameIsNotImportant", "1234567890");
        mockWebServer.enqueue(new MockResponse().setResponseCode(HttpStatus.INTERNAL_SERVER_ERROR.value()));
        // When-Then
        assertThrows(WebClientResponseException.class, () -> bankProxy.pay(customer, 100.0));
    }

    @Test
    void payTimeout() {
        // Given
        Customer customer = new Customer("nameIsNotImportant", "1234567890");
        MockResponse response = new MockResponse()
                .setBody("Delayed Response That Will Never Come")
                .setBodyDelay(10, java.util.concurrent.TimeUnit.SECONDS);
        mockWebServer.enqueue(response);
        // When-Then
        assertThrows(RuntimeException.class, () -> bankProxy.pay(customer, 100.0));
    }
}
