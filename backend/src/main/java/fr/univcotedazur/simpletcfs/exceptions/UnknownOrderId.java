package fr.univcotedazur.simpletcfs.exceptions;

import java.io.Serializable;

public class UnknownOrderId extends Exception {

    private Long orderId;

    public UnknownOrderId(Long id) {
        orderId = id;
    }

    public UnknownOrderId() {
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }
}