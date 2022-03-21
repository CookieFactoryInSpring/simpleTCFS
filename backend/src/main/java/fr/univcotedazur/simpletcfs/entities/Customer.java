package fr.univcotedazur.simpletcfs.entities;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Entity
public class Customer {

    @Id
    @GeneratedValue
    private Long id;

    @NotBlank
    @Column(unique = true)
    private String name;

    @Pattern(regexp = "\\d{10}+", message = "Invalid creditCardNumber")
    private String creditCard;

    @OneToMany(cascade = {CascadeType.REMOVE}, fetch = FetchType.LAZY, mappedBy = "customer")
    private Set<Order> orders = new HashSet<>();

    @ElementCollection
    private Set<Item> cart = new HashSet<>();

    public Customer() {
    }

    public Customer(String n, String c) {
        this.name = n;
        this.creditCard = c;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCreditCard() {
        return creditCard;
    }

    public void setCreditCard(String creditCard) {
        this.creditCard = creditCard;
    }

    public void add(Order o) {
        this.orders.add(o);
    }

    public Set<Order> getOrders() {
        return orders;
    }

    public Set<Item> getCart() {
        return cart;
    }

    public void setCart(Set<Item> cart) {
        this.cart = cart;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Customer)) return false;
        Customer customer = (Customer) o;
        if (getName() != null ? !getName().equals(customer.getName()) : customer.getName() != null) return false;
        return getCreditCard() != null ? !getCreditCard().equals(customer.getCreditCard()) : customer.getCreditCard() != null;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, creditCard);
    }
}
