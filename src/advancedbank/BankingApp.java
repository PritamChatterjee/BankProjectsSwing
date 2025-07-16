package advancedbank;


/*
 * Advanced Banking Application: Additional Features apart from Basic Banking
 * Show Bank Statement
 * Facilty to Print Bank Statement
 * Multi-threading for Operations
 * Transaction Logging in a separate file
 * Separate options for Personal, Home and Car Loans
 * View in Full Screen Mode
 */
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.awt.print.*;
import java.io.*;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BankingApp extends JFrame implements Printable {
    private double savingsBalance = 1000.00; // Initial balance
    private double loanAmount = 0.0;
    private double emiAmount = 0.0;
    private int emiTenure = 0;
    private String currentLoanType = null;
    private final double SAVINGS_INTEREST_RATE = 0.04; // 4% annual interest for savings
    private final double PERSONAL_LOAN_RATE = 0.10; // 10% for personal loan
    private final double HOME_LOAN_RATE = 0.07; // 7% for home loan
    private final double CAR_LOAN_RATE = 0.08; // 8% for car loan
    private final int PERSONAL_LOAN_TENURE = 12; // 12 months
    private final int HOME_LOAN_TENURE = 60; // 60 months
    private final int CAR_LOAN_TENURE = 36; // 36 months
    private ExecutorService executor = Executors.newFixedThreadPool(3); // Thread pool for operations
    private DefaultTableModel transactionModel;
    private File logFile = new File("transaction_log.txt");

    // UI Components
    private JLabel balanceLabel, loanLabel, emiLabel, eligibilityLabel;
    private JTextField depositField, withdrawField, loanRequestField, emiPayField;
    private JComboBox<String> loanTypeCombo;
    private JButton depositButton, withdrawButton, loanButton, emiButton, applyInterestButton, printStatementButton;
    private JTable transactionTable;
    private JTextArea logArea;

    public BankingApp() {
        // Frame setup
        setTitle("Advanced Banking Application");
        setSize(900, 700);
        setMinimumSize(new Dimension(800, 600));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(15, 15));
        getContentPane().setBackground(new Color(240, 242, 245));

        // Initialize components
        initComponents();

        // Initialize log file
        initLogFile();

        // Add window listener to shutdown executor
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                executor.shutdown();
            }
        });

        setLocationRelativeTo(null);
    }

    private void initComponents() {
        // North Panel - Account Info
        JPanel northPanel = new JPanel(new GridBagLayout());
        northPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(0, 102, 204), 2), 
            "Account Overview", 
            javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, 
            javax.swing.border.TitledBorder.DEFAULT_POSITION, 
            new Font("Arial", Font.BOLD, 16)
        ));
        northPanel.setBackground(new Color(230, 240, 255));
        northPanel.setBorder(BorderFactory.createCompoundBorder(
            northPanel.getBorder(), 
            BorderFactory.createEmptyBorder(15, 20, 15, 20)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 15, 8, 15);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 0.5;

        balanceLabel = new JLabel("Savings Balance: $" + formatAmount(savingsBalance));
        balanceLabel.setFont(new Font("Arial", Font.BOLD, 14));
        loanLabel = new JLabel("Loan Amount: $" + formatAmount(loanAmount) + (currentLoanType != null ? " (" + currentLoanType + ")" : ""));
        loanLabel.setFont(new Font("Arial", Font.BOLD, 14));
        emiLabel = new JLabel("EMI: $" + formatAmount(emiAmount) + " (Remaining: " + emiTenure + " months)");
        emiLabel.setFont(new Font("Arial", Font.BOLD, 14));
        eligibilityLabel = new JLabel("Loan Eligibility: $" + formatAmount(calculateLoanEligibility()));
        eligibilityLabel.setFont(new Font("Arial", Font.BOLD, 14));

        // Line 1: Savings Balance and Loan Amount
        gbc.gridx = 0; gbc.gridy = 0; gbc.anchor = GridBagConstraints.EAST;
        northPanel.add(createLabel("Savings Balance:"), gbc);
        gbc.gridx = 1; gbc.anchor = GridBagConstraints.WEST;
        northPanel.add(balanceLabel, gbc);
        gbc.gridx = 2; gbc.anchor = GridBagConstraints.EAST;
        northPanel.add(createLabel("Loan Amount:"), gbc);
        gbc.gridx = 3; gbc.anchor = GridBagConstraints.WEST;
        northPanel.add(loanLabel, gbc);

        // Line 2: EMI Status and Loan Eligibility
        gbc.gridx = 0; gbc.gridy = 1;
        northPanel.add(createLabel("EMI Status:"), gbc);
        gbc.gridx = 1;
        northPanel.add(emiLabel, gbc);
        gbc.gridx = 2;
        northPanel.add(createLabel("Loan Eligibility:"), gbc);
        gbc.gridx = 3;
        northPanel.add(eligibilityLabel, gbc);

        // Center Panel - Operations
        JPanel centerPanel = new JPanel(new GridBagLayout());
        centerPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(0, 102, 204), 2), 
            "Banking Operations", 
            javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, 
            javax.swing.border.TitledBorder.DEFAULT_POSITION, 
            new Font("Arial", Font.BOLD, 16)
        ));
        centerPanel.setBackground(new Color(245, 247, 250));
        centerPanel.setBorder(BorderFactory.createCompoundBorder(
            centerPanel.getBorder(), 
            BorderFactory.createEmptyBorder(15, 20, 15, 20)
        ));

        gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 15, 10, 15);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 0.0;

        // Deposit
        gbc.gridx = 0; gbc.gridy = 0; gbc.anchor = GridBagConstraints.EAST;
        centerPanel.add(createLabel("Deposit Amount:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        depositField = new JTextField(15);
        depositField.setFont(new Font("Arial", Font.PLAIN, 14));
        depositField.setPreferredSize(new Dimension(200, 30));
        centerPanel.add(depositField, gbc);
        gbc.gridx = 2; gbc.weightx = 0.0;
        depositButton = createStyledButton("Deposit");
        centerPanel.add(depositButton, gbc);

        // Withdraw
        gbc.gridx = 0; gbc.gridy = 1;
        centerPanel.add(createLabel("Withdraw Amount:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        withdrawField = new JTextField(15);
        withdrawField.setFont(new Font("Arial", Font.PLAIN, 14));
        withdrawField.setPreferredSize(new Dimension(200, 30));
        centerPanel.add(withdrawField, gbc);
        gbc.gridx = 2; gbc.weightx = 0.0;
        withdrawButton = createStyledButton("Withdraw");
        centerPanel.add(withdrawButton, gbc);

        // Loan Request (Type and Amount)
        JPanel loanPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        loanPanel.setBackground(new Color(245, 247, 250));
        loanTypeCombo = new JComboBox<>(new String[]{"Select Loan Type", "Personal", "Home", "Car"});
        loanTypeCombo.setFont(new Font("Arial", Font.PLAIN, 14));
        loanTypeCombo.setPreferredSize(new Dimension(150, 30));
        loanRequestField = new JTextField(10);
        loanRequestField.setFont(new Font("Arial", Font.PLAIN, 14));
        loanRequestField.setPreferredSize(new Dimension(150, 30));
        loanPanel.add(loanTypeCombo);
        loanPanel.add(loanRequestField);

        gbc.gridx = 0; gbc.gridy = 2;
        centerPanel.add(createLabel("Loan Request:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        centerPanel.add(loanPanel, gbc);
        gbc.gridx = 2; gbc.weightx = 0.0;
        loanButton = createStyledButton("Request Loan");
        centerPanel.add(loanButton, gbc);

        // EMI Payment
        gbc.gridx = 0; gbc.gridy = 3;
        centerPanel.add(createLabel("Pay EMI:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        emiPayField = new JTextField(15);
        emiPayField.setFont(new Font("Arial", Font.PLAIN, 14));
        emiPayField.setPreferredSize(new Dimension(200, 30));
        emiPayField.setEditable(false);
        centerPanel.add(emiPayField, gbc);
        gbc.gridx = 2; gbc.weightx = 0.0;
        emiButton = createStyledButton("Pay EMI");
        emiButton.setEnabled(false);
        centerPanel.add(emiButton, gbc);

        // Apply Interest
        gbc.gridx = 0; gbc.gridy = 4;
        centerPanel.add(createLabel("Apply Savings Interest:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        centerPanel.add(new JLabel(""), gbc);
        gbc.gridx = 2; gbc.weightx = 0.0;
        applyInterestButton = createStyledButton("Apply Interest");
        centerPanel.add(applyInterestButton, gbc);

        // Print Statement
        gbc.gridx = 0; gbc.gridy = 5;
        centerPanel.add(createLabel("Bank Statement:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        centerPanel.add(new JLabel(""), gbc);
        gbc.gridx = 2; gbc.weightx = 0.0;
        printStatementButton = createStyledButton("Print Statement");
        centerPanel.add(printStatementButton, gbc);

        // South Panel - Transaction History and Log
        JPanel southPanel = new JPanel(new BorderLayout(10, 10));
        southPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
        southPanel.setBackground(new Color(240, 242, 245));

        // Transaction Table
        transactionModel = new DefaultTableModel(new Object[]{"Date", "Type", "Amount", "Balance"}, 0);
        transactionTable = new JTable(transactionModel);
        transactionTable.setRowHeight(25);
        transactionTable.setFont(new Font("Arial", Font.PLAIN, 12));
        transactionTable.setGridColor(new Color(200, 200, 200));
        transactionTable.getColumnModel().getColumn(0).setPreferredWidth(150);
        transactionTable.getColumnModel().getColumn(1).setPreferredWidth(150);
        transactionTable.getColumnModel().getColumn(2).setPreferredWidth(100);
        transactionTable.getColumnModel().getColumn(3).setPreferredWidth(100);
        JScrollPane tableScrollPane = new JScrollPane(transactionTable);
        tableScrollPane.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(0, 102, 204), 2), 
            "Transaction History", 
            javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, 
            javax.swing.border.TitledBorder.DEFAULT_POSITION, 
            new Font("Arial", Font.BOLD, 16)
        ));
        tableScrollPane.setPreferredSize(new Dimension(0, 200));

        // Log Area
        logArea = new JTextArea(6, 40);
        logArea.setEditable(false);
        logArea.setFont(new Font("Arial", Font.PLAIN, 12));
        logArea.setBackground(new Color(255, 255, 255));
        JScrollPane logScrollPane = new JScrollPane(logArea);
        logScrollPane.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(0, 102, 204), 2), 
            "Transaction Log", 
            javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, 
            javax.swing.border.TitledBorder.DEFAULT_POSITION, 
            new Font("Arial", Font.BOLD, 16)
        ));

        southPanel.add(tableScrollPane, BorderLayout.CENTER);
        southPanel.add(logScrollPane, BorderLayout.SOUTH);

        // Add panels to frame
        add(northPanel, BorderLayout.NORTH);
        add(centerPanel, BorderLayout.CENTER);
        add(southPanel, BorderLayout.SOUTH);

        // Add action listeners
        addActionListeners();
    }

    private void initLogFile() {
        try {
            if (!logFile.exists()) {
                logFile.createNewFile();
            }
        } catch (IOException ex) {
            logMessage("Failed to initialize log file: " + ex.getMessage());
        }
    }

    private JLabel createLabel(String text) {
        JLabel label = new JLabel(text, SwingConstants.RIGHT);
        label.setFont(new Font("Arial", Font.PLAIN, 14));
        return label;
    }

    private JButton createStyledButton(String text) {
        JButton button = new JButton(text);
        button.setBackground(new Color(0, 102, 204));
        button.setForeground(Color.WHITE);
        button.setFont(new Font("Arial", Font.BOLD, 12));
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        button.setPreferredSize(new Dimension(120, 30));
        return button;
    }

    private void addActionListeners() {
        depositButton.addActionListener(e -> executor.submit(() -> processDeposit()));
        withdrawButton.addActionListener(e -> executor.submit(() -> processWithdrawal()));
        loanButton.addActionListener(e -> executor.submit(() -> processLoanRequest()));
        emiButton.addActionListener(e -> executor.submit(() -> processEMIPayment()));
        applyInterestButton.addActionListener(e -> executor.submit(() -> applySavingsInterest()));
        printStatementButton.addActionListener(e -> executor.submit(() -> printBankStatement()));
    }

    private void processDeposit() {
        try {
            double amount = Double.parseDouble(depositField.getText());
            if (amount <= 0) {
                logMessage("Invalid deposit amount!");
                return;
            }
            synchronized (this) {
                savingsBalance += amount;
                addTransaction("Deposit", amount, savingsBalance);
                updateUI();
                logMessage("Deposited $" + formatAmount(amount) + " successfully");
            }
            Thread.sleep(1000); // Simulate processing time
        } catch (NumberFormatException ex) {
            logMessage("Invalid deposit amount format!");
        } catch (InterruptedException ex) {
            logMessage("Deposit processing interrupted");
        } finally {
            SwingUtilities.invokeLater(() -> depositField.setText(""));
        }
    }

    private void processWithdrawal() {
        try {
            double amount = Double.parseDouble(withdrawField.getText());
            if (amount <= 0) {
                logMessage("Invalid withdrawal amount!");
                return;
            }
            synchronized (this) {
                if (amount <= savingsBalance) {
                    savingsBalance -= amount;
                    addTransaction("Withdrawal", amount, savingsBalance);
                    updateUI();
                    logMessage("Withdrawn $" + formatAmount(amount) + " successfully");
                } else {
                    logMessage("Insufficient balance!");
                }
            }
            Thread.sleep(1000); // Simulate processing time
        } catch (NumberFormatException ex) {
            logMessage("Invalid withdrawal amount format!");
        } catch (InterruptedException ex) {
            logMessage("Withdrawal processing interrupted");
        } finally {
            SwingUtilities.invokeLater(() -> withdrawField.setText(""));
        }
    }

    private void processLoanRequest() {
        try {
            double amount = Double.parseDouble(loanRequestField.getText());
            String selectedLoanType = (String) loanTypeCombo.getSelectedItem();
            if (amount <= 0) {
                logMessage("Invalid loan amount!");
                return;
            }
            if (selectedLoanType.equals("Select Loan Type")) {
                logMessage("Please select a loan type!");
                return;
            }
            synchronized (this) {
                double eligibility = calculateLoanEligibility();
                if (loanAmount == 0 && amount <= eligibility) {
                    loanAmount = amount;
                    currentLoanType = selectedLoanType;
                    switch (selectedLoanType) {
                        case "Personal":
                            emiTenure = PERSONAL_LOAN_TENURE;
                            emiAmount = calculateEMI(amount, PERSONAL_LOAN_RATE, emiTenure);
                            break;
                        case "Home":
                            emiTenure = HOME_LOAN_TENURE;
                            emiAmount = calculateEMI(amount, HOME_LOAN_RATE, emiTenure);
                            break;
                        case "Car":
                            emiTenure = CAR_LOAN_TENURE;
                            emiAmount = calculateEMI(amount, CAR_LOAN_RATE, emiTenure);
                            break;
                    }
                    SwingUtilities.invokeLater(() -> {
                        emiPayField.setText(formatAmount(emiAmount));
                        emiPayField.setEditable(true);
                        emiButton.setEnabled(true);
                        loanTypeCombo.setSelectedIndex(0);
                    });
                    addTransaction(currentLoanType + " Loan Taken", amount, savingsBalance);
                    updateUI();
                    logMessage(currentLoanType + " Loan of $" + formatAmount(amount) + " approved. EMI: $" + formatAmount(emiAmount) + "/month");
                } else if (loanAmount > 0) {
                    logMessage("Existing loan must be cleared first!");
                } else {
                    logMessage("Loan amount exceeds eligibility ($" + formatAmount(eligibility) + ")!");
                }
            }
            Thread.sleep(1500); // Simulate loan processing time
        } catch (NumberFormatException ex) {
            logMessage("Invalid loan amount format!");
        } catch (InterruptedException ex) {
            logMessage("Loan processing interrupted");
        } finally {
            SwingUtilities.invokeLater(() -> loanRequestField.setText(""));
        }
    }

    private void processEMIPayment() {
        try {
            double amount = Double.parseDouble(emiPayField.getText());
            if (amount != emiAmount) {
                logMessage("Please pay exact EMI amount: $" + formatAmount(emiAmount));
                return;
            }
            synchronized (this) {
                if (amount <= savingsBalance) {
                    savingsBalance -= amount;
                    loanAmount -= amount;
                    emiTenure--;
                    addTransaction("EMI Payment (" + currentLoanType + ")", amount, savingsBalance);
                    if (emiTenure <= 0 || loanAmount <= 0) {
                        loanAmount = 0;
                        emiAmount = 0;
                        emiTenure = 0;
                        currentLoanType = null;
                        SwingUtilities.invokeLater(() -> {
                            emiPayField.setEditable(false);
                            emiButton.setEnabled(false);
                        });
                        logMessage(currentLoanType + " Loan fully repaid!");
                    } else {
                        logMessage("EMI of $" + formatAmount(amount) + " paid successfully");
                    }
                    updateUI();
                } else {
                    logMessage("Insufficient balance to pay EMI!");
                }
            }
            Thread.sleep(1000); // Simulate EMI processing time
        } catch (NumberFormatException ex) {
            logMessage("Invalid EMI amount format!");
        } catch (InterruptedException ex) {
            logMessage("EMI payment processing interrupted");
        }
    }

    private void applySavingsInterest() {
        try {
            synchronized (this) {
                double interest = savingsBalance * SAVINGS_INTEREST_RATE / 12;
                savingsBalance += interest;
                addTransaction("Interest Credited", interest, savingsBalance);
                updateUI();
                logMessage("Interest of $" + formatAmount(interest) + " credited successfully");
            }
            Thread.sleep(800); // Simulate interest processing time
        } catch (InterruptedException ex) {
            logMessage("Interest processing interrupted");
        }
    }

    private void printBankStatement() {
        try {
            PrinterJob job = PrinterJob.getPrinterJob();
            job.setPrintable(this);
            if (job.printDialog()) {
                job.print();
                logMessage("Bank statement printed successfully");
            }
        } catch (PrinterException ex) {
            logMessage("Printing failed: " + ex.getMessage());
        }
    }

    @Override
    public int print(Graphics g, PageFormat pf, int page) throws PrinterException {
        if (page > 0) {
            return NO_SUCH_PAGE;
        }

        Graphics2D g2d = (Graphics2D) g;
        g2d.translate(pf.getImageableX(), pf.getImageableY());

        // Print header
        g2d.setFont(new Font("Arial", Font.BOLD, 16));
        g2d.drawString("Bank Statement", 100, 30);
        g2d.setFont(new Font("Arial", Font.PLAIN, 12));
        g2d.drawString("Date: " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()), 100, 50);
        g2d.drawString("Savings Balance: $" + formatAmount(savingsBalance), 100, 70);
        g2d.drawString("Loan Amount: $" + formatAmount(loanAmount) + (currentLoanType != null ? " (" + currentLoanType + ")" : ""), 100, 90);
        g2d.drawString("EMI: $" + formatAmount(emiAmount) + " (Remaining: " + emiTenure + " months)", 100, 110);

        // Print transaction table
        g2d.drawString("Transaction History:", 100, 140);
        g2d.setFont(new Font("Arial", Font.PLAIN, 10));
        int y = 160;
        g2d.drawString("Date                Type                Amount        Balance", 100, y);
        g2d.drawString("------------------------------------------------------------", 100, y + 10);
        y += 20;
        for (int i = 0; i < transactionModel.getRowCount(); i++) {
            String date = (String) transactionModel.getValueAt(i, 0);
            String type = (String) transactionModel.getValueAt(i, 1);
            String amount = (String) transactionModel.getValueAt(i, 2);
            String balance = (String) transactionModel.getValueAt(i, 3);
            g2d.drawString(String.format("%-20s %-20s %-12s %-12s", date, type, amount, balance), 100, y);
            y += 15;
        }

        return PAGE_EXISTS;
    }

    private double calculateEMI(double principal, double annualRate, int months) {
        double monthlyRate = annualRate / 12;
        return (principal * monthlyRate * Math.pow(1 + monthlyRate, months)) /
               (Math.pow(1 + monthlyRate, months) - 1);
    }

    private double calculateLoanEligibility() {
        return savingsBalance * 5;
    }

    private String formatAmount(double amount) {
        return new DecimalFormat("#,##0.00").format(amount);
    }

    private void addTransaction(String type, double amount, double balance) {
        String date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        Vector<Object> row = new Vector<>();
        row.add(date);
        row.add(type);
        row.add("$" + formatAmount(amount));
        row.add("$" + formatAmount(balance));
        SwingUtilities.invokeLater(() -> transactionModel.addRow(row));
    }

    private void updateUI() {
        SwingUtilities.invokeLater(() -> {
            balanceLabel.setText("Savings Balance: $" + formatAmount(savingsBalance));
            loanLabel.setText("Loan Amount: $" + formatAmount(loanAmount) + (currentLoanType != null ? " (" + currentLoanType + ")" : ""));
            emiLabel.setText("EMI: $" + formatAmount(emiAmount) + " (Remaining: " + emiTenure + " months)");
            eligibilityLabel.setText("Loan Eligibility: $" + formatAmount(calculateLoanEligibility()));
        });
    }

    private void logMessage(String message) {
        String timestampedMessage = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()) + ": " + message;
        SwingUtilities.invokeLater(() -> {
            logArea.append(timestampedMessage + "\n");
            logArea.setCaretPosition(logArea.getDocument().getLength());
        });
        executor.submit(() -> {
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(logFile, true))) {
                writer.write(timestampedMessage);
                writer.newLine();
            } catch (IOException ex) {
                SwingUtilities.invokeLater(() -> {
                    logArea.append("Failed to write to log file: " + ex.getMessage() + "\n");
                    logArea.setCaretPosition(logArea.getDocument().getLength());
                });
            }
        });
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new BankingApp().setVisible(true);
        });
    }
}