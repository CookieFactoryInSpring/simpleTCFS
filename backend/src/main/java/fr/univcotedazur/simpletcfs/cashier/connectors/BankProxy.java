package fr.univcotedazur.simpletcfs.cashier.connectors;

import fr.univcotedazur.simpletcfs.cashier.connectors.externaldto.PaymentReceiptDTO;
import fr.univcotedazur.simpletcfs.cashier.connectors.externaldto.PaymentRequestDTO;
import fr.univcotedazur.simpletcfs.cashier.connectors.interfaces.Bank;
import fr.univcotedazur.simpletcfs.customer.entities.Customer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.Optional;

@Component
public class BankProxy implements Bank {

    private static final Logger LOG = LoggerFactory.getLogger(BankProxy.class);

    private final String bankHostandPort;

    private final RestClient restClient;

    public BankProxy(@Value("${bank.host.baseurl}") String bankHostandPort, RestClient.Builder restClientBuilder) {
        this.bankHostandPort = bankHostandPort;
        this.restClient = restClientBuilder.baseUrl(this.bankHostandPort).build();
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public Optional<String> pay(Customer customer, double value) {
        try {
            ResponseEntity<PaymentReceiptDTO> responseEntity = restClient.post()
                    .uri("/cctransactions")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(new PaymentRequestDTO(customer.getCreditCard(), value))
                    .retrieve()
                    .toEntity(PaymentReceiptDTO.class);
            if (responseEntity.getStatusCode() == HttpStatus.CREATED) {
                if (responseEntity.getBody() != null) {
                    return Optional.of(responseEntity.getBody().payReceiptId());
                } else {
                    LOG.warn("Payment not successful: status CREATED received, but empty body for customer", customer.getName());
                }
            } else {
                LOG.warn("Payment not successful: Unexpected response status " + responseEntity.getStatusCode());
            }
        } catch (RestClientException e) { // Catch Exceptions sent on 4XX and 5XX HTTP status codes
            LOG.warn("Payment not successful: Exception during REST call to Bank for customer" + customer.getName()
                    + " with exception " + e.getMessage());
        }
        return Optional.empty();
    }

}
