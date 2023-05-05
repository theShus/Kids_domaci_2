package servent.handler;

import app.AppConfig;
import app.CausalBroadcastShared;
import app.snapshot_bitcake.BitcakeManager;
import app.snapshot_bitcake.ab.AbBitCakeManager;
import app.snapshot_bitcake.av.AvBitCakeManager;
import servent.message.Message;
import servent.message.MessageType;

public class TransactionHandler implements MessageHandler {

    private final Message clientMessage;
    private final BitcakeManager bitcakeManager;

    public TransactionHandler(Message clientMessage, BitcakeManager bitcakeManager) {
        this.clientMessage = clientMessage;
        this.bitcakeManager = bitcakeManager;
    }

    @Override
    public void run() {
        if (clientMessage.getMessageType() == MessageType.TRANSACTION) {
            String amountString = clientMessage.getMessageText();

            int amountNumber = 0;
            try {
                amountNumber = Integer.parseInt(amountString);
            } catch (NumberFormatException e) {
                AppConfig.timestampedErrorPrint("Couldn't parse amount: " + amountString);
                return;
            }
            bitcakeManager.addSomeBitcakes(amountNumber);

            if (bitcakeManager instanceof AbBitCakeManager) {
                CausalBroadcastShared.addReceivedTransaction(clientMessage);
            } else if (bitcakeManager instanceof AvBitCakeManager) {
                CausalBroadcastShared.recordGetTransaction(clientMessage.getSenderVectorClock(), clientMessage.getOriginalSenderInfo().getId(), amountNumber);

            }

        } else {
            AppConfig.timestampedErrorPrint("Transaction handler got: " + clientMessage);
        }
    }

}
