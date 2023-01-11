package fr.univcotedazur.simpletcfs.customer.interfaces;

import fr.univcotedazur.simpletcfs.customer.entities.Cookies;

import java.util.Set;

public interface CatalogExplorator {

    Set<Cookies> listPreMadeRecipes();

    Set<Cookies> exploreCatalogue(String regexp);

}

