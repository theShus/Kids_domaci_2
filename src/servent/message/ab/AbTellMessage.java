package servent.message.ab;

import app.AppConfig;
import app.ServentInfo;
import servent.message.BasicMessage;
import servent.message.Message;
import servent.message.MessageType;

import java.io.Serial;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

public class AbTellMessage extends BasicMessage {

    @Serial
    private static final long serialVersionUID = -3498920491848174004L;
    private final List<Message> sentTransactions;
    private final List<Message> receivedTransactions;

    public AbTellMessage(ServentInfo sender, ServentInfo receiver, ServentInfo neighbor, Map<Integer, Integer> senderVectorClock, int amount, List<Message> sentTransactions, List<Message> receivedTransactions) {
        super(MessageType.AB_TELL, sender, receiver, neighbor, senderVectorClock, String.valueOf(amount));

        this.sentTransactions = new CopyOnWriteArrayList<>(sentTransactions);
        this.receivedTransactions = new CopyOnWriteArrayList<>(receivedTransactions);
    }

    protected AbTellMessage(MessageType type, ServentInfo originalSenderInfo, ServentInfo originalReceiverInfo, ServentInfo receiverInfo,
                            Map<Integer, Integer> senderVectorClock, List<ServentInfo> routeList, String messageText,
                            int messageId, List<Message> sentTransactions, List<Message> receivedTransactions) {

        super(type, originalSenderInfo, originalReceiverInfo, receiverInfo, senderVectorClock, routeList, messageText, messageId);

        this.sentTransactions = sentTransactions;
        this.receivedTransactions = receivedTransactions;
    }

    public List<Message> getSentTransactions() {
        return sentTransactions;
    }

    public List<Message> getReceivedTransactions() {
        return receivedTransactions;
    }

    @Override
    public Message makeMeASender() {
        ServentInfo newRouteItem = AppConfig.myServentInfo;
        List<ServentInfo> newRouteList = new ArrayList<>(getRoute());
        newRouteList.add(newRouteItem);

        return new AbTellMessage(getMessageType(), getOriginalSenderInfo(), getOriginalReceiverInfo(), getReceiverInfo(), getSenderVectorClock(), newRouteList, getMessageText(), getMessageId(), getSentTransactions(), getReceivedTransactions());
    }

    @Override
    public Message changeReceiver(Integer newReceiverId) {
        if (AppConfig.myServentInfo.getNeighbors().contains(newReceiverId)) {
            ServentInfo newReceiverInfo = AppConfig.getInfoById(newReceiverId);

            return new AbTellMessage(getMessageType(), getOriginalSenderInfo(), getOriginalReceiverInfo(), newReceiverInfo, getSenderVectorClock(), getRoute(), getMessageText(), getMessageId(), getSentTransactions(), getReceivedTransactions());
        } else {
            AppConfig.timestampedErrorPrint("Trying to make a message for " + newReceiverId + " who is not a neighbor.");
            return null;
        }
    }


}
