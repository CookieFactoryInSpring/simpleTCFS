package fr.univcotedazur.simpletcfs.interfaces;

import fr.univcotedazur.simpletcfs.entities.Order;

// very partial vision, the OrderStatus cannot properly make progress
// MVP just to set the order from VALIDATED to IN_PROGRESS
public interface OrderCooking {

    Order processInKitchen(Order order);

}
