package bank;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.text.DecimalFormat;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/*
 * Develop a Java based Banking Application using Swing to demonstrate
- common bank operations
⦁	using multithreading whenever applicable.
⦁	Display relevant options for savings bank account
⦁	- deposit and withdraw,
Also add facility to
⦁	take request Loan
⦁	pay EMI in 12 months
 */
public class BankingApp extends JFrame {
    private double savingsBalance = 1000.00; // Initial balance
    private double loanAmount = 0.0;
    private double emiAmount = 0.0;
    private int emiTenure = 0;
    private final double INTEREST_RATE = 0.08; // 8% annual interest
    private ExecutorService executor = Executors.newFixedThreadPool(2); // Thread pool for operations

    // UI Components
    private JLabel balanceLabel, loanLabel, emiLabel;
    private JTextField depositField, withdrawField, loanRequestField, emiPayField;
    private JButton depositButton, withdrawButton, loanButton, emiButton;
    private JTextArea logArea;

    public BankingApp() {
        // Frame setup
        setTitle("Banking Application");
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));
        getContentPane().setBackground(new Color(240, 240, 240));

        // Initialize components
        initComponents();

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
        // North Panel - Balance and Loan Info
        JPanel northPanel = new JPanel(new GridLayout(3, 2, 5, 5));
        northPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        northPanel.setBackground(new Color(220, 220, 220));

        balanceLabel = new JLabel("Savings Balance: $" + formatAmount(savingsBalance));
        loanLabel = new JLabel("Loan Amount: $" + formatAmount(loanAmount));
        emiLabel = new JLabel("EMI: $" + formatAmount(emiAmount) + " (Remaining: " + emiTenure + " months)");
        
        northPanel.add(new JLabel("Account Status:", SwingConstants.RIGHT));
        northPanel.add(balanceLabel);
        northPanel.add(new JLabel("Loan Status:", SwingConstants.RIGHT));
        northPanel.add(loanLabel);
        northPanel.add(new JLabel("EMI Status:", SwingConstants.RIGHT));
        northPanel.add(emiLabel);

        // Center Panel - Operations
        JPanel centerPanel = new JPanel(new GridLayout(4, 3, 10, 10));
        centerPanel.setBorder(BorderFactory.createTitledBorder("Banking Operations"));
        
        // Deposit
        centerPanel.add(new JLabel("Deposit Amount:"));
        depositField = new JTextField(10);
        centerPanel.add(depositField);
        depositButton = new JButton("Deposit");
        centerPanel.add(depositButton);

        // Withdraw
        centerPanel.add(new JLabel("Withdraw Amount:"));
        withdrawField = new JTextField(10);
        centerPanel.add(withdrawField);
        withdrawButton = new JButton("Withdraw");
        centerPanel.add(withdrawButton);

        // Loan Request
        centerPanel.add(new JLabel("Loan Amount:"));
        loanRequestField = new JTextField(10);
        centerPanel.add(loanRequestField);
        loanButton = new JButton("Request Loan");
        centerPanel.add(loanButton);

        // EMI Payment
        centerPanel.add(new JLabel("Pay EMI:"));
        emiPayField = new JTextField(10);
        emiPayField.setEditable(false);
        centerPanel.add(emiPayField);
        emiButton = new JButton("Pay EMI");
        emiButton.setEnabled(false);
        centerPanel.add(emiButton);

        // South Panel - Transaction Log
        logArea = new JTextArea(5, 40);
        logArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(logArea);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Transaction Log"));

        // Add panels to frame
        add(northPanel, BorderLayout.NORTH);
        add(centerPanel, BorderLayout.CENTER);
        add(scrollPane, BorderLayout.SOUTH);

        // Add action listeners
        addActionListeners();
    }

    private void addActionListeners() {
        depositButton.addActionListener(e -> {
            executor.submit(() -> processDeposit());
        });

        withdrawButton.addActionListener(e -> {
            executor.submit(() -> processWithdrawal());
        });

        loanButton.addActionListener(e -> {
            executor.submit(() -> processLoanRequest());
        });

        emiButton.addActionListener(e -> {
            executor.submit(() -> processEMIPayment());
        });
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
            if (amount <= 0) {
                logMessage("Invalid loan amount!");
                return;
            }
            synchronized (this) {
                if (loanAmount == 0) {
                    loanAmount = amount;
                    emiTenure = 12; // 1-year loan term
                    emiAmount = calculateEMI(amount, INTEREST_RATE, emiTenure);
                    SwingUtilities.invokeLater(() -> {
                        emiPayField.setText(formatAmount(emiAmount));
                        emiPayField.setEditable(true);
                        emiButton.setEnabled(true);
                    });
                    updateUI();
                    logMessage("Loan of $" + formatAmount(amount) + " approved. EMI: $" + formatAmount(emiAmount) + "/month");
                } else {
                    logMessage("Existing loan must be cleared first!");
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
                    if (emiTenure <= 0 || loanAmount <= 0) {
                        loanAmount = 0;
                        emiAmount = 0;
                        emiTenure = 0;
                        SwingUtilities.invokeLater(() -> {
                            emiPayField.setEditable(false);
                            emiButton.setEnabled(false);
                        });
                        logMessage("Loan fully repaid!");
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

    private double calculateEMI(double principal, double annualRate, int months) {
        double monthlyRate = annualRate / 12;
        return (principal * monthlyRate * Math.pow(1 + monthlyRate, months)) /
               (Math.pow(1 + monthlyRate, months) - 1);
    }

    private String formatAmount(double amount) {
        return new DecimalFormat("#,##0.00").format(amount);
    }

    private void updateUI() {
        SwingUtilities.invokeLater(() -> {
            balanceLabel.setText("Savings Balance: $" + formatAmount(savingsBalance));
            loanLabel.setText("Loan Amount: $" + formatAmount(loanAmount));
            emiLabel.setText("EMI: $" + formatAmount(emiAmount) + " (Remaining: " + emiTenure + " months)");
        });
    }

    private void logMessage(String message) {
        SwingUtilities.invokeLater(() -> {
            logArea.append(message + "\n");
            logArea.setCaretPosition(logArea.getDocument().getLength());
        });
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new BankingApp().setVisible(true);
        });
    }
}