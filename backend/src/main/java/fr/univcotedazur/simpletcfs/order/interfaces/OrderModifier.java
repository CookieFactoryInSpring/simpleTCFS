package fr.univcotedazur.simpletcfs.order.interfaces;

import fr.univcotedazur.simpletcfs.order.entities.Order;

public interface OrderModifier {

    Order orderIsNowInProgress(Order order);

    Order orderIsNowReady(Order order);
}
