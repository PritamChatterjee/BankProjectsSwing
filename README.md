Overview

This is a Java Swing-based desktop application that simulates a banking system. It allows users to manage a savings account, request loans (Personal, Home, or Car), pay EMIs, apply savings interest, view transaction history, log transactions to a file, and print bank statements. The application features a professional GUI with a clean layout, multithreading for responsive operations, and file-based logging for transaction records.

Features





Account Management:





View savings balance, loan amount, EMI status, and loan eligibility.



Deposit and withdraw funds from the savings account.



Loan Management:





Request one of three loan types:





Personal Loan: 10% annual interest, 12-month tenure.



Home Loan: 7% annual interest, 60-month tenure.



Car Loan: 8% annual interest, 36-month tenure.



Pay monthly EMIs for active loans.



Loan eligibility is 5x the current savings balance.



Only one loan can be active at a time.



Savings Interest:





Apply 4% annual interest (credited monthly) to the savings balance.



Transaction History:





Display all transactions (deposits, withdrawals, loans, EMI payments, interest) in a table.



Logging:





Save all transaction logs with timestamps to transaction_log.txt.



Printing:





Print a bank statement with account details and transaction history.



Responsive UI:





Multithreaded operations using ExecutorService for smooth performance.



Thread-safe updates with synchronized blocks.



Clean, professional layout with aligned components and consistent styling.

Prerequisites





Java Development Kit (JDK): Version 8 or higher.



Java Runtime Environment (JRE): To run the application.



Operating System: Windows, macOS, or Linux with Java support.

Setup Instructions





Clone or Download the Project:





Clone the repository or download the source code.



Compile the Code:





Navigate to the project directory containing BankingApp.java.



Compile the Java file:

javac BankingApp.java



Run the Application:





Execute the compiled program:

java BankingApp



A transaction_log.txt file will be created in the project directory to store transaction logs.

Usage Guide





Launch the Application:





Run the program to open the GUI window.



Account Overview (Top Section):





Displays:





Savings Balance and Loan Amount on the first line.



EMI Status (EMI amount and remaining months) and Loan Eligibility on the second line.



Banking Operations (Middle Section):





Deposit: Enter an amount and click "Deposit" to add funds.



Withdraw: Enter an amount and click "Withdraw" to deduct funds (if sufficient balance).



Loan Request:





Select a loan type (Personal, Home, Car) from the dropdown.



Enter an amount within the eligibility limit (5x savings balance).



Click "Request Loan" to initiate the loan (only one loan allowed at a time).



Pay EMI: Pay the pre-filled EMI amount for the active loan.



Apply Interest: Click to credit monthly savings interest (4% annually).



Print Statement: Click to print a bank statement with account details and transaction history.



Transaction History and Log (Bottom Section):





View all transactions in the table (Date, Type, Amount, Balance).



Monitor logs in the text area, also saved to transaction_log.txt.

Example Workflow





Start with an initial savings balance of $1000.



Deposit $500: Balance becomes $1500.



Check eligibility: $1500 × 5 = $7500.



Request a Personal Loan of $1000:





Interest: 10%, Tenure: 12 months, EMI: ~$87.92/month.



Loan Amount: $1000, EMI Status: $87.92 (12 months remaining).



Pay EMI: Deduct $87.92 from balance, reduce loan amount and tenure.



Apply Interest: Credit 4%/12 = 0.333% of balance monthly.



Print Statement: View account details and transaction history.

Project Structure





BankingApp.java: Main Java file containing the application logic and GUI.



transaction_log.txt: Generated file for storing transaction logs.

Notes





The application uses GridBagLayout for precise component alignment, ensuring no overlap.



Loan types have fixed interest rates and tenures:





Personal: 10%, 12 months.



Home: 7%, 60 months.



Car: 8%, 36 months.



Transactions are logged with timestamps to transaction_log.txt.



The UI is styled with consistent fonts (Arial, 12–16), blue borders, and subtle background colors.



Thread safety is ensured using ExecutorService and synchronized blocks.

Troubleshooting





Compilation Errors: Ensure JDK is installed and the Java file is compiled correctly.



File I/O Issues: Verify write permissions in the project directory for transaction_log.txt.



Printing Issues: Ensure a printer is configured or select a PDF printer for testing.



UI Overlap: The layout has been optimized to prevent overlap; resize the window to at least 800x600 if issues persist.

License

This project is for educational purposes and not licensed for commercial use.
