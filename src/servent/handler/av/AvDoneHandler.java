package servent.handler.av;

import app.AppConfig;
import app.snapshot_bitcake.SnapshotCollector;
import servent.handler.MessageHandler;
import servent.message.Message;
import servent.message.MessageType;

public class AvDoneHandler implements MessageHandler {

    private final Message clientMessage;
    private final SnapshotCollector snapshotCollector;

    public AvDoneHandler(Message clientMessage, SnapshotCollector snapshotCollector) {
        this.clientMessage = clientMessage;
        this.snapshotCollector = snapshotCollector;
    }

    @Override
    public void run() {
        System.out.println("### USAO U DONE HANDLER");

        if (clientMessage.getMessageType() == MessageType.AV_DONE) {
            snapshotCollector.addDoneMessage(clientMessage.getReceiverInfo().getId());
            System.out.println("dodao je na listu done poruka ovog " + clientMessage.getReceiverInfo().getId());

        } else {
            AppConfig.timestampedErrorPrint("Tell amount handler got: " + clientMessage);
        }
    }

}
