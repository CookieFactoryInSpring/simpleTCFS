package fr.univcotedazur.simpletcfs.cli.commands;

import fr.univcotedazur.simpletcfs.cli.CliContext;
import fr.univcotedazur.simpletcfs.cli.model.CartElement;
import fr.univcotedazur.simpletcfs.cli.model.CliOrder;
import fr.univcotedazur.simpletcfs.cli.model.CookieEnum;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.web.client.RestClient;

import java.util.Set;

@ShellComponent
public class CartCommands {

    public static final String BASE_URI = "/customers";

    private final RestClient restClient;

    private final CliContext cliContext;

    public CartCommands(RestClient restClient, CliContext cliContext) {
        this.restClient = restClient;
        this.cliContext = cliContext;
    }

    @ShellMethod("Show cart content of customer (showcart CUSTOMER_NAME)")
    public Set<CartElement> showCart(String name) {
        // Spring-shell is catching exception (could be the case if name is not from a valid customer)
        return restClient.get()
                .uri(getUriForCustomer(name))
                .retrieve()
                .body(new ParameterizedTypeReference<Set<CartElement>>() {});
    }

    @ShellMethod("Add cookie to cart of customer (add-to-cart CUSTOMER_NAME COOKIE_NAME QUANTITY)")
    public CartElement addToCart(String name, CookieEnum cookie, int quantity) {
        // Spring-shell is catching exception (could be the case if name is not from a valid customer)
        return restClient.post()
                .uri(getUriForCustomer(name))
                .body(new CartElement(cookie, quantity))
                .retrieve()
                .body(CartElement.class);
    }

    @ShellMethod("Remove cookie from cart of customer (remove-from-cart CUSTOMER_NAME COOKIE_NAME QUANTITY)")
    public CartElement removeFromCart(String name, CookieEnum cookie, int quantity) {
        return addToCart(name, cookie, -quantity);
    }

    @ShellMethod("Validate cart of customer (validate CUSTOMER_NAME)")
    public CliOrder validateCart(String name) {
        return restClient.post()
                .uri(getUriForCustomer(name) + "/validate")
                .retrieve()
                .body(CliOrder.class);
    }

    private String getUriForCustomer(String name) {
        return BASE_URI + "/" + cliContext.getCustomers().get(name).getId() + "/cart";
    }

}
