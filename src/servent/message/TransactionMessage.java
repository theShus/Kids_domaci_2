package servent.message;

import app.ServentInfo;
import app.snapshot_bitcake.BitcakeManager;

import java.io.Serial;
import java.util.Map;

/**
 * Represents a bitcake transaction. We are sending some bitcakes to another node.
 *
 * @author bmilojkovic
 */
public class TransactionMessage extends BasicMessage {

    @Serial
    private static final long serialVersionUID = -333251402058492901L;
    private final transient BitcakeManager bitcakeManager;

    public TransactionMessage(ServentInfo sender, ServentInfo receiver, ServentInfo neighbor, Map<Integer, Integer> senderVectorClock, int amount, BitcakeManager bitcakeManager) {
        super(MessageType.TRANSACTION, sender, receiver, neighbor, senderVectorClock, String.valueOf(amount));
        this.bitcakeManager = bitcakeManager;
    }

    /**
     * We want to take away our amount exactly as we are sending, so our snapshots don't mess up.
     * This method is invoked by the sender just before sending, and with a lock that guarantees
     * that we are white when we are doing this in Chandy-Lamport.
     */
    @Override
    public void sendEffect() {
        int amount = Integer.parseInt(getMessageText());
        bitcakeManager.takeSomeBitcakes(amount);
    }
}
