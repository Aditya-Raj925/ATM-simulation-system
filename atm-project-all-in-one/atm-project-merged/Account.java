import java.util.ArrayList;
import java.util.List;

/**
 * Represents a single bank account tied to a card.
 * Stores balance, PIN, and a simple transaction history.
 */
public class Account {

    private final String accountNumber;
    private final String holderName;
    private String pin;
    private double balance;
    private final List<String> transactionHistory;

    private static final double DAILY_WITHDRAWAL_LIMIT = 20000.0;
    private double withdrawnToday = 0.0;

    public Account(String accountNumber, String holderName, String pin, double initialBalance) {
        this.accountNumber = accountNumber;
        this.holderName = holderName;
        this.pin = pin;
        this.balance = initialBalance;
        this.transactionHistory = new ArrayList<>();
        log("Account opened with balance: " + formatCurrency(initialBalance));
    }

    public boolean verifyPin(String enteredPin) {
        return this.pin != null && this.pin.equals(enteredPin);
    }

    public void changePin(String newPin) {
        this.pin = newPin;
        log("PIN changed successfully");
    }

    public double getBalance() {
        return balance;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public String getHolderName() {
        return holderName;
    }

    /**
     * Attempts a withdrawal. Returns a result enum describing success/failure reason
     * so the calling layer (ATM) can show an appropriate message.
     */
    public WithdrawResult withdraw(double amount) {
        if (amount <= 0) {
            return WithdrawResult.INVALID_AMOUNT;
        }
        if (amount % 100 != 0) {
            return WithdrawResult.INVALID_DENOMINATION;
        }
        if (withdrawnToday + amount > DAILY_WITHDRAWAL_LIMIT) {
            return WithdrawResult.DAILY_LIMIT_EXCEEDED;
        }
        if (amount > balance) {
            return WithdrawResult.INSUFFICIENT_FUNDS;
        }

        balance -= amount;
        withdrawnToday += amount;
        log("Withdrew: " + formatCurrency(amount) + " | New Balance: " + formatCurrency(balance));
        return WithdrawResult.SUCCESS;
    }

    public boolean deposit(double amount) {
        if (amount <= 0) {
            return false;
        }
        balance += amount;
        log("Deposited: " + formatCurrency(amount) + " | New Balance: " + formatCurrency(balance));
        return true;
    }

    public double getRemainingDailyLimit() {
        return DAILY_WITHDRAWAL_LIMIT - withdrawnToday;
    }

    private void log(String entry) {
        transactionHistory.add("[" + java.time.LocalDateTime.now().format(
                java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) + "] " + entry);
    }

    public List<String> getTransactionHistory() {
        return new ArrayList<>(transactionHistory);
    }

    public static String formatCurrency(double amount) {
        return String.format("Rs. %,.2f", amount);
    }

    /** Possible outcomes of a withdrawal attempt. */
    public enum WithdrawResult {
        SUCCESS,
        INSUFFICIENT_FUNDS,
        INVALID_AMOUNT,
        INVALID_DENOMINATION,
        DAILY_LIMIT_EXCEEDED
    }
}
