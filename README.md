# ePortfolio
Student id: 1189193
Name: Telman Hasanli

## Compile and run:

navigate to thasanli_a3
javac ePortfolio/*.java
java ePortfolio.Portfolio investments.txt

Assignment 3: ePortfolio Application

## General Problem Statement

The purpose of this assignment is to develop a graphical user interface (GUI) for an "ePortfolio" investment management system.
The functionality is the same with previous assignment - A2 (buying/selling investments, updating prices, getting the total gain, and searching investments).
The new version features an interactive GUI using Java Swing. The program also incorporates exception handling to improve robustness and user-friendliness.

## Assumptions and Limitations

## Assumptions:

The user is familiar with basic investment concepts, such as stocks and mutual funds.
The application will run on a machine with a Java runtime environment installed.
The GUI will provide prompts and instructions to guide the user through various actions.
The application persists user data between sessions, meaning investments will be written to a file once the application is closed.

## Limitations:

No support for advanced investment types beyond stocks and mutual funds.
Limited to a single-user interface, without multi-user or concurrent access capabilities.

User Guide: Building and Running the Program
To use the ePortfolio application, follow these steps:

## Requirements:

Java Development Kit (JDK) version 8 or higher.
Ensure all files are in the correct directory structure as specified.

Upon starting the program, you will be presented with a welcome screen. Use the menu options to buy, sell, update, get total gains, or search investments.

## Using the GUI:

The Commands Menu allows you to select actions:
Buy: Purchase new investments or add to existing ones.
Sell: Sell some or all of an existing investment.
Update: Update the prices of existing investments.
Get Gain: Display the total gain for the portfolio.
Search: Search for investments using various criteria.

## Testing the Program

The program has been tested to ensure the correctness of various functionalities:

Buying and Selling Investments:
Different investments were added and sold to validate proper calculation of quantities and updates to the portfolio.

Price Update:
The price update functionality was tested by updating individual investments and ensuring they reflect the changes.

Exception Handling:
Input validation was tested by providing invalid data (e.g., negative quantities, non-numeric values) to ensure appropriate error messages are shown.

Search Functionality:
The search functionality was tested to ensure accurate retrieval of investments by validating symbols, keywords, price ranges, and their combinations, including partial, case-insensitive matches, and appropriate handling of no matches.


## Test Plan

Input Validation:
- Ensure that positive numbers are entered for quantity and price.
- Reject empty values for investment names and symbols.

Menu Options:
- Accept valid inputs like "5" and " 5 ".
- Reject invalid options such as "9" or non-numeric entries like "Buy".

Search Scenarios
- Symbol Search:
  - Search for symbols that exist in the portfolio.
  - Handle non-existent symbols.
  - Perform case-insensitive searches (e.g., "AAPL" and "aapl" should return the same result).
- Keyword Search:
  - Match a single keyword.
  - Match multiple keywords in any order.
  - Perform case-insensitive searches for keywords.
  - Handle cases with no matching investments.
- Price Range Search:
  - Test with both valid bounds for the range.
  - Handle scenarios where the minimum price is greater than the maximum price.
  - Handle cases where no investments are within the given range.
- Combination Searches:
  - Search by symbol and keywords: Ensure that both criteria are met.
  - Search by symbol and price range: Ensure that both criteria are met.
  - Search by keywords and price range: Ensure that both criteria are met.
  - Search by symbol, keywords, and price range: Ensure that all criteria are met.

## Other Operations
- Buy:
  - Add a new investment to the portfolio.
  - Update the quantity of an existing investment when the same symbol is provided.
- Sell:
  - Sell a valid quantity of an investment and calculate the correct gain.
  - Attempt to sell more than the available quantity and validate rejection.
  - Fully sell an investment and ensure that it is removed from the portfolio.
- Update Prices:
  - Allow only valid price inputs for each investment.
  - Handle invalid inputs, such as non-numeric or negative values, with appropriate error messages.
- Get Gain:
  - Accurately calculate the total gain after a sequence of buy and sell operations.

## Sample Test Cases

Buying Investments
- Input:
  - Type: Stock
  - Symbol: GOOG
  - Name: Google Inc.
  - Quantity: 100
  - Price: 800.0
- Output:
  - Added investment: Name: Google Inc., Symbol: GOOG, Quantity: 100, Price: $800.0, BookValue: $80000.00.

Selling Investments
- Input:
  - Symbol: AAPL
  - Quantity: 250
  - Price: 140.0
- Output:
  - You received $35,000.00 for selling 250 units of AAPL.
  - Gain from this sale: $7,500.00.


## Possible Improvements

Implementing Search Behaviour:
Add an ActionListener to the search button to trigger the search functionality when clicked.

Advanced Investment Types:
Add support for other types of investments, such as bonds or ETFs, to make the portfolio more versatile.

Improved User Interface:
Use modern GUI frameworks or libraries to make the interface more visually appealing and intuitive.

Multi-user Support:
Extend the application to support multiple users, each managing their individual portfolios.

Testing Automation:
Introduce unit tests using JUnit to automate testing of core functionalities, ensuring robustness during future development.
