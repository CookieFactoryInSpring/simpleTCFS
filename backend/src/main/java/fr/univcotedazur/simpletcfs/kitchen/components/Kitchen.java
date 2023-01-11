package fr.univcotedazur.simpletcfs.kitchen.components;

import fr.univcotedazur.simpletcfs.kitchen.interfaces.OrderCooking;
import fr.univcotedazur.simpletcfs.order.entities.Order;
import fr.univcotedazur.simpletcfs.order.interfaces.OrderModifier;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;


@Component
public class Kitchen implements OrderCooking {

    private final OrderModifier orderer;

    public Kitchen(OrderModifier orderModifier) {
        this.orderer = orderModifier;
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY) // must be called within a transaction
    public Order processInKitchen(Order order) {
        // MVP no business logic in the kitchen (could be the order display for chefs)
        return orderer.orderIsNowInProgress(order);
    }

}
