package fr.univcotedazur.simpletcfs.exceptions;

public class EmptyCartException extends Exception {

    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public EmptyCartException() {
    }

    public EmptyCartException(String customerName) {
        this.name = customerName;
    }

}