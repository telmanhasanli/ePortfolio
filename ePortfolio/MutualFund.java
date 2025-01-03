package ePortfolio;

/**
 * MutualFund subclass extending Investment with specific attributes for mutual
 * funds.
 */
public class MutualFund extends Investment {

    public static final double MUTUAL_FUND_FEE = 45.00;

    public MutualFund(String symbol, String name, int quantity, double price, double bookValue) {
        super(symbol, name, quantity, price, bookValue);
    }
}
