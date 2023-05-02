package servent.message;

import app.AppConfig;
import app.CausalBroadcastShared;
import app.ServentInfo;
import app.snapshot_bitcake.BitcakeManager;
import app.snapshot_bitcake.ab.AbBitCakeManager;
import app.snapshot_bitcake.av.AvBitCakeManager;

import java.io.Serial;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Represents a bitcake transaction. We are sending some bitcakes to another node.
 *
 * @author bmilojkovic
 */
public class TransactionMessage extends BasicMessage {

    @Serial
    private static final long serialVersionUID = -333251402058492901L;
    private int originallyIntendedReceiver;//todo skloni kasnije ako originalReceiverInfo radi
    private final transient BitcakeManager bitcakeManager;

    public TransactionMessage(ServentInfo sender, ServentInfo originalReceiverInfo, ServentInfo receiver, int amount, BitcakeManager bitcakeManager, Map<Integer, Integer> vectorClock) {
        super(MessageType.TRANSACTION, sender,originalReceiverInfo ,receiver, String.valueOf(amount));
        this.bitcakeManager = bitcakeManager;
        this.setSenderVectorClock(vectorClock);
    }

    private TransactionMessage(ServentInfo originalSenderInfo, ServentInfo originalReceiverInfo, ServentInfo receiverInfo, List<ServentInfo> routeList, String messageText,
                               int messageId, Map<Integer, Integer> senderVectorClock, BitcakeManager bitcakeManager, int originallyIntendedReceiver) {
        super(MessageType.TRANSACTION, originalSenderInfo, originalReceiverInfo, receiverInfo, routeList, senderVectorClock, messageText, messageId);
        this.originallyIntendedReceiver = originallyIntendedReceiver;
        this.bitcakeManager = bitcakeManager;

    }

    /**
     * We want to take away our amount exactly as we are sending, so our snapshots don't mess up.
     * This method is invoked by the sender just before sending, and with a lock that guarantees
     * that we are white when we are doing this in Chandy-Lamport.
     */
    @Override
    public void sendEffect() {
        if (bitcakeManager == null) {
            System.out.println("BitCake manager is null");
            return;
        }

        int amount = Integer.parseInt(getMessageText());

        synchronized (AppConfig.syncHole) {
            bitcakeManager.takeSomeBitcakes(amount);

            CausalBroadcastShared.incrementClock(AppConfig.myServentInfo.getId());
            //todo zato ovo?

            if (bitcakeManager instanceof AbBitCakeManager) {
                AbBitCakeManager abBitCakeManager = (AbBitCakeManager) bitcakeManager;
                abBitCakeManager.recordGiveTransaction(getReceiverInfo().getId(), amount);
                //todo proveri
            }
            else if (bitcakeManager instanceof AvBitCakeManager) {
                AvBitCakeManager avBitCakeManager = (AvBitCakeManager) bitcakeManager;
                avBitCakeManager.recordGiveTransaction(getSenderVectorClock(), getReceiverInfo().getId(), amount);
                //todo dovrsi i proveri
            }
        }
    }

    @Override
    public Message changeReceiver(Integer newReceiverId) {
        if (AppConfig.myServentInfo.getNeighbors().contains(newReceiverId)) {
            ServentInfo newReceiverInfo = AppConfig.getInfoById(newReceiverId);

            Message toReturn = new TransactionMessage(getOriginalSenderInfo(), getOriginalReceiverInfo(),newReceiverInfo, getRoute(), getMessageText(),
                    getMessageId(), getSenderVectorClock(), bitcakeManager, originallyIntendedReceiver);

            return toReturn;
        } else {
            AppConfig.timestampedErrorPrint("Trying to make a message for " + newReceiverId + " who is not a neighbor.");

            return null;
        }

    }

    @Override
    public Message makeMeASender() {
        ServentInfo newRouteItem = AppConfig.myServentInfo;

        List<ServentInfo> newRouteList = new ArrayList<>(getRoute());
        newRouteList.add(newRouteItem);
        Message toReturn = new TransactionMessage(getOriginalSenderInfo(), getOriginalReceiverInfo(), getReceiverInfo(), newRouteList,
                getMessageText(), getMessageId(), getSenderVectorClock(), bitcakeManager, originallyIntendedReceiver);

        return toReturn;
    }


    public int getOriginallyIntendedReceiver() {
        return originallyIntendedReceiver;
    }
}
