import java.util.Scanner;

/**
 * Drives the ATM session: reads user input, transitions between
 * SessionState values, and delegates real work to Bank/Card/Account.
 *
 * State machine overview:
 *
 *   IDLE --(insert card)--> CARD_INSERTED --(correct PIN)--> AUTHENTICATED
 *      ^                         |  (wrong PIN, retries left)  |
 *      |                         v                              v
 *      |                   CARD_BLOCKED                TRANSACTION_IN_PROGRESS
 *      |                         |                              |
 *      +----------(eject)--------+---------(done/exit)----------+
 *                                |
 *                          SESSION_ENDED --(reset)--> IDLE
 */
public class ATM {

    private final Bank bank;
    private final Scanner scanner;

    private SessionState state;
    private Card currentCard;

    public ATM(Bank bank) {
        this.bank = bank;
        this.scanner = new Scanner(System.in);
        this.state = SessionState.IDLE;
    }

    /** Entry point for running the ATM continuously until the user chooses to exit. */
    public void start() {
        printWelcomeBanner();

        boolean keepRunning = true;
        while (keepRunning) {
            switch (state) {
                case IDLE:
                    keepRunning = handleIdleState();
                    break;
                case CARD_INSERTED:
                    handleCardInsertedState();
                    break;
                case AUTHENTICATED:
                    handleAuthenticatedState();
                    break;
                case CARD_BLOCKED:
                    handleCardBlockedState();
                    break;
                case SESSION_ENDED:
                    handleSessionEndedState();
                    break;
                default:
                    // TRANSACTION_IN_PROGRESS is a transient state handled
                    // inline within handleAuthenticatedState(); should not loop here.
                    state = SessionState.IDLE;
            }
        }

        System.out.println("\nThank you for using the ATM. Goodbye!");
        scanner.close();
    }

    // ---------------------------------------------------------
    // State handlers
    // ---------------------------------------------------------

    private boolean handleIdleState() {
        System.out.println("\n===== Please insert your card =====");
        System.out.println("Enter card number (or type 'exit' to quit): ");
        String input = scanner.nextLine().trim();

        if (input.equalsIgnoreCase("exit")) {
            return false; // stop the main loop
        }

        Card card = bank.findCard(input);
        if (card == null) {
            System.out.println("Card not recognized. Please try again.");
            return true;
        }
        if (card.isBlocked()) {
            currentCard = card;
            state = SessionState.CARD_BLOCKED;
            return true;
        }

        currentCard = card;
        state = SessionState.CARD_INSERTED;
        return true;
    }

    private void handleCardInsertedState() {
        System.out.println("\nCard accepted for account holder: "
                + maskName(currentCard.getLinkedAccount().getHolderName()));

        boolean authenticated = false;
        while (!authenticated && !currentCard.isBlocked()) {
            System.out.print("Enter 4-digit PIN: ");
            String pin = scanner.nextLine().trim();

            if (currentCard.validatePin(pin)) {
                authenticated = true;
                state = SessionState.AUTHENTICATED;
                System.out.println("PIN verified. Access granted.");
            } else if (currentCard.isBlocked()) {
                state = SessionState.CARD_BLOCKED;
            } else {
                System.out.println("Incorrect PIN. Attempts remaining: "
                        + currentCard.getRemainingAttempts());
            }
        }
    }

    private void handleAuthenticatedState() {
        boolean sessionActive = true;

        while (sessionActive && state == SessionState.AUTHENTICATED) {
            printMenu();
            System.out.print("Choose an option: ");
            String choice = scanner.nextLine().trim();

            state = SessionState.TRANSACTION_IN_PROGRESS;

            switch (choice) {
                case "1":
                    checkBalance();
                    state = SessionState.AUTHENTICATED;
                    break;
                case "2":
                    handleWithdrawal();
                    state = SessionState.AUTHENTICATED;
                    break;
                case "3":
                    handleDeposit();
                    state = SessionState.AUTHENTICATED;
                    break;
                case "4":
                    handleChangePin();
                    state = SessionState.AUTHENTICATED;
                    break;
                case "5":
                    printMiniStatement();
                    state = SessionState.AUTHENTICATED;
                    break;
                case "6":
                    System.out.println("Ejecting card. Have a nice day, "
                            + maskName(currentCard.getLinkedAccount().getHolderName()) + "!");
                    state = SessionState.SESSION_ENDED;
                    sessionActive = false;
                    break;
                default:
                    System.out.println("Invalid option. Please select a number from the menu.");
                    state = SessionState.AUTHENTICATED;
            }
        }
    }

