package fr.univcotedazur.simpletcfs.entities;

import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.NotNull;

import java.util.Objects;

@Embeddable
public class Item {

    @Enumerated(EnumType.STRING)
    @NotNull
    private Cookies cookie;

    @NotNull
    private int quantity;

    public Item() {}

    public Item(Cookies cookie, int quantity) {
        this.cookie = cookie;
        this.quantity = quantity;
    }

    public Cookies getCookie() {
        return cookie;
    }
    public void setCookie(Cookies cookie) {
        this.cookie = cookie;
    }

    public int getQuantity() {
        return quantity;
    }
    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    @Override
    public String toString() { return quantity + "x" + cookie.toString(); }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Item)) return false;
        Item item = (Item) o;
        return quantity == item.quantity && cookie == item.cookie;
    }

    @Override
    public int hashCode() {
        return Objects.hash(cookie, quantity);
    }
}
