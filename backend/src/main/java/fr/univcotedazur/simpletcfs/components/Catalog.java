package fr.univcotedazur.simpletcfs.components;

import fr.univcotedazur.simpletcfs.entities.Cookies;
import fr.univcotedazur.simpletcfs.interfaces.CatalogExplorator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumSet;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class Catalog implements CatalogExplorator {

    @Override
    @Transactional(readOnly = true)
    public Set<Cookies> listPreMadeRecipes() {
        return EnumSet.allOf(Cookies.class);
    }

    @Override
    @Transactional(readOnly = true)
    public Set<Cookies> exploreCatalogue(String regexp) {
        return EnumSet.allOf(Cookies.class).stream().filter(cookie -> cookie.name().matches(regexp)).collect(Collectors.toSet());
    }

}
