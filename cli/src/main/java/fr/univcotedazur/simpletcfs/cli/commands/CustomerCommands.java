package fr.univcotedazur.simpletcfs.cli.commands;

import fr.univcotedazur.simpletcfs.cli.CliContext;
import fr.univcotedazur.simpletcfs.cli.model.CliCustomer;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.web.client.RestClient;

import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@ShellComponent
public class CustomerCommands {

    public static final String BASE_URI = "/customers";

    private final RestClient restClient;

    private final CliContext cliContext;

    public CustomerCommands(RestClient restClient, CliContext cliContext) {
        this.restClient = restClient;
        this.cliContext = cliContext;
    }

    @ShellMethod("Register a customer in the CoD backend (register CUSTOMER_NAME CREDIT_CARD_NUMBER)")
    public CliCustomer register(String name, String creditCard) {
        CliCustomer res = restClient.post()
                .uri(BASE_URI)
                .body(new CliCustomer(name, creditCard))
                .retrieve()
                .body(CliCustomer.class);
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
        CliCustomer[] customers = restClient
                .get()
                .uri(BASE_URI)
                .retrieve()
                .body(CliCustomer[].class);
        customerMap.putAll(Arrays.stream(customers)
                        .collect(Collectors.toMap(CliCustomer::getName, Function.identity()))
        );
        return customerMap.toString();
    }

}
