package fr.univcotedazur.simpletcfs.exceptions;

import fr.univcotedazur.simpletcfs.entities.Cookies;

public class NegativeQuantityException extends Exception {

    private String name;
    private Cookies cookie;
    private int potentialQuantity;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Cookies getCookie() {
        return cookie;
    }

    public void setCookie(Cookies cookie) {
        this.cookie = cookie;
    }

    public int getPotentialQuantity() {
        return potentialQuantity;
    }

    public void setPotentialQuantity(int potentialQuantity) {
        this.potentialQuantity = potentialQuantity;
    }

    public NegativeQuantityException() {
    }

    public NegativeQuantityException(String name, Cookies cookie, int potentialQuantity) {
        this.name = name;
        this.cookie = cookie;
        this.potentialQuantity = potentialQuantity;
    }
}
