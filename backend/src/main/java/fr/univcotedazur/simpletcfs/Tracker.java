package fr.univcotedazur.simpletcfs;

import fr.univcotedazur.simpletcfs.entities.OrderStatus;
import fr.univcotedazur.simpletcfs.exceptions.UnknownOrderId;

public interface Tracker {

    OrderStatus retrieveStatus(long orderId) throws UnknownOrderId;

}