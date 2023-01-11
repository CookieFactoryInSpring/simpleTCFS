package fr.univcotedazur.simpletcfs.cli.commands;

import fr.univcotedazur.simpletcfs.cli.model.CliOrder;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.web.client.RestClient;

import java.util.Set;

@ShellComponent
public class OrderTrackingCommands {

    public static final String BASE_URI = "/orders";

    private final RestClient restClient;

    public OrderTrackingCommands(RestClient restClient) {
        this.restClient = restClient;
    }

    @ShellMethod("Show all orders")
    public Set<CliOrder> orders() {
        return restClient.get()
                .uri(BASE_URI)
                .retrieve()
                .body(new org.springframework.core.ParameterizedTypeReference<Set<CliOrder>>() {});
    }

    @ShellMethod("Show order status by id (order-status ORDER_ID)")
    public String orderStatus(Long id) {
        return restClient.get()
                .uri(BASE_URI + "/" + id + "/status")
                .retrieve()
                .body(String.class);
    }

}
