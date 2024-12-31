package fr.univcotedazur.simpletcfs.cucumber.catalog;

import fr.univcotedazur.simpletcfs.entities.Cookies;
import fr.univcotedazur.simpletcfs.interfaces.CatalogExplorator;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class BrowsingCatalog {

    @Autowired
    private CatalogExplorator catalogExplorator;

    private Set<Cookies> cookiesSet;

    @When("one check the catalog contents")
    public void oneCheckTheCatalogContents() {
        cookiesSet = catalogExplorator.listPreMadeRecipes();
    }

    @Then("^there (?:is|are) (\\d+) items? in it$")
    public void thereAreItemsInIt(int itemsNb) {
        assertEquals(itemsNb, cookiesSet.size());
    }

}
