package fr.univcotedazur.simpletcfs.entities;

public enum Cookies {

    CHOCOLALALA("Chocolalala", 1.30),
    DARK_TEMPTATION("Dark Temptation", 1.90),
    SOO_CHOCOLATE("Soo Chocolate", 1.25);

    private String fullName;
    private double price;

    Cookies(String fullName, double price) {
        this.fullName = fullName;
        this.price = price;
    }

    public double getPrice() {
        return price;
    }

    public String getFullName() {
        return fullName;
    }

}
