/**
 * Represents every possible state of an ATM session.
 * Used to drive the state machine in the ATM class and to make
 * valid transitions explicit and easy to reason about.
 */
public enum SessionState {
    IDLE,               // No card inserted, waiting for user
    CARD_INSERTED,      // Card read, waiting for PIN entry
    AUTHENTICATED,       // PIN verified, showing main menu
    TRANSACTION_IN_PROGRESS, // Performing balance/withdraw/deposit
    CARD_BLOCKED,       // Too many failed PIN attempts
    SESSION_ENDED        // Session terminated, card ejected
}
