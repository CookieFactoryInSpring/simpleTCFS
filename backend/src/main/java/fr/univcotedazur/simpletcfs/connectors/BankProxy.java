package fr.univcotedazur.simpletcfs.connectors;

import fr.univcotedazur.simpletcfs.connectors.externaldto.PaymentReceiptDTO;
import fr.univcotedazur.simpletcfs.connectors.externaldto.PaymentRequestDTO;
import fr.univcotedazur.simpletcfs.entities.Customer;
import fr.univcotedazur.simpletcfs.interfaces.Bank;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Optional;

@Component
public class BankProxy implements Bank {

    private static final Logger LOG = LoggerFactory.getLogger(BankProxy.class);

    private final String bankHostandPort;

    private final WebClient webClient;

    @Autowired
    public BankProxy(@Value("${bank.host.baseurl}") String bankHostandPort) {
        this.bankHostandPort = bankHostandPort;
        this.webClient = WebClient.builder()
                .baseUrl(this.bankHostandPort)
                .build();
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public Optional<String> pay(Customer customer, double value) {
        return webClient.post()
                .uri("/cctransactions")
                .bodyValue(new PaymentRequestDTO(customer.getCreditCard(), value))
                .retrieve()
                .onStatus(HttpStatusCode::is2xxSuccessful,
                        clientResponse -> clientResponse.statusCode().equals(HttpStatus.CREATED) ?
                                Mono.empty() : // no Error raised, we let the flow continue
                                Mono.error(new EmptyResponseException("Unexpected status code: " + clientResponse.statusCode())))
                .onStatus(HttpStatusCode::isError,
                        clientResponse -> clientResponse.statusCode().equals(HttpStatus.BAD_REQUEST) ?
                                Mono.error(new EmptyResponseException("Unexpected status code: " + clientResponse.statusCode())) :
                                clientResponse.createException().flatMap(Mono::error)) // return an error with the predefined exception
                .bodyToMono(PaymentReceiptDTO.class)
                .switchIfEmpty(Mono.error(new EmptyResponseException("Empty response body from the bank")))
                .timeout(Duration.ofSeconds(5))
                .map(PaymentReceiptDTO::payReceiptId)
                .onErrorResume(error -> {
                    if (error instanceof EmptyResponseException) { // this is our exception, for cases where we cant to return an empty optional
                        LOG.warn("Unexpected behavior while processing payment (no exception thrown): {}", error.getMessage());
                        return Mono.empty();
                    }
                    return Mono.error(error); // other errors, we want to propagate
                })
                .blockOptional();
    }

    private static class EmptyResponseException extends RuntimeException {
        public EmptyResponseException(String message) {
            super(message);
        }
    }

}
