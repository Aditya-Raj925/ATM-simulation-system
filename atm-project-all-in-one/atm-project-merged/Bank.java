import java.util.HashMap;
import java.util.Map;

/**
 * Acts as the central repository of cards and accounts.
 * In a real system this would be backed by a database.
 */
public class Bank {

    private final Map<String, Card> cardsByNumber;

    public Bank() {
        cardsByNumber = new HashMap<>();
        seedDummyData();
    }

    /** Pre-loads a few sample cards so the simulation is usable out of the box. */
    private void seedDummyData() {
        Account acc1 = new Account("ACC1001", "Asha Verma", "1234", 15000.00);
        Account acc2 = new Account("ACC1002", "Rohan Mehta", "4321", 5000.00);
        Account acc3 = new Account("ACC1003", "Priya Singh", "0000", 250000.00);

        registerCard(new Card("1111222233334444", acc1));
        registerCard(new Card("5555666677778888", acc2));
        registerCard(new Card("9999000011112222", acc3));
    }

    public void registerCard(Card card) {
        cardsByNumber.put(card.getCardNumber(), card);
    }

    public Card findCard(String cardNumber) {
        return cardsByNumber.get(cardNumber);
    }

    public boolean cardExists(String cardNumber) {
        return cardsByNumber.containsKey(cardNumber);
    }
}
