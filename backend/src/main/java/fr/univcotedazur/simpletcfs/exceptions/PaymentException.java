package fr.univcotedazur.simpletcfs.exceptions;

public class PaymentException extends Exception {

    private String name;
    private double amount;

    public PaymentException(String customerName, double amount) {
        this.name = customerName;
        this.amount = amount;
    }

    public PaymentException() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

}
