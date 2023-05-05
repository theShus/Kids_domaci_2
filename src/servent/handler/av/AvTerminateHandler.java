package servent.handler.av;

import app.AppConfig;
import app.CausalBroadcastShared;
import app.snapshot_bitcake.SnapshotCollector;
import servent.handler.MessageHandler;
import servent.message.Message;

import java.util.Map;

public class AvTerminateHandler implements MessageHandler {

    private final Message clientMessage;
    private final SnapshotCollector snapshotCollector;

    public AvTerminateHandler(Message clientMessage, SnapshotCollector snapshotCollector) {
        this.clientMessage = clientMessage;
        this.snapshotCollector = snapshotCollector;
    }

    @Override
    public void run() {
        System.out.println("\n----------TERMINATE PRINT----------\n");
        CausalBroadcastShared.tokenVectorClock = null;

        int sum = CausalBroadcastShared.recordedAmount;
        AppConfig.timestampedStandardPrint("Recorded bitcake amount: " + CausalBroadcastShared.recordedAmount);

        for (Map.Entry<Integer, Integer> entry : CausalBroadcastShared.getChannel.entrySet()) {
            AppConfig.timestampedStandardPrint("Unreceived bitcake amount: " + entry.getValue() + " from " + entry.getKey());
            sum += entry.getValue();
        }

        for (Map.Entry<Integer, Integer> entry : CausalBroadcastShared.giveChannel.entrySet()) {
            AppConfig.timestampedStandardPrint("Sent bitcake amount: " + entry.getValue() + " from " + entry.getKey());
            sum -= entry.getValue();
        }

        AppConfig.timestampedStandardPrint("Total node bitcake amount: " + sum);

        System.out.println("\n----------TERMINATE PRINT----------\n");

        snapshotCollector.clearCollectedDoneValues();
    }

}
