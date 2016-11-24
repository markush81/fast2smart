package net.fast2smart.legacy.model;

/**
 * Created by markus on 22/10/2016.
 */
public enum Partner {

    BOOKS("Online Bookstore"),
    ECOMMERCE("eCommerce Store"),
    SUPERMARKET("Local Supermarket"),
    HOLIDAY("Holiday Booking Platform");

    private String displayName;

    Partner(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
