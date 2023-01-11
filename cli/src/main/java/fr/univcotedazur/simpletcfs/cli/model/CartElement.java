package fr.univcotedazur.simpletcfs.cli.model;

public class CartElement {

    private CookieEnum cookie;
    private int quantity;

    public CookieEnum getCookie() {
        return cookie;
    }

    public void setCookie(CookieEnum cookie) {
        this.cookie = cookie;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public CartElement() {
    }

    public CartElement(CookieEnum cookie, int howMany) {
        this.cookie = cookie;
        this.quantity = howMany;
    }

    @Override
    public String toString() {
        return quantity + "x" + cookie.toString();
    }

}
