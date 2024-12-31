package fr.univcotedazur.simpletcfs.cli.commands;

import fr.univcotedazur.simpletcfs.cli.model.CliOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Set;

import static java.util.stream.Collectors.toSet;

@ShellComponent
public class OrderTrackingCommands {

    public static final String BASE_URI = "/orders";

    private final WebClient webClient;

    @Autowired
    public OrderTrackingCommands(WebClient webClient) {
        this.webClient = webClient;
    }

    @ShellMethod("Show all orders)")
    public Set<CliOrder> orders() {
        return webClient.get()
                .uri(BASE_URI)
                .retrieve()
                .bodyToFlux(CliOrder.class)
                .collect(toSet())
                .block();
    }

    @ShellMethod("Show order status by id (order-status ORDER_ID)")
    public String orderStatus(Long id) {
        return webClient.get()
                .uri(BASE_URI + "/" + id + "/status")
                .retrieve()
                .bodyToMono(String.class)
                .block();
    }

}
