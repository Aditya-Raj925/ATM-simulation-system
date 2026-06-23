/**
 * Application entry point.
 * Creates the Bank (data layer) and ATM (UI / state machine layer),
 * then starts the interactive session.
 */
public class Main {
    public static void main(String[] args) {
        Bank bank = new Bank();
        ATM atm = new ATM(bank);
        atm.start();
    }
}
