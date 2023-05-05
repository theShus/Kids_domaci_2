package servent.handler.av;

import app.AppConfig;
import app.CausalBroadcastShared;
import app.snapshot_bitcake.SnapshotCollector;
import servent.handler.MessageHandler;
import servent.message.Message;
import servent.message.av.AvDoneMessage;
import servent.message.util.MessageUtil;

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

        for (Integer neighbor : AppConfig.myServentInfo.getNeighbors()) {
            CausalBroadcastShared.getChannel.put(neighbor, 0);
            CausalBroadcastShared.giveChannel.put(neighbor, 0);
        }

        CausalBroadcastShared.tokenVectorClock = clientMessage.getSenderVectorClock();
        CausalBroadcastShared.recordedAmount = currentBitcakeAmount;
        CausalBroadcastShared.initiatorId = clientMessage.getReceiverInfo().getId();
        Message doneMessage = new AvDoneMessage(AppConfig.myServentInfo, clientMessage.getOriginalSenderInfo(), null, clientMessage.getSenderVectorClock(), clientMessage.getOriginalSenderInfo().getId());

        for (Integer neighbor : AppConfig.myServentInfo.getNeighbors()) {
            doneMessage = doneMessage.changeReceiver(neighbor);
            MessageUtil.sendMessage(doneMessage);
        }
//        CausalBroadcastShared.incrementClock(AppConfig.myServentInfo.getId());
        CausalBroadcastShared.causalClockIncrement(doneMessage);
    }

}
