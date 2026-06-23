/**
 * Represents an ATM card. Tracks failed PIN attempts and lock status
 * independently of the underlying Account.
 */
public class Card {

    private final String cardNumber;
    private final Account linkedAccount;
    private int failedAttempts;
    private boolean blocked;

    private static final int MAX_FAILED_ATTEMPTS = 3;

    public Card(String cardNumber, Account linkedAccount) {
        this.cardNumber = cardNumber;
        this.linkedAccount = linkedAccount;
        this.failedAttempts = 0;
        this.blocked = false;
    }

    public String getCardNumber() {
        return cardNumber;
    }

    public Account getLinkedAccount() {
        return linkedAccount;
    }

    public boolean isBlocked() {
        return blocked;
    }

    /**
     * Validates the PIN against the linked account.
     * Increments failure counter and auto-blocks the card after too many tries.
     */
    public boolean validatePin(String enteredPin) {
        if (blocked) {
            return false;
        }
        boolean correct = linkedAccount.verifyPin(enteredPin);
        if (correct) {
            failedAttempts = 0;
        } else {
            failedAttempts++;
            if (failedAttempts >= MAX_FAILED_ATTEMPTS) {
                blocked = true;
            }
        }
        return correct;
    }

    public int getRemainingAttempts() {
        return MAX_FAILED_ATTEMPTS - failedAttempts;
    }

    public void unblock() {
        blocked = false;
        failedAttempts = 0;
    }
}
