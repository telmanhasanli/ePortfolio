package ePortfolio;

/**
 * Stock subclass extending Investment with specific attributes for stocks.
 */
public class Stock extends Investment {

    public static final double STOCK_FUND_FEE = 9.99;

    /**
     * Constructs a new Stock object with the specified attributes.
     *
     * @param symbol the symbol of the stock
     * @param name the name of the stock
     * @param quantity the quantity of the stock owned
     * @param price the current price of the stock
     * @param bookValue the total book value of the stock
     */
    public Stock(String symbol, String name, int quantity, double price, double bookValue) {
        super(symbol, name, quantity, price, bookValue);
    }
}
