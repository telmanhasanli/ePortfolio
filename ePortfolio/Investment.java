package ePortfolio;

import java.util.Scanner;

/**
 * Superclass for investments, containing common attributes and methods for
 * stocks and mutual funds.
 */
public abstract class Investment {

    protected String symbol;
    protected String name;
    protected int quantity;
    protected double price;
    protected double bookValue;

    /**
     * Constructs a new Investment object with the specified attributes.
     *
     * @param symbol the symbol of the investment
     * @param name the name of the investment
     * @param quantity the quantity of the investment
     * @param price the current price of the investment
     * @param bookValue the total book value of the investment
     */
    public Investment(String symbol, String name, int quantity, double price, double bookValue) {
        validateSymbol(symbol);
        validateName(name);
        validateQuantity(quantity);
        validatePrice(price);

        this.symbol = symbol;
        this.name = name;
        this.quantity = quantity;
        this.price = price;
        this.bookValue = bookValue;
    }

    /**
     * Gets the symbol of the investment.
     *
     * @return the symbol of the investment
     */
    public String getSymbol() {
        return symbol;
    }

    /**
     * Sets the symbol of the investment after validation.
     *
     * @param symbol the new symbol of the investment
     */
    public void setSymbol(String symbol) {
        validateSymbol(symbol);
        this.symbol = symbol;
    }

    /**
     * Gets the name of the investment.
     *
     * @return the name of the investment
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of the investment after validation.
     *
     * @param name the new name of the investment
     */
    public void setName(String name) {
        validateName(name);
        this.name = name;
    }

    /**
     * Gets the quantity of the investment owned.
     *
     * @return the quantity of the investment
     */
    public int getQuantity() {
        return quantity;
    }

    /**
     * Sets the quantity of the investment after validation.
     *
     * @param quantity the new quantity of the investment
     */
    public void setQuantity(int quantity) {
        if (quantity < 0) {
            throw new IllegalArgumentException("Error: Quantity must be zero or greater.");
        }
        this.quantity = quantity;
    }

    /**
     * Gets the current price of the investment.
     *
     * @return the current price of the investment
     */
    public double getPrice() {
        return price;
    }

    /**
     * Sets the current price of the investment after validation.
     *
     * @param price the new price of the investment
     */
    public void setPrice(double price) {
        validatePrice(price);
        this.price = price;
    }

    public double getBookValue() {
        return bookValue;
    }

    /**
     * Sets the total book value of the investment.
     *
     * @param bookValue the new book value of the investment
     */
    public void setBookValue(double bookValue) {
        this.bookValue = bookValue;
    }

    /**
     * Returns a string representation of the investment, including its
     * attributes.
     *
     * @return a formatted string with investment details
     */
    @Override
    public String toString() {
        return "Name: " + name + ", Symbol: " + symbol + ", Quantity: " + quantity + ", Price: $" + price + ", BookValue: $" + bookValue;
    }

    /**
     * Validates the symbol of the investment.
     *
     * @param symbol the symbol to validate
     * @throws IllegalArgumentException if the symbol is null or empty
     */
    private void validateSymbol(String symbol) {
        if (symbol == null || symbol.trim().isEmpty()) {
            throw new IllegalArgumentException("Error: Symbol cannot be empty.");
        }
    }

    /**
     * Validates the name of the investment.
     *
     * @param name the name to validate
     * @throws IllegalArgumentException if the name is null or empty
     */
    private void validateName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Error: Name cannot be empty.");
        }
    }

    /**
     * Validates the quantity of the investment.
     *
     * @param quantity the quantity to validate
     * @throws IllegalArgumentException if the quantity is zero or negative
     */
    private void validateQuantity(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Error: Quantity must be greater than 0.");
        }
    }

    /**
     * Validates the price of the investment.
     *
     * @param price the price to validate
     * @throws IllegalArgumentException if the price is zero or negative
     */
    private void validatePrice(double price) {
        if (price <= 0) {
            throw new IllegalArgumentException("Error: Price must be greater than 0.");
        }
    }

    /**
     * Validates the type of the investment.
     *
     * @param type the type to validate (must be "stock" or "mutualfund")
     * @throws IllegalArgumentException if the type is invalid
     */
    public static void validateType(String type) {
        if (!type.equalsIgnoreCase("stock") && !type.equalsIgnoreCase("mutualfund")) {
            throw new IllegalArgumentException("Error: Invalid investment type. Please enter 'stock' or 'mutualfund'.");
        }
    }

    /**
     * Creates a new investment by prompting the user for input. Supports both
     * "stock" and "mutualfund" types.
     *
     * @param scanner the Scanner object for reading user input
     * @return the created Investment object
     */
    public static Investment createInvestment(Scanner scanner) {
        System.out.println("Enter type (stock/mutualfund): ");
        String type = scanner.nextLine().trim().toLowerCase();

        System.out.println("Enter symbol: ");
        String symbol = scanner.nextLine().trim();

        System.out.println("Enter name: ");
        String name = scanner.nextLine().trim();

        System.out.println("Enter quantity: ");
        int quantity = scanner.nextInt();

        System.out.println("Enter price: ");
        double price = scanner.nextDouble();
        scanner.nextLine();

        if (type.equals("stock")) {
            double bookValue = (quantity * price) + 9.99;
            return new Stock(symbol, name, quantity, price, bookValue);
        } else if (type.equals("mutualfund")) {
            double bookValue = quantity * price;
            return new MutualFund(symbol, name, quantity, price, bookValue);
        } else {
            System.out.println("Invalid type. Defaulting to Stock.");
            return new Stock(symbol, name, quantity, price, (quantity * price) + 9.99);
        }
    }
}
