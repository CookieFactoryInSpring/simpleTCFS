package fr.univcotedazur.simpletcfs.interfaces;

import fr.univcotedazur.simpletcfs.entities.Order;

public interface OrderModifier {

    Order orderIsNowInProgress(Order order);

    Order orderIsNowReady(Order order);
}
