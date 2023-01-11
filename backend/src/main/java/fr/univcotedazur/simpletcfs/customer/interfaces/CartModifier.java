package fr.univcotedazur.simpletcfs.customer.interfaces;

import fr.univcotedazur.simpletcfs.customer.entities.Item;
import fr.univcotedazur.simpletcfs.customer.exceptions.CustomerIdNotFoundException;
import fr.univcotedazur.simpletcfs.customer.exceptions.NegativeQuantityException;

import java.util.Set;

public interface CartModifier {

    Item update(Long customerId, Item it) throws NegativeQuantityException, CustomerIdNotFoundException;

    Set<Item> cartContent(Long customerId) throws CustomerIdNotFoundException;

}
