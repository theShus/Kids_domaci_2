package servent.handler.av;

import app.AppConfig;
import app.CausalBroadcastShared;
import app.ServentInfo;
import app.snapshot_bitcake.SnapshotCollector;
import servent.handler.MessageHandler;
import servent.message.BasicMessage;
import servent.message.Message;
import servent.message.MessageType;
import servent.message.av.AvDoneMessage;
import servent.message.util.MessageUtil;

import java.io.Serial;
import java.sql.SQLOutput;
import java.util.Map;

public class AvAskTokenHandler implements MessageHandler {

    private final Message clientMessage;
    private final SnapshotCollector snapshotCollector;
    private final Integer currentBitcakeAmount;

    public AvAskTokenHandler(Message clientMessage, Integer currentBitcakeAmount, SnapshotCollector snapshotCollector) {
        this.clientMessage = clientMessage;
        this.snapshotCollector = snapshotCollector;
        this.currentBitcakeAmount = currentBitcakeAmount;
    }

    @Override
    public void run() {
        System.out.println("### USAO U TOKEN HANDLER");

        for (Integer neighbor : AppConfig.myServentInfo.getNeighbors()) {
            CausalBroadcastShared.getChannel.put(neighbor, 0);
            CausalBroadcastShared.giveChannel.put(neighbor, 0);
        }

        CausalBroadcastShared.tokenVectorClock = clientMessage.getSenderVectorClock();
        CausalBroadcastShared.recordedAmount = currentBitcakeAmount;
        CausalBroadcastShared.initiatorId = clientMessage.getReceiverInfo().getId();
        Message doneMessage = new AvDoneMessage(AppConfig.myServentInfo, null, null, clientMessage.getSenderVectorClock(), clientMessage.getOriginalSenderInfo().getId());

        for (Integer neighbor : AppConfig.myServentInfo.getNeighbors()) {
            doneMessage = doneMessage.changeReceiver(neighbor);
            System.out.println("DONE SALJEMO n:" + neighbor + " dm:" + doneMessage);
            MessageUtil.sendMessage(doneMessage);
        }
//        CausalBroadcastShared.incrementClock(AppConfig.myServentInfo.getId());
        CausalBroadcastShared.causalClockIncrement(doneMessage);
    }

}
