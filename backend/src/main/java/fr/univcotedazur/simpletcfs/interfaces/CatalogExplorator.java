package fr.univcotedazur.simpletcfs.interfaces;

import fr.univcotedazur.simpletcfs.entities.Cookies;

import java.util.Set;

public interface CatalogExplorator {

    Set<Cookies> listPreMadeRecipes();

    Set<Cookies> exploreCatalogue(String regexp);

}

