package fr.univcotedazur.simpletcfs.cli.commands;

import fr.univcotedazur.simpletcfs.cli.model.CookieEnum;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.web.client.RestClient;

import java.util.Set;

@ShellComponent
public class RecipeCommands {

    private final RestClient restClient;

    public RecipeCommands(RestClient restClient) {
        this.restClient = restClient;
    }

    @ShellMethod("List all available recipes")
    public Set<CookieEnum> recipes() {
        return restClient.get()
                .uri("/recipes")
                .retrieve()
                .body(new ParameterizedTypeReference<Set<CookieEnum>>() {});
    }

}
