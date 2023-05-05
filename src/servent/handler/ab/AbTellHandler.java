package servent.handler.ab;

import app.AppConfig;
import app.snapshot_bitcake.SnapshotCollector;
import app.snapshot_bitcake.ab.AbSnapshotResult;
import servent.handler.MessageHandler;
import servent.message.Message;
import servent.message.MessageType;
import servent.message.ab.AbTellMessage;

public class AbTellHandler implements MessageHandler {

    private final Message clientMessage;
    private final SnapshotCollector snapshotCollector;

    public AbTellHandler(Message clientMessage, SnapshotCollector snapshotCollector) {
        this.clientMessage = clientMessage;
        this.snapshotCollector = snapshotCollector;
    }


    @Override
    public void run() {
        try {
            if (clientMessage.getMessageType() == MessageType.AB_TELL) {
                int neighborAmount = Integer.parseInt(clientMessage.getMessageText());
                AbTellMessage tellAmountMessage = (AbTellMessage) clientMessage;

                AbSnapshotResult abSnapshotResult = new AbSnapshotResult(
                        clientMessage.getOriginalSenderInfo().getId(),
                        neighborAmount,
                        tellAmountMessage.getSentTransactions(),
                        tellAmountMessage.getReceivedTransactions());

                snapshotCollector.getCollectedAbValues().put("node " + clientMessage.getOriginalSenderInfo().getId(), abSnapshotResult);
            } else {
                AppConfig.timestampedErrorPrint("Tell amount handler got: " + clientMessage);
            }
        } catch (Exception e) {
            AppConfig.timestampedErrorPrint(e.getMessage());
        }
    }

}
