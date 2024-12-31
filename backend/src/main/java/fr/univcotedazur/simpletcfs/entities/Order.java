package fr.univcotedazur.simpletcfs.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(name= "orders")
public class Order {

    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne
    @NotNull
    private Customer customer;

    @ElementCollection
    private Set<Item> items;

    @Positive
    private double price;

    @NotBlank
    private String payReceiptId;

    @Enumerated(EnumType.STRING)
    @NotNull
    private OrderStatus status;

    public Order(Customer customer, Set<Item> items, double price, String payReceiptId) {
        this.customer = customer;
        this.items = new HashSet<>(items);
        this.price = price;
        this.payReceiptId = payReceiptId;
        this.status = OrderStatus.VALIDATED;
        customer.addOrder(this);
    }

    public Order() {
    }

    public double getPrice() {
        return price;
    }

    public String getPayReceiptId() {
        return payReceiptId;
    }

    public void setPayReceiptId(String payReceiptId) {
        this.payReceiptId = payReceiptId;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public Customer getCustomer() {
        return customer;
    }

    public Set<Item> getItems() {
        return items;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Order order)) return false;
        return Objects.equals(customer, order.customer) && Objects.equals(items, order.items) && Objects.equals(price, order.price) && status == order.status;

    }

    @Override
    public int hashCode() {
        return Objects.hash(customer, items, status);
    }
}
