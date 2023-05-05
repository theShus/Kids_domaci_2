package servent.message.av;

import app.ServentInfo;
import servent.message.BasicMessage;
import servent.message.MessageType;
import java.io.Serial;
import java.util.Map;

public class AvDoneMessage extends BasicMessage {

    @Serial
    private static final long serialVersionUID = 604964775378108409L;
    private int initiatorId;

    public AvDoneMessage(ServentInfo sender, ServentInfo receiver, ServentInfo neighbor, Map<Integer, Integer> senderVectorClock, int initiatorId) {
        super(MessageType.AV_DONE, sender, receiver, neighbor, senderVectorClock);
        this.initiatorId = initiatorId;
        System.out.println("INITIATOR ID");
        System.out.println(this.initiatorId);
    }

    public int getInitiatorId() {
        return initiatorId;
    }

    //    protected AvDoneMessage(MessageType type, ServentInfo originalSenderInfo, ServentInfo originalReceiverInfo, ServentInfo receiverInfo,
//                                Map<Integer, Integer> senderVectorClock, List<ServentInfo> routeList, String messageText, int messageId) {
//        super(type, originalSenderInfo, originalReceiverInfo, receiverInfo, senderVectorClock, routeList, messageText, messageId);
//    }
//
//    @Override
//    public Message changeReceiver(Integer newReceiverId) {
//        System.out.println("pushi ga picko");
//        if (AppConfig.myServentInfo.getNeighbors().contains(newReceiverId)) {
//            System.out.println("usli smo u if, yay postojimo");
//            ServentInfo newReceiverInfo = AppConfig.getInfoById(newReceiverId);
//
//            System.out.println("newRecieverInfo");
//            System.out.println(newReceiverInfo);
//
//            Message toReturn = new AvDoneMessage(getMessageType(),getOriginalSenderInfo(), newReceiverInfo,
//                    newReceiverInfo, getSenderVectorClock(), getRoute(), getMessageText(), getMessageId());
//
//            System.out.println("toReturn");
//            System.out.println(toReturn);
//
//            return toReturn;
//        } else {
//            AppConfig.timestampedErrorPrint("Trying to make a message for " + newReceiverId + " who is not a neighbor.");
//            return null;
//        }
//    }

}
