package fr.univcotedazur.simpletcfs.cli.commands;

import fr.univcotedazur.simpletcfs.cli.CliContext;
import fr.univcotedazur.simpletcfs.cli.model.CliCustomer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@ShellComponent
public class CustomerCommands {

    public static final String BASE_URI = "/customers";

    private final WebClient webClient;

    private final CliContext cliContext;

    @Autowired
    public CustomerCommands(WebClient webClient, CliContext cliContext) {
        this.webClient = webClient;
        this.cliContext = cliContext;
    }

    @ShellMethod("Register a customer in the CoD backend (register CUSTOMER_NAME CREDIT_CARD_NUMBER)")
    public CliCustomer register(String name, String creditCard) {
        CliCustomer res = webClient.post()
                .uri(BASE_URI)
                .bodyValue(new CliCustomer(name, creditCard))
                .retrieve()
                .bodyToMono(CliCustomer.class)
                .block();
        cliContext.getCustomers().put(Objects.requireNonNull(res).getName(), res);
        return res;
    }

    @ShellMethod("List all known customers")
    public String customers() {
        return cliContext.getCustomers().toString();
    }

    @ShellMethod("Update all known customers from server")
    public String updateCustomers() {
        Map<String, CliCustomer> customerMap = cliContext.getCustomers();
        customerMap.clear();
        CliCustomer[] customers = webClient
                .get()
                .uri(BASE_URI)
                .retrieve()
                .bodyToMono(CliCustomer[].class)
                .switchIfEmpty(Mono.just(new CliCustomer[0])) // Provide default empty array if response body is null
                .block();
        customerMap.putAll(Arrays.stream(customers)
                        .collect(Collectors.toMap(CliCustomer::getName, Function.identity()))
        );
        return customerMap.toString();
    }

}
