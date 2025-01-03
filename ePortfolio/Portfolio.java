package ePortfolio;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;
import javax.swing.*;
import javax.swing.border.EmptyBorder;


/**
 * The Portfolio class manages a collection of investments, including stocks
 * and mutual funds. It provides functionality to load and save investments
 * from/to a file, search by keywords, and maintain a keyword index for
 * efficient searching.
 */
public class Portfolio {

    private ArrayList<Investment> investments;
    // HashMap to maintain keyword index for efficient searches
    private HashMap<String, ArrayList<Integer>> keywordIndex;
    private static JPanel welcomePanel;
    private static JPanel buyPanel;
    private static JPanel sellPanel;
    private static JPanel updatePanel;
    private static JPanel searchPanel;
    private static JPanel gainPanel;

    public ArrayList<Investment> getInvestments() {
        return investments;
    }

    /**
     * Constructor initializes an empty portfolio and keyword index.
     */
    public Portfolio() {
        investments = new ArrayList<>();
        keywordIndex = new HashMap<>();
    }

    /**
     * Loads investments from a file. If the file does not exist, it will be
     * created upon saving.
     *
     * @param filename the name of the file to load investments from
     */
    public void loadFromFile(String filename) {
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            String line;
            String type = null, symbol = null, name = null;
            int quantity = 0;
            double price = 0.0, bookValue = 0.0;

            //Read the file line by line and parse investment details
            while ((line = reader.readLine()) != null) {

                line = line.trim();
                line = line.replace("“", "\"").replace("”", "\"");

                // Check for end of an investment entry
                if (line.isEmpty()) {
                    if (type != null && symbol != null && name != null && quantity > 0 && price > 0) {
                        Investment investment = null;
                        if (type.equalsIgnoreCase("stock")) {
                            investment = new Stock(symbol, name, quantity, price, bookValue);
                        } else if (type.equalsIgnoreCase("mutualfund")) {
                            investment = new MutualFund(symbol, name, quantity, price, bookValue);
                        }
                        if (investment != null) {
                            investments.add(investment);
                            updateKeywordIndex(investment, investments.size() - 1);
                        }
                    } else {
                        System.out.println("Error: Incomplete or invalid investment entry. Skipping.\n");
                    }

                    // Reseting variables for the next investment
                    type = null;
                    symbol = null;
                    name = null;
                    quantity = 0;
                    price = 0.0;
                    bookValue = 0.0;
                    continue;
                }

                // Parsing each line to extract the details of an investment.
                if (line.startsWith("type")) {
                    type = line.split("=")[1].trim().replace("\"", "");
                } else if (line.startsWith("symbol")) {
                    symbol = line.split("=")[1].trim().replace("\"", "");
                } else if (line.startsWith("name")) {
                    name = line.split("=")[1].trim().replace("\"", "");
                } else if (line.startsWith("quantity")) {
                    quantity = Integer.parseInt(line.split("=")[1].trim().replace("\"", ""));
                } else if (line.startsWith("price")) {
                    price = Double.parseDouble(line.split("=")[1].trim().replace("\"", ""));
                } else if (line.startsWith("bookValue")) {
                    bookValue = Double.parseDouble(line.split("=")[1].trim().replace("\"", ""));
                }
            }

            // Add the last investment if the file does not end with an empty line
            if (type != null && symbol != null && name != null && quantity > 0 && price > 0) {
                Investment investment = null;
                if (type.equalsIgnoreCase("stock")) {
                    investment = new Stock(symbol, name, quantity, price, bookValue);
                } else if (type.equalsIgnoreCase("mutualfund")) {
                    investment = new MutualFund(symbol, name, quantity, price, bookValue);
                }
                if (investment != null) {
                    investments.add(investment);
                    updateKeywordIndex(investment, investments.size() - 1);
                }
            }

        } catch (FileNotFoundException e) {
            System.out.println("File not found. A new file will be created upon saving.\n");
        } catch (IOException e) {
            System.out.println("Error reading file: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("Error parsing file: " + e.getMessage());
        }
    }

