package fr.univcotedazur.simpletcfs.cli.commands;

import fr.univcotedazur.simpletcfs.cli.CliContext;
import fr.univcotedazur.simpletcfs.cli.model.CartElement;
import fr.univcotedazur.simpletcfs.cli.model.CliOrder;
import fr.univcotedazur.simpletcfs.cli.model.CookieEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Set;

import static java.util.stream.Collectors.toSet;

@ShellComponent
public class CartCommands {

    public static final String BASE_URI = "/customers";

    private final WebClient webClient;

    private final CliContext cliContext;

    @Autowired
    public CartCommands(WebClient webClient, CliContext cliContext) {
        this.webClient = webClient;
        this.cliContext = cliContext;
    }

    @ShellMethod("Show cart content of customer (showcart CUSTOMER_NAME)")
    public Set<CartElement> showCart(String name) {
        // Spring-shell is catching exception (could be the case if name is not from a valid customer
        return webClient.get()
                .uri(getUriForCustomer(name))
                .retrieve()
                .bodyToFlux(CartElement.class)
                .collect(toSet())
                .block();
    }

    @ShellMethod("Add cookie to cart of customer (add-to-cart CUSTOMER_NAME COOKIE_NAME QUANTITY)")
    public CartElement addToCart(String name, CookieEnum cookie, int quantity) {
        // Spring-shell is catching exception (could be the case if name is not from a valid customer)
        return webClient.post()
                .uri(getUriForCustomer(name))
                .bodyValue(new CartElement(cookie, quantity))
                .retrieve()
                .bodyToMono(CartElement.class)
                .block();
    }

    @ShellMethod("Remove cookie from cart of customer (remove-from-cart CUSTOMER_NAME COOKIE_NAME QUANTITY)")
    public CartElement removeFromCart(String name, CookieEnum cookie, int quantity) {
        return addToCart(name, cookie, -quantity);
    }

    @ShellMethod("Validate cart of customer (validate CUSTOMER_NAME)")
    public CliOrder validateCart(String name) {
        return webClient.post()
                .uri(getUriForCustomer(name) + "/validate")
                .retrieve()
                .bodyToMono(CliOrder.class)
                .block();
    }

    private String getUriForCustomer(String name) {
        return BASE_URI + "/" + cliContext.getCustomers().get(name).getId() + "/cart";
    }

}