    private void handleCardBlockedState() {
        System.out.println("\n*** This card has been BLOCKED due to too many failed PIN attempts. ***");
        System.out.println("Please contact your bank to unblock the card.");
        // Simulate ejecting the blocked card and returning to idle for the next customer.
        currentCard = null;
        state = SessionState.SESSION_ENDED;
    }

    private void handleSessionEndedState() {
        currentCard = null;
        state = SessionState.IDLE;
    }

    // ---------------------------------------------------------
    // Transaction operations
    // ---------------------------------------------------------

    private void checkBalance() {
        Account acc = currentCard.getLinkedAccount();
        System.out.println("\n--- Balance Inquiry ---");
        System.out.println("Account: " + acc.getAccountNumber());
        System.out.println("Current Balance: " + Account.formatCurrency(acc.getBalance()));
    }

    private void handleWithdrawal() {
        Account acc = currentCard.getLinkedAccount();
        System.out.println("\n--- Withdraw Cash ---");
        System.out.println("Remaining daily limit: " + Account.formatCurrency(acc.getRemainingDailyLimit()));
        System.out.print("Enter amount to withdraw (multiples of 100): ");

        double amount = readAmount();
        if (amount < 0) return;

        Account.WithdrawResult result = acc.withdraw(amount);
        switch (result) {
            case SUCCESS:
                System.out.println("Please collect your cash.");
                System.out.println("New Balance: " + Account.formatCurrency(acc.getBalance()));
                break;
            case INSUFFICIENT_FUNDS:
                System.out.println("Transaction failed: Insufficient funds.");
                break;
            case INVALID_AMOUNT:
                System.out.println("Transaction failed: Amount must be positive.");
                break;
            case INVALID_DENOMINATION:
                System.out.println("Transaction failed: Amount must be a multiple of 100.");
                break;
            case DAILY_LIMIT_EXCEEDED:
                System.out.println("Transaction failed: Daily withdrawal limit exceeded.");
                break;
        }
    }

    private void handleDeposit() {
        Account acc = currentCard.getLinkedAccount();
        System.out.println("\n--- Deposit Cash ---");
        System.out.print("Enter amount to deposit: ");

        double amount = readAmount();
        if (amount < 0) return;

        boolean success = acc.deposit(amount);
        if (success) {
            System.out.println("Deposit successful.");
            System.out.println("New Balance: " + Account.formatCurrency(acc.getBalance()));
        } else {
            System.out.println("Transaction failed: Amount must be positive.");
        }
    }

    private void handleChangePin() {
        Account acc = currentCard.getLinkedAccount();
        System.out.println("\n--- Change PIN ---");
        System.out.print("Enter current PIN: ");
        String oldPin = scanner.nextLine().trim();

        if (!acc.verifyPin(oldPin)) {
            System.out.println("Incorrect current PIN. PIN not changed.");
            return;
        }

        System.out.print("Enter new 4-digit PIN: ");
        String newPin = scanner.nextLine().trim();

        if (!newPin.matches("\\d{4}")) {
            System.out.println("Invalid PIN format. PIN must be exactly 4 digits.");
            return;
        }

        acc.changePin(newPin);
        System.out.println("PIN changed successfully.");
    }

    private void printMiniStatement() {
        Account acc = currentCard.getLinkedAccount();
        System.out.println("\n--- Mini Statement (last transactions) ---");
        var history = acc.getTransactionHistory();
        int start = Math.max(0, history.size() - 5);
        for (int i = start; i < history.size(); i++) {
            System.out.println(history.get(i));
        }
        if (history.isEmpty()) {
            System.out.println("No transactions yet.");
        }
    }

    // ---------------------------------------------------------
    // Helpers
    // ---------------------------------------------------------

    private double readAmount() {
        try {
            double amount = Double.parseDouble(scanner.nextLine().trim());
            return amount;
        } catch (NumberFormatException e) {
            System.out.println("Invalid input. Please enter a numeric amount.");
            return -1;
        }
    }

    private void printMenu() {
        System.out.println("\n========= MAIN MENU =========");
        System.out.println("1. Check Balance");
        System.out.println("2. Withdraw Cash");
        System.out.println("3. Deposit Cash");
        System.out.println("4. Change PIN");
        System.out.println("5. Mini Statement");
        System.out.println("6. Exit / Eject Card");
        System.out.println("==============================");
    }

    private void printWelcomeBanner() {
        System.out.println("***********************************");
        System.out.println("*   WELCOME TO JAVA BANK ATM     *");
        System.out.println("***********************************");
        System.out.println("(Demo cards: 1111222233334444 / PIN 1234)");
        System.out.println("(Demo cards: 5555666677778888 / PIN 4321)");
    }

    private String maskName(String name) {
        // Just a simple courtesy formatter; not true masking.
        return name;
    }
}
