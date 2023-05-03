package servent.handler;

import app.AppConfig;
import app.CausalBroadcastShared;
import app.ServentInfo;
import app.snapshot_bitcake.SnapshotCollector;
import servent.message.Message;
import servent.message.util.MessageUtil;

import java.util.Set;

/**
 * Handles the CAUSAL_BROADCAST message. Fairly simple, as we assume that we are
 * in a clique. We add the message to a pending queue, and let the check on the queue
 * take care of the rest.
 *
 * @author bmilojkovic
 */
public class CausalBroadcastHandler implements MessageHandler {

    private final Message clientMessage;
    private final Set<Message> receivedBroadcasts;
    private final SnapshotCollector snapshotCollector;
    private final Object pendingMessagesSync;


    public CausalBroadcastHandler(Message clientMessage, SnapshotCollector snapshotCollector, Set<Message> receivedBroadcasts, Object pendingMessagesSync) {
        this.clientMessage = clientMessage;
        this.snapshotCollector = snapshotCollector;
        this.receivedBroadcasts = receivedBroadcasts;
        this.pendingMessagesSync = pendingMessagesSync;
    }

    @Override
    public void run() {
        ServentInfo senderInfo = clientMessage.getOriginalSenderInfo();

//        ServentInfo lastSenderInfo = clientMessage.getRoute().size() == 0 ? clientMessage.getOriginalSenderInfo() : clientMessage.getRoute().get(clientMessage.getRoute().size() - 1);
//        String text = String.format("Got %s from %s broadcast by %s", clientMessage.getMessageText(), lastSenderInfo, senderInfo);
//        AppConfig.timestampedStandardPrint(text);

        if (senderInfo.getId() == AppConfig.myServentInfo.getId()) {
            //We are the sender :o someone bounced this back to us. /ignore
            AppConfig.timestampedStandardPrint("Got own message back. No rebroadcast.");
        } else {
            synchronized (pendingMessagesSync) {
                //Try to put in the set. Thread safe add ftw.
                boolean didPut = receivedBroadcasts.add(clientMessage);

                if (didPut) {
                    CausalBroadcastShared.addPendingMessage(clientMessage);//dodamo poruku na pending listu i uradimo check (kao u v5)
                    CausalBroadcastShared.checkPendingMessages();

                    if (!AppConfig.IS_CLIQUE) {//radimo rebrodcast samo ako nije svaki sa svakim
                        //New message for us. Rebroadcast it.
                        AppConfig.timestampedStandardPrint("Rebroadcasting... " + receivedBroadcasts.size());

                        for (Integer neighbor : AppConfig.myServentInfo.getNeighbors()) {
                            //Same message, different receiver, and add us to the route table.
                            MessageUtil.sendMessage(clientMessage.changeReceiver(neighbor).makeMeASender());
                        }
                    }
                } else {
                    //We already got this from somewhere else. /ignore
                    AppConfig.timestampedStandardPrint("Already had this. No rebroadcast.");
                }
            }
        }
    }

}
