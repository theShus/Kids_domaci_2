package cli.command;

import app.AppConfig;
import app.CausalBroadcastShared;
import app.ServentInfo;
import app.snapshot_bitcake.SnapshotCollector;
import app.snapshot_bitcake.ab.AbBitCakeManager;
import app.snapshot_bitcake.av.AvBitCakeManager;
import servent.message.Message;
import servent.message.TransactionMessage;
import servent.message.util.MessageUtil;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TransactionBurstCommand implements CLICommand {

    private static final int TRANSACTION_COUNT = 5;
    private static final int BURST_WORKERS = 5;
    private static final int MAX_TRANSFER_AMOUNT = 5;
    private final SnapshotCollector snapshotCollector;
    private final Object transactionSync = new Object();

    public TransactionBurstCommand(SnapshotCollector snapshotCollector) {
        this.snapshotCollector = snapshotCollector;
    }

    private class TransactionBurstWorker implements Runnable {

        @Override
        public void run() {
            for (int i = 0; i < TRANSACTION_COUNT; i++) {
                ServentInfo receiverInfo = AppConfig.getInfoById((int) (Math.random() * AppConfig.getServentCount()));

                // Check if receiverInfo is myServentInfo, if so find another receiverInfo because we can't send a transaction to ourselves
                while (receiverInfo.getId() == AppConfig.myServentInfo.getId()) {
                    receiverInfo = AppConfig.getInfoById((int) (Math.random() * AppConfig.getServentCount()));
                }

                int amount = 1 + (int) (Math.random() * MAX_TRANSFER_AMOUNT);

                Message transactionMessage;
                synchronized (transactionSync) {
                    Map<Integer, Integer> vectorClock = new ConcurrentHashMap<>(CausalBroadcastShared.getVectorClock());
                    transactionMessage = new TransactionMessage(AppConfig.myServentInfo, receiverInfo, null, vectorClock, amount, snapshotCollector.getBitcakeManager());


                    System.out.println("PRE CAKE MANAGER PROVERE");
                    if (snapshotCollector.getBitcakeManager() instanceof AbBitCakeManager) {
                        CausalBroadcastShared.addSentTransaction(transactionMessage);
                    } else if (snapshotCollector.getBitcakeManager() instanceof AvBitCakeManager) {
                        System.out.println("UNUTAR PROVERE");
                        System.out.println("snimamo give - " + amount);
                        CausalBroadcastShared.recordGiveTransaction(transactionMessage.getSenderVectorClock(), receiverInfo.getId(), amount);
                    }

                    // reduce our bitcake count then send the message
                    transactionMessage.sendEffect();
                    CausalBroadcastShared.causalClockIncrement(transactionMessage);
                }

                for (int neighbor : AppConfig.myServentInfo.getNeighbors()) {
                    //Same message, different receiver, and add us to the route table.
                    MessageUtil.sendMessage(transactionMessage.changeReceiver(neighbor).makeMeASender());
                }

            }
        }
    }

    @Override
    public String commandName() {
        return "transaction_burst";
    }

    @Override
    public void execute(String args) {
        for (int i = 0; i < BURST_WORKERS; i++) {
            Thread t = new Thread(new TransactionBurstWorker());
            t.start();
        }
    }

}
