package fr.univcotedazur.simpletcfs.components;

import fr.univcotedazur.simpletcfs.entities.Order;
import fr.univcotedazur.simpletcfs.interfaces.OrderCooking;
import fr.univcotedazur.simpletcfs.interfaces.OrderModifier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;


@Component
public class Kitchen implements OrderCooking {

    private final OrderModifier orderer;

    @Autowired
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
