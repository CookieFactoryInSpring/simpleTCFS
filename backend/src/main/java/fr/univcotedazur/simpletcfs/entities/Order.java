package fr.univcotedazur.simpletcfs.entities;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
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

    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    public Order(Customer customer, Set<Item> items) {
        this.customer = customer;
        this.items = items;
        this.status = OrderStatus.VALIDATED;
    }

    public Order() {
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

    public double getPrice() {
        double result = 0.0;
        for (Item item : items) {
            result += (item.getQuantity() * item.getCookie().getPrice());
        }
        return result;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Order)) return false;
        Order order = (Order) o;
        if (getCustomer() != null ? !getCustomer().getName().equals(order.getCustomer().getName()) : order.getCustomer() != null)
            return false;
        if (getItems() != null ? !getItems().equals(order.getItems()) : order.getItems() != null) return false;
        return getStatus() == order.getStatus();
    }

    @Override
    public int hashCode() {
        return Objects.hash(customer, items, status);
    }
}
