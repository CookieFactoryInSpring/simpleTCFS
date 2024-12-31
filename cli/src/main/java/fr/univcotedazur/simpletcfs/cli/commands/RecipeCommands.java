package fr.univcotedazur.simpletcfs.cli.commands;

import fr.univcotedazur.simpletcfs.cli.model.CookieEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Set;

import static java.util.stream.Collectors.toSet;

@ShellComponent
public class RecipeCommands {

    private final WebClient webClient;

    @Autowired
    public RecipeCommands(WebClient webClient) {
        this.webClient = webClient;
    }

    @ShellMethod("List all available recipes")
    public Set<CookieEnum> recipes() {
        return webClient.get()
                .uri("/recipes")
                .retrieve()
                .bodyToFlux(CookieEnum.class)
                .collect(toSet())
                .block();
    }

}
