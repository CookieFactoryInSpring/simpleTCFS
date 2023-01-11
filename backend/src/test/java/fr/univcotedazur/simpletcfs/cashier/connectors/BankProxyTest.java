package fr.univcotedazur.simpletcfs.cashier.connectors;

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.univcotedazur.simpletcfs.cashier.connectors.externaldto.PaymentReceiptDTO;
import fr.univcotedazur.simpletcfs.cashier.connectors.interfaces.Bank;
import fr.univcotedazur.simpletcfs.customer.entities.Customer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;

@RestClientTest(Bank.class)
class BankProxyTest {

    private static final String CC_ROUTE = "/cctransactions";

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private BankProxy bankProxy;

    @Autowired
    private MockRestServiceServer mockServer;

    @Value("${bank.host.baseurl}")
    String bankHostandPort;

    @Test
    void payWithSuccess() throws Exception {
        // Given
        mockServer.expect(requestTo(bankHostandPort + CC_ROUTE))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withStatus(HttpStatus.CREATED)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(objectMapper.writeValueAsString(new PaymentReceiptDTO("654321", 100.0))));

        // When
        Optional<String> payReceiptId = bankProxy.pay(new Customer("nameIsNotImportant", "1234567890"), 100.0);
        // Then
        assertTrue(payReceiptId.isPresent());
        assertEquals("654321", payReceiptId.get());
        mockServer.verify(); // Verify that all expectations were met when calling the mocked url
    }

    @Test
    void payWithWrongStatusReturnedShouldReturnEmpty() throws Exception {
       // Given
        mockServer.expect(requestTo(bankHostandPort + CC_ROUTE))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withStatus(HttpStatus.OK) // should be CREATED
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(objectMapper.writeValueAsString(new PaymentReceiptDTO("654321", 100.0))));
        // When
        Optional<String> payReceiptId = bankProxy.pay(new Customer("nameIsNotImportant", "1234567890"), 100.0);
        // Then
        assertTrue(payReceiptId.isEmpty());
    }

    @Test
    void payWithEmptyBodyShouldReturnEmpty() {
       // Given
        mockServer.expect(requestTo(bankHostandPort + CC_ROUTE))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withStatus(HttpStatus.CREATED)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body("")); // Empty body
        // When
        Optional<String> payReceiptId = bankProxy.pay(new Customer("nameIsNotImportant", "1234567890"), 100.0);
        // Then
        assertTrue(payReceiptId.isEmpty());
    }

    @Test
    void payRejectedShouldReturnEmpty() {
       // Given
        mockServer.expect(requestTo(bankHostandPort + CC_ROUTE))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withStatus(HttpStatus.BAD_REQUEST));
        // When
        Optional<String> payReceiptId = bankProxy.pay(new Customer("nameIsNotImportant", "1234567890"), 100.0);
        // Then
        assertTrue(payReceiptId.isEmpty());
    }

    @ParameterizedTest
    @ValueSource(ints = {404, 409, 500})
    void payOnErrorStatusShouldReturnEmpty(int statusCode) {
        // Given
        mockServer.expect(requestTo(bankHostandPort + CC_ROUTE))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withStatus(HttpStatus.valueOf(statusCode)));
        // When
        Optional<String> payReceiptId = bankProxy.pay(new Customer("nameIsNotImportant", "1234567890"), 100.0);
        // Then
        assertTrue(payReceiptId.isEmpty());
    }

}