    /**
     * Saves all investments to the specified file in a readable format.
     *
     * @param filename the name of the file to save investments to
     */
    public void saveToFile(String filename) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename))) {
            for (Investment investment : investments) {
                if (investment instanceof Stock) {
                    writer.write("type = \"stock\"\n");
                } else if (investment instanceof MutualFund) {
                    writer.write("type = \"mutualfund\"\n");
                }
                writer.write("symbol = \"" + investment.getSymbol() + "\"\n");
                writer.write("name = \"" + investment.getName() + "\"\n");
                writer.write("quantity = \"" + investment.getQuantity() + "\"\n");
                writer.write("price = \"" + investment.getPrice() + "\"\n");
                writer.write("bookValue = \"" + String.format("%.2f", investment.getBookValue()) + "\"\n");
                writer.write("\n");
            }
        } catch (IOException e) {
            System.out.println("Error saving to file: " + e.getMessage());
        }
    }

    /**
     * Adds an investment to the portfolio and updates the keyword index.
     *
     * @param investment the investment to add
     */
    public void addInvestment(Investment newInvestment) {
        boolean updated = false;

        // Checking if investment with the same symbol exists
        for (Investment investment : investments) {
            if (investment.getSymbol().equalsIgnoreCase(newInvestment.getSymbol())) {
                investment.setQuantity(investment.getQuantity() + newInvestment.getQuantity());
                investment.setBookValue(investment.getBookValue() + (newInvestment.getQuantity() * newInvestment.getPrice()));
                investment.setPrice(newInvestment.getPrice());
                updated = true;
                break;
            }
        }

        // Add new investment if not updated
        if (!updated) {
            investments.add(newInvestment);
            updateKeywordIndex(newInvestment, investments.size() - 1);
        }
    }

    /**
     * Sells a specified quantity of an investment based on its symbol at the
     * given price. If the quantity sold equals the total quantity, the
     * investment is removed from the portfolio. The method calculates the
     * payment, fees, and gain from the sale.
     *
     * @param symbol the symbol of the investment to sell
     * @param quantitySold the quantity of the investment to sell
     * @param sellPrice the price at which the investment is sold
     * @return a message describing the result of the sale, including the gain
     * or any errors
     */
    public String sellInvestment(String symbol, int quantitySold, double sellPrice) {
        Investment investment = null;
        String output = "";

        // Find the investment by symbol
        for (Investment inv : investments) {
            if (inv.getSymbol().equalsIgnoreCase(symbol)) {
                investment = inv;
                break;
            }
        }

        if (investment == null) {
            return "Error: No investment found with symbol '" + symbol + "'.";
        }

        if (quantitySold <= 0 || quantitySold > investment.getQuantity()) {
            return "Error: Invalid quantity to sell.";
        }

        double payment = quantitySold * sellPrice;
        double fee = (investment instanceof Stock) ? 9.99 : 45.00;
        double gain = (payment - fee) - (investment.getBookValue() * (quantitySold / (double) investment.getQuantity()));

        output += String.format("You received $%.2f for selling %d units of %s.\n", payment - fee, quantitySold, symbol);
        output += String.format("Gain from this sale: $%.2f.\n", gain);

        // Update the investment's quantity and book value
        int remainingQuantity = investment.getQuantity() - quantitySold;
        investment.setQuantity(remainingQuantity);

        // If the entire investment is sold, remove it and update the index
        if (remainingQuantity == 0) {
            investments.remove(investment);
            rebuildKeywordIndex();
            output += String.format("Investment with symbol '" + symbol + "' fully sold and removed from portfolio.");
            return output;
        } else /* if (remainingQuantity > 0) */ {
            double newBookValue = investment.getBookValue() * (remainingQuantity / (double) (remainingQuantity + quantitySold));
            investment.setBookValue(newBookValue);
        }
        return output;
    }

    /**
     * Updates the prices of all investments in the portfolio. Prompts the user
     * to enter a new price for each investment.
     *
     * @param scanner the Scanner object to read user input
     */
    public void updatePrices(Scanner scanner) {

        for (Investment investment : investments) {
            System.out.println("Enter new price for " + investment.getName() + " (" + investment.getSymbol() + "): ");
            double newPrice = scanner.nextDouble();
            investment.setPrice(newPrice);
        }
    }

    /**
     * Calculates the total gain of all investments in the portfolio. The gain
     * is calculated as (current value - book value) for each investment.
     *
     * @return the total gain of the portfolio
     */
    public double getTotalGain() {

        double totalGain = 0.0;
        // Iterate over all investments and calculate their individual gains
        for (Investment investment : investments) {
            totalGain += investment.getPrice() * investment.getQuantity() - investment.getBookValue();
        }
        return totalGain;
    }

    /**
     * Generates a summary of individual gains for each investment in the
     * portfolio. The gain is calculated as (current value - book value) for
     * each investment.
     *
     * @return a formatted string listing the individual gains for each
     * investment
     */
    public String getIndividualGains() {

        String output = "";
        // Iterate over all investments to calculate and append their gains
        for (Investment investment : investments) {
            double gain = investment.getPrice() * investment.getQuantity() - investment.getBookValue();
            output += String.format("%s (%s): $%.2f\n", investment.getName(), investment.getSymbol(), gain);
        }
        return output;
    }

    /**
     * Retrieves an investment from the portfolio by its symbol.
     *
     * @param symbol the symbol of the investment to find
     * @return the Investment object if found, or null if no matching investment
     * exists
     */
    public Investment getInvestmentBySymbol(String symbol) {
        // Iterate over all investments to find a match by symbol
        for (Investment investment : investments) {
            if (investment.getSymbol().equalsIgnoreCase(symbol)) {
                return investment;
            }
        }
        return null;
    }

    /**
     * Updates the keyword index for a new investment.
     *
     * @param investment the investment to index
     * @param index investment's index in the list
     */
    private void updateKeywordIndex(Investment investment, int index) {
        String[] nameKeywords = investment.getName().toLowerCase().split(" ");
        for (String keyword : nameKeywords) {
            if (!keyword.isEmpty()) {
                keywordIndex.computeIfAbsent(keyword, k -> new ArrayList<>()).add(index);
            }
        }

        String symbol = investment.getSymbol().toLowerCase();
        if (!symbol.isEmpty()) {
            keywordIndex.computeIfAbsent(symbol, k -> new ArrayList<>()).add(index);
        }
    }

    /**
     * Rebuilds the entire keyword index from the current list of investments.
     * This is used after major changes like deletions.
     */
    private void rebuildKeywordIndex() {
        keywordIndex.clear();
        for (int i = 0; i < investments.size(); i++) {
            updateKeywordIndex(investments.get(i), i);
        }
    }

    /**
     * Finds and returns an investment from the portfolio by its symbol.
     * Performs a case-insensitive search through the list of investments.
     *
     * @param symbol the symbol of the investment to find
     * @return the Investment object if found, or null if no matching investment
     * exists
     */
    public Investment findInvestmentBySymbol(String symbol) {
        // Check if the investment's symbol matches the given symbol (case-insensitive)
        for (Investment investment : investments) {
            if (investment.getSymbol().equalsIgnoreCase(symbol)) {
                return investment;
            }
        }
        return null;
    }

    /**
     * Updates the details of an existing investment by adding the specified
     * quantity and updating the price. The book value is recalculated based on
     * the new quantity and price.
     *
     * @param investment the existing Investment object to update
     * @param quantity the additional quantity to add to the investment
     * @param price the new price to update the investment with
     */
    public void updateExistingInvestment(Investment investment, int quantity, double price) {
        // Calculate the new total quantity by adding the additional quantity
        int newQuantity = investment.getQuantity() + quantity;
        double newBookValue = investment.getBookValue() + (quantity * price);
        investment.setQuantity(newQuantity);
        investment.setPrice(price);
        investment.setBookValue(newBookValue);
    }

    /**
     * Searches for investments containing the specified keyword in their name.
     *
     * @param keyword the keyword to search for
     * @return a list of matching investments
     */
    public ArrayList<Investment> search(String keywordString) {
        String[] keywords = keywordString.toLowerCase().split(" ");
        if (keywords.length == 0) {
            return new ArrayList<>();
        }

        ArrayList<Integer> intersection = new ArrayList<>(keywordIndex.getOrDefault(keywords[0], new ArrayList<>()));

        for (int i = 1; i < keywords.length; i++) {
            ArrayList<Integer> keywordList = keywordIndex.getOrDefault(keywords[i], new ArrayList<>());
            intersection.retainAll(keywordList);
        }

        ArrayList<Investment> result = new ArrayList<>();
        for (int index : intersection) {
            result.add(investments.get(index));
        }

        return result;
    }

    public static final int WIDTH = 600;
    public static final int HEIGHT = 400;
    private static int currentIndex = 0;
    private static int currentInvestment = 0;

    public static JTextField gainSymbolField;
    public static JTextArea gainMessagesArea;

    public static int getCurrentIndex() {
        return currentIndex;
    }

    public static void setCurrentIndex(int index) {
        currentIndex = index;
    }

    /**
     * Main method to run the Portfolio application. Loads investments from the
     * specified file and demonstrates adding, searching, and saving
     * functionality.
     *
     * @param args command-line arguments, where args[0] is the filename
     */
    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Please provide a filename as a command-line argument.\n");
            return;
        }

        String filename = args[0];
        Portfolio portfolio = new Portfolio();
        portfolio.loadFromFile(filename);
        Scanner scanner = new Scanner(System.in);

        JFrame window = new JFrame();
        window.setSize(WIDTH, HEIGHT);
        window.setTitle("ePortfolio");
        window.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        window.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                System.out.println("Program is closing...\n");
                System.out.println("Saving portfolio to the file...\n");
                portfolio.saveToFile(filename);
                window.dispose();
            }
        });
        window.setLayout(new BorderLayout());

        JMenuBar menuBar = new JMenuBar();
        JMenu commandsMenu = new JMenu("Commands");
            
        JMenuItem buyItem = new JMenuItem("Buy");
        buyItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                welcomePanel.setVisible(false);
                buyPanel.setVisible(true);
                sellPanel.setVisible(false);
                updatePanel.setVisible(false);
                searchPanel.setVisible(false);
                gainPanel.setVisible(false);
                window.validate();
            }
        });
        commandsMenu.add(buyItem);
        
        JMenuItem sellItem = new JMenuItem("Sell");
        sellItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                welcomePanel.setVisible(false);
                buyPanel.setVisible(false);
                sellPanel.setVisible(true);
                updatePanel.setVisible(false);
                searchPanel.setVisible(false);
                gainPanel.setVisible(false);
                window.validate();
            }
        });
        commandsMenu.add(sellItem);

        JMenuItem updateItem = new JMenuItem("Update");
        updateItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                welcomePanel.setVisible(false);
                buyPanel.setVisible(false);
                sellPanel.setVisible(false);
                updatePanel.setVisible(true);
                searchPanel.setVisible(false);
                gainPanel.setVisible(false);
                window.validate();
            }
        });
        commandsMenu.add(updateItem);

        JMenuItem searchItem = new JMenuItem("Search");
        searchItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                welcomePanel.setVisible(false);
                buyPanel.setVisible(false);
                sellPanel.setVisible(false);
                updatePanel.setVisible(false);
                searchPanel.setVisible(true);
                gainPanel.setVisible(false);
                window.validate();
            }
        });
        commandsMenu.add(searchItem);

        JMenuItem gainItem = new JMenuItem("Get Gain");
        gainItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                welcomePanel.setVisible(false);
                buyPanel.setVisible(false);
                sellPanel.setVisible(false);
                updatePanel.setVisible(false);
                searchPanel.setVisible(false);
                gainPanel.setVisible(true);
                window.validate();

                double totalGain = portfolio.getTotalGain();
                gainSymbolField.setText(Double.toString(totalGain));
                gainMessagesArea.setText(portfolio.getIndividualGains()); 
            }
        });
        commandsMenu.add(gainItem);

        JMenuItem quitItem = new JMenuItem("Quit");
        quitItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.out.println("Saving portfolio and exiting...\n");
                portfolio.saveToFile(filename);
                System.exit(0);
            }
        });
        commandsMenu.add(quitItem);
        menuBar.add(commandsMenu);

        JTextArea textArea = new JTextArea("Welcome to ePortfolio!\n\n\n\n\nChoose a command from the “Commands” menu to buy or sell an investment, update prices for all investments, get gain for the portfolio, search for relevant investments, or quit the program.");
        textArea.setEditable(false); 
        textArea.setFont(new Font("Arial", Font.PLAIN, 16));
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setAlignmentX(Component.CENTER_ALIGNMENT);
        textArea.setBorder(new EmptyBorder(80, 20, 20, 20));

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));

        welcomePanel = new JPanel(new BorderLayout());
        welcomePanel.add(textArea, BorderLayout.CENTER);


        // <<<<< BUY PANEL >>>>>
        buyPanel = new JPanel();
        buyPanel.setBorder(BorderFactory.createTitledBorder("Buying an investment"));
        buyPanel.setLayout(new BoxLayout(buyPanel, BoxLayout.Y_AXIS));
        JPanel buyPanelTop = new JPanel(new BorderLayout());
        
        JPanel buyInputPanel = new JPanel(new GridLayout(5, 2, 5, 5));
        buyInputPanel.add(new JLabel("Type:"));
        JComboBox<String> typeDropdown = new JComboBox<>(new String[]{"stock", "mutualfund"});
        buyInputPanel.add(typeDropdown);
        buyInputPanel.add(new JLabel("Symbol:"));
        JTextField symbolField = new JTextField();
        buyInputPanel.add(symbolField);
        buyInputPanel.add(new JLabel("Name:"));
        JTextField nameField = new JTextField();
        buyInputPanel.add(nameField);
        buyInputPanel.add(new JLabel("Quantity:"));
        JTextField quantityField = new JTextField();
        buyInputPanel.add(quantityField);
        buyInputPanel.add(new JLabel("Price:"));
        JTextField priceField = new JTextField();
        buyInputPanel.add(priceField);

        JPanel buyButtonPanel = new JPanel();
        buyButtonPanel.setLayout(new BoxLayout(buyButtonPanel, BoxLayout.Y_AXIS));
        JButton resetButton = new JButton("Reset");
        JButton buyButton = new JButton("Buy");
        resetButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        buyButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        buyButtonPanel.add(Box.createVerticalGlue()); 
        buyButtonPanel.add(resetButton);
        buyButtonPanel.add(Box.createRigidArea(new Dimension(0, 10))); 
        buyButtonPanel.add(buyButton);
        buyButtonPanel.add(Box.createVerticalGlue()); 

        JPanel buyTextPanel = new JPanel(new BorderLayout());
        buyTextPanel.setBorder(BorderFactory.createTitledBorder("Messages"));
        JTextArea messagesArea = new JTextArea();
        messagesArea.setEditable(false); 
        JScrollPane scrollPane = new JScrollPane(messagesArea, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        buyTextPanel.add(scrollPane, BorderLayout.CENTER);

        buyPanelTop.add(buyInputPanel, BorderLayout.CENTER);
        buyPanelTop.add(buyButtonPanel, BorderLayout.EAST);
        buyPanel.add(buyPanelTop);
        buyPanel.add(buyTextPanel);


        // ActionListener for the "Buy" button
        buyButton.addActionListener(e -> {
            String type = (String) typeDropdown.getSelectedItem(); 
            String symbol = symbolField.getText().trim();
            String name = nameField.getText().trim();
            String quantity = quantityField.getText().trim();
            String price = priceField.getText().trim();

            // Validate inputs
            if (symbol.isEmpty() || name.isEmpty() || quantity.isEmpty() || price.isEmpty()) {
                messagesArea.append("Error: All fields must be filled!\n");
            } else {
                try {
                    int quantityInt = Integer.parseInt(quantity);
                    double priceDouble = Double.parseDouble(price);
                    if(quantityInt > 0 && priceDouble > 0){
                        Investment existingInvestment = portfolio.findInvestmentBySymbol(symbol);
                        if (existingInvestment != null) {
                            boolean isTypeMatch = (existingInvestment instanceof Stock && type.equals("stock"))
                                    || (existingInvestment instanceof MutualFund && type.equals("mutualfund"));
                            if (isTypeMatch) {
                                // Update existing investment
                                messagesArea.setText("");
                                messagesArea.append("Symbol found. Updating the existing investment...\n");
                                portfolio.updateExistingInvestment(existingInvestment, quantityInt, priceDouble);
                                messagesArea.append("Existing investment updated successfully!\n");
                            } else {
                                messagesArea.append("Error: The type of the existing investment does not match the provided type.\n");
                            }
                        } else {
                            // Add a new investment
                            Investment newInvestment;
                            if (type.equals("stock")) {
                                newInvestment = new Stock(symbol, name, quantityInt, priceDouble, (quantityInt * priceDouble) + 9.99);
                            } else /* if (type.equals("mutualfund")) */ {
                                newInvestment = new MutualFund(symbol, name, quantityInt, priceDouble, quantityInt * priceDouble);
                            }
                            portfolio.addInvestment(newInvestment);
                            messagesArea.setText("");
                            messagesArea.append("Investment added successfully!\n");
                        }
                    } else {
                        if(quantityInt <= 0){
                            messagesArea.append("Error: Quantity must be greater than 0.\n");
                        } 
                        if(priceDouble <= 0){
                            messagesArea.append("Error: Price must be greater than 0.\n");
                        }
                    }
                } catch (NumberFormatException ex) {
                    messagesArea.append("Error: Quantity and price must be valid numbers.\n");
                }
            }
        });

        // ActionListener for the "Reset" button
        resetButton.addActionListener(e -> {
            symbolField.setText("");          
            nameField.setText("");            
            quantityField.setText("");       
            priceField.setText("");           
            messagesArea.setText("");
        });


        // <<<<< SELL PANEL >>>>>
        sellPanel = new JPanel();
        sellPanel.setBorder(BorderFactory.createTitledBorder("Selling an investment"));
        sellPanel.setLayout(new BoxLayout(sellPanel, BoxLayout.Y_AXIS));
        JPanel sellPanelTop = new JPanel(new BorderLayout());
        
        JPanel sellInputPanel = new JPanel(new GridLayout(3, 2, 5, 5));
        sellInputPanel.add(new JLabel("Symbol:"));
        JTextField sellSymbolField = new JTextField();
        sellInputPanel.add(sellSymbolField);
        sellInputPanel.add(new JLabel("Quantity:"));
        JTextField sellQuantityField = new JTextField();
        sellInputPanel.add(sellQuantityField);
        sellInputPanel.add(new JLabel("Price:"));
        JTextField sellPriceField = new JTextField();
        sellInputPanel.add(sellPriceField);

        JPanel sellButtonPanel = new JPanel();
        sellButtonPanel.setLayout(new BoxLayout(sellButtonPanel, BoxLayout.Y_AXIS));
        JButton sellResetButton = new JButton("Reset");
        JButton sellButton = new JButton("Sell");
        sellResetButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        sellButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        sellButtonPanel.add(Box.createVerticalGlue()); 
        sellButtonPanel.add(sellResetButton);
        sellButtonPanel.add(Box.createRigidArea(new Dimension(0, 10))); 
        sellButtonPanel.add(sellButton);
        sellButtonPanel.add(Box.createVerticalGlue()); 

        JPanel sellTextPanel = new JPanel(new BorderLayout());
        sellTextPanel.setBorder(BorderFactory.createTitledBorder("Messages"));
        JTextArea sellMessagesArea = new JTextArea();
        sellMessagesArea.setEditable(false); 
        JScrollPane sellScrollPane = new JScrollPane(sellMessagesArea, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        sellTextPanel.add(sellScrollPane, BorderLayout.CENTER);

        sellPanelTop.add(sellInputPanel, BorderLayout.CENTER);
        sellPanelTop.add(sellButtonPanel, BorderLayout.EAST);
        sellPanel.add(sellPanelTop);
        sellPanel.add(sellTextPanel);

        // ActionListener for the "Sell" button
        sellButton.addActionListener(e -> {
            String symbol = sellSymbolField.getText().trim();
            String quantity = sellQuantityField.getText().trim();
            String price = sellPriceField.getText().trim();

            // Validate inputs
            if (symbol.isEmpty() || quantity.isEmpty() || price.isEmpty()) {
                sellMessagesArea.append("Error: All fields must be filled!\n");
            } else {
                try {
                    int quantitySold = Integer.parseInt(quantity);
                    double sellPrice = Double.parseDouble(price);
                    if(quantitySold > 0 && sellPrice > 0){
                        String sellInfo = portfolio.sellInvestment(symbol, quantitySold, sellPrice);
                        sellMessagesArea.setText("");
                        sellMessagesArea.append(sellInfo);
                    } else {
                        if(quantitySold <= 0){
                            sellMessagesArea.append("Error: Quantity must be greater than 0.\n");
                        } 
                        if(sellPrice <= 0){
                            sellMessagesArea.append("Error: Price must be greater than 0.\n");
                        }
                    }
                } catch (NumberFormatException ex) {
                    sellMessagesArea.append("Error: Quantity and price must be valid numbers.\n");
                }
            }
        });


        // ActionListener for the "Reset" button
        sellResetButton.addActionListener(e -> {
            sellSymbolField.setText("");          
            sellQuantityField.setText("");       
            sellPriceField.setText("");           
            sellMessagesArea.setText("");
        });

        // <<<<< UPDATE PANEL >>>>>
        updatePanel = new JPanel();
        updatePanel.setBorder(BorderFactory.createTitledBorder("Updating investments"));
        updatePanel.setLayout(new BoxLayout(updatePanel, BoxLayout.Y_AXIS));
        JPanel updatePanelTop = new JPanel(new BorderLayout());
        
        JPanel updateInputPanel = new JPanel(new GridLayout(3, 2, 5, 5));
        updateInputPanel.add(new JLabel("Symbol:"));
        JTextField updateSymbolField = new JTextField();
        updateSymbolField.setEditable(false);
        updateInputPanel.add(updateSymbolField);
        updateInputPanel.add(new JLabel("Name:"));
        JTextField updateQuantityField = new JTextField();
        updateQuantityField.setEditable(false);
        updateInputPanel.add(updateQuantityField);
        updateInputPanel.add(new JLabel("Price:"));
        JTextField updatePriceField = new JTextField();
        updateInputPanel.add(updatePriceField);

        JPanel updateButtonPanel = new JPanel();
        updateButtonPanel.setLayout(new BoxLayout(updateButtonPanel, BoxLayout.Y_AXIS));
        JButton updatePrevButton = new JButton("Prev");
        JButton updateNextButton = new JButton("Next");
        JButton updateSaveButton = new JButton("Save");
        updatePrevButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        updateNextButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        updateSaveButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        updateButtonPanel.add(Box.createVerticalGlue()); 
        updateButtonPanel.add(updatePrevButton);
        updateButtonPanel.add(Box.createRigidArea(new Dimension(0, 10))); 
        updateButtonPanel.add(updateNextButton);
        updateButtonPanel.add(Box.createVerticalGlue()); 
        updateButtonPanel.add(updateSaveButton);
        updateButtonPanel.add(Box.createVerticalGlue()); 

        JPanel updateTextPanel = new JPanel(new BorderLayout());
        updateTextPanel.setBorder(BorderFactory.createTitledBorder("Messages"));
        JTextArea updateMessagesArea = new JTextArea();
        updateMessagesArea.setEditable(false);
        JScrollPane updateScrollPane = new JScrollPane(updateMessagesArea, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        updateTextPanel.add(updateScrollPane, BorderLayout.CENTER);

        updatePanelTop.add(updateInputPanel, BorderLayout.CENTER);
        updatePanelTop.add(updateButtonPanel, BorderLayout.EAST);
        updatePanel.add(updatePanelTop);
        updatePanel.add(updateTextPanel);

        Investment inv = portfolio.getInvestments().get(getCurrentIndex());
        updateSymbolField.setText(inv.getSymbol());
        updateQuantityField.setText(Integer.toString(inv.getQuantity()));
        updatePriceField.setText(Double.toString(inv.getPrice()));


        updateNextButton.addActionListener(e -> {
            int length = portfolio.getInvestments().size();
            setCurrentIndex((getCurrentIndex() + 1) % length);
            Investment invest = portfolio.getInvestments().get(getCurrentIndex());
            updateSymbolField.setText(invest.getSymbol());
            updateQuantityField.setText(Integer.toString(invest.getQuantity()));
            updatePriceField.setText(Double.toString(invest.getPrice()));
        });
        updatePrevButton.addActionListener(e -> {
            int length = portfolio.getInvestments().size();
            setCurrentIndex((getCurrentIndex() - 1 + length) % length);
            Investment invest = portfolio.getInvestments().get(getCurrentIndex());
            updateSymbolField.setText(invest.getSymbol());
            updateQuantityField.setText(Integer.toString(invest.getQuantity()));
            updatePriceField.setText(Double.toString(invest.getPrice()));
        });

        updateSaveButton.addActionListener(e -> {
            String price = updatePriceField.getText().trim();
            if (price.isEmpty()) {
                updateMessagesArea.append("Error: Price cannot be empty!\n");
            } else {
                try {
                    double updatePrice = Double.parseDouble(price);
                    if(updatePrice <= 0){
                        updateMessagesArea.append("Error: Price must be greater than 0.\n");
                    } else {
                        Investment invest = portfolio.getInvestments().get(getCurrentIndex());
                        invest.setPrice(updatePrice);
                        updateMessagesArea.setText("Investment price updated successfully!\n");
                    }
                } catch (NumberFormatException ex) {
                    updateMessagesArea.append("Error: Price must be a valid number.\n");
                }
            }
        });
           
        // <<<<< SEARCH PANEL >>>>>
        searchPanel = new JPanel();
        searchPanel.setBorder(BorderFactory.createTitledBorder("Searching investments"));
        searchPanel.setLayout(new BoxLayout(searchPanel, BoxLayout.Y_AXIS));
        JPanel searchPanelTop = new JPanel(new BorderLayout());
        
        JPanel searchInputPanel = new JPanel(new GridLayout(4, 2, 5, 5));
        searchInputPanel.add(new JLabel("Symbol:"));
        JTextField searchSymbolField = new JTextField();
        searchInputPanel.add(searchSymbolField);
        searchInputPanel.add(new JLabel("Name Keywords:"));
        JTextField searchQuantityField = new JTextField();
        searchInputPanel.add(searchQuantityField);
        searchInputPanel.add(new JLabel("Low Price:"));
        JTextField searchLowPriceField = new JTextField();
        searchInputPanel.add(searchLowPriceField);
        searchInputPanel.add(new JLabel("High Price:"));
        JTextField searchHighPriceField = new JTextField();
        searchInputPanel.add(searchHighPriceField);

        JPanel searchButtonPanel = new JPanel();
        searchButtonPanel.setLayout(new BoxLayout(searchButtonPanel, BoxLayout.Y_AXIS));
        JButton searchResetButton = new JButton("Reset");
        JButton searchButton = new JButton("Search");
        searchResetButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        searchButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        searchButtonPanel.add(Box.createVerticalGlue()); 
        searchButtonPanel.add(searchResetButton);
        searchButtonPanel.add(Box.createRigidArea(new Dimension(0, 10))); 
        searchButtonPanel.add(searchButton);
        searchButtonPanel.add(Box.createVerticalGlue()); 

        JPanel searchTextPanel = new JPanel(new BorderLayout());
        searchTextPanel.setBorder(BorderFactory.createTitledBorder("Search results"));
        JTextArea searchMessagesArea = new JTextArea();
        searchMessagesArea.setEditable(false);
        JScrollPane searchScrollPane = new JScrollPane(searchMessagesArea, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        searchTextPanel.add(searchScrollPane, BorderLayout.CENTER);

        searchPanelTop.add(searchInputPanel, BorderLayout.CENTER);
        searchPanelTop.add(searchButtonPanel, BorderLayout.EAST);
        searchPanel.add(searchPanelTop);
        searchPanel.add(searchTextPanel);

        // ActionListener for the "Search" button
        searchButton.addActionListener(e -> {
            String symbol = searchSymbolField.getText().trim();
            String keywords = searchQuantityField.getText().trim();
            String lowPrice = searchLowPriceField.getText().trim();
            String highPrice = searchHighPriceField.getText().trim();

            // Validate inputs
            if (symbol.isEmpty() || keywords.isEmpty() || lowPrice.isEmpty() || highPrice.isEmpty()) {
                searchMessagesArea.append("Error: All fields must be filled!\n");
            } else {
                try {
                    double lowSearchPrice = Double.parseDouble(lowPrice);
                    double highSearchPrice = Double.parseDouble(highPrice);
                    if (lowSearchPrice < 0 || highSearchPrice < 0) {
                        searchMessagesArea.append("Error: Search prices must be positive numbers.\n");
                    } else if (lowSearchPrice > highSearchPrice) {
                        searchMessagesArea.append("Error: Minimum price cannot be greater than maximum price.\n");
                    } else {
                        ArrayList<Investment> results = portfolio.search(keywords);
                        boolean found = false;
                        searchMessagesArea.setText("");
                        // ...
                        for (Investment investment : results) {
                            if (investment.getSymbol().equalsIgnoreCase(symbol)
                                    && investment.getPrice() >= lowSearchPrice
                                    && investment.getPrice() <= highSearchPrice) {
                                searchMessagesArea.append(investment.toString());
                                found = true;
                            }
                        }
                        if (!found) {
                            searchMessagesArea.append("No investments found matching symbol: " + symbol + ", keywords: " + keywords + " and price range: $" + lowSearchPrice + " - $" + highSearchPrice + "\n");
                        }
                    }
                } catch (NumberFormatException ex) {
                    searchMessagesArea.append("Error: Low price and High price must be valid numbers.\n");
                }
            }
        });

        // ActionListener for the "Reset" button
        searchResetButton.addActionListener(e -> {
            searchSymbolField.setText("");
            searchQuantityField.setText("");
            searchLowPriceField.setText("");
            searchHighPriceField.setText("");
            searchMessagesArea.setText("");
        });

        // <<<<< GAIN PANEL >>>>>
        gainPanel = new JPanel();
        gainPanel.setBorder(BorderFactory.createTitledBorder("Getting total gain"));
        gainPanel.setLayout(new BoxLayout(gainPanel, BoxLayout.Y_AXIS));
        JPanel gainPanelTop = new JPanel(new BorderLayout());
        
        JPanel gainInputPanel = new JPanel(new GridLayout(1, 2, 5, 5));
        gainInputPanel.add(new JLabel("Total Gain:"));
        gainSymbolField = new JTextField();
        gainSymbolField.setEditable(false);
        gainInputPanel.add(gainSymbolField);

        JPanel gainTextPanel = new JPanel(new BorderLayout());
        gainTextPanel.setBorder(BorderFactory.createTitledBorder("Individual gains"));
        gainMessagesArea = new JTextArea();
        gainMessagesArea.setEditable(false);
        JScrollPane gainScrollPane = new JScrollPane(gainMessagesArea, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        gainTextPanel.add(gainScrollPane, BorderLayout.CENTER);

        gainPanelTop.add(gainInputPanel, BorderLayout.NORTH);
        gainPanel.add(gainPanelTop);
        gainPanel.add(gainTextPanel);

        double totalGain = portfolio.getTotalGain();
        gainSymbolField.setText(Double.toString(totalGain));
        gainMessagesArea.setText(portfolio.getIndividualGains()); 
        

        // <<<<< MAIN PANEL >>>>>
        mainPanel.add(welcomePanel);
        mainPanel.add(buyPanel);
        mainPanel.add(sellPanel);
        mainPanel.add(updatePanel);
        mainPanel.add(searchPanel);
        mainPanel.add(gainPanel);

        window.setJMenuBar(menuBar);
        window.add(mainPanel);

        welcomePanel.setVisible(true);
        buyPanel.setVisible(false);
        sellPanel.setVisible(false);
        updatePanel.setVisible(false);
        searchPanel.setVisible(false);
        gainPanel.setVisible(false);
        
        window.validate();
        window.setVisible(true);
    }
}


