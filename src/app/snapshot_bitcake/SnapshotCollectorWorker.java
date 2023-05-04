package app.snapshot_bitcake;

import app.AppConfig;
import app.CausalBroadcastShared;
import app.snapshot_bitcake.ab.AbBitCakeManager;
import app.snapshot_bitcake.ab.AbSnapshotResult;
import app.snapshot_bitcake.av.AvBitCakeManager;
import servent.message.Message;
import servent.message.snapshot.AbAskTokenMessage;
import servent.message.util.MessageUtil;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Main snapshot collector class. Has support for Naive, Chandy-Lamport
 * and Lai-Yang snapshot algorithms.
 *
 * @author bmilojkovic
 */
public class SnapshotCollectorWorker implements SnapshotCollector {

    private volatile boolean working = true;
    private final AtomicBoolean collecting = new AtomicBoolean(false);

    private final Map<String, AbSnapshotResult> collectedAbValues = new ConcurrentHashMap<>();

    private final SnapshotType snapshotType;
    private BitcakeManager bitcakeManager;

    public SnapshotCollectorWorker(SnapshotType snapshotType) {
        this.snapshotType = snapshotType;
        switch (snapshotType) {
            case AB -> bitcakeManager = new AbBitCakeManager();
            case AV -> bitcakeManager = new AvBitCakeManager();
            case NONE -> {
                AppConfig.timestampedErrorPrint("Making snapshot collector without specifying type. Exiting...");
                System.exit(0);
            }
        }
    }

    @Override
    public BitcakeManager getBitcakeManager() {
        return bitcakeManager;
    }


    @Override
    public void run() {
        while (working) {

            /*
             * Not collecting yet - just sleep until we start actual work, or finish
             */
            while (!collecting.get()) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

                if (!working) {
                    return;
                }
            }

            /*
             * Collecting is done in three stages:
             * 1. Send messages asking for values
             * 2. Wait for all the responses
             * 3. Print result
             */
            Map<Integer, Integer> vectorClock;
            Message askMessage;

            switch (snapshotType) {
                case AB -> {
                    System.out.println("COLL- USLI SMO U FAZU 1");

                    //napravimo novi ask message
                    vectorClock = new ConcurrentHashMap<>(CausalBroadcastShared.getVectorClock());
                    askMessage = new AbAskTokenMessage(AppConfig.myServentInfo, null, null, vectorClock);

                    //piramo za stanje sve susede
                    for (Integer neighbor : AppConfig.myServentInfo.getNeighbors()) {
                        askMessage = askMessage.changeReceiver(neighbor);
                        MessageUtil.sendMessage(askMessage);
                    }

                    //sacuvamo nase stanje (on onoga koji je posalo ask)
                    AbSnapshotResult abSnapshotResult = new AbSnapshotResult(
                            AppConfig.myServentInfo.getId(),
                            bitcakeManager.getCurrentBitcakeAmount(),
                            CausalBroadcastShared.getSendTransactions(),
                            CausalBroadcastShared.getReceivedTransactions());
                    collectedAbValues.put("node " + AppConfig.myServentInfo.getId(), abSnapshotResult);

                    CausalBroadcastShared.commitCausalMessage(askMessage);//TODO promeni ovo govno
                }
                case AV -> {} //todo av ask message

                case NONE -> System.out.println("Error snapshot type is null");
            }


            //2 wait for responses or finish
            boolean waiting = true;
            while (waiting) {
                switch (snapshotType) {
                    case AB -> {//ako smo sakupili sve tellMessages mozes da printas
                        System.out.println("COLL FAZA 2 - " + collectedAbValues.size() + " == " + AppConfig.getServentCount());
                        if (collectedAbValues.size() == AppConfig.getServentCount()) {
                            System.out.println("COLL- IZASLI SMO IZ FAZE 2");
                            waiting = false;
                        }
                    }
                    case AV -> {} //todo AV snapshot collector
                    case NONE -> System.out.println("Error snapshot type is null");
                    //Shouldn't be able to come here. See constructor.
                }

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (!working) return;
            }

            //print
            int sum = 0;
            switch (snapshotType) {
                case AB -> {
                    System.out.println("COLL- USLI SMO U FAZU 3");

                    for (Entry<String, AbSnapshotResult> abSR : collectedAbValues.entrySet()) {
                        boolean exist = false;
                        int bitCakeAmount = abSR.getValue().getBitCakeAmount();
                        List<Message> sentTransactions = abSR.getValue().getSentTransactions();

                        sum += bitCakeAmount;
                        AppConfig.timestampedStandardPrint("Snapshot for " + abSR.getKey() + " = " + bitCakeAmount + " bitcake");

                        for (Message sentTransaction : sentTransactions) {
                            AbSnapshotResult abSnapshotResult = collectedAbValues.get("node " + sentTransaction.getOriginalReceiverInfo().getId());
                            List<Message> receivedTransactions = abSnapshotResult.getReceivedTransactions();

                            for (Message receivedTransaction : receivedTransactions) {
                                if (sentTransaction.getMessageId() == receivedTransaction.getMessageId() &&
                                        sentTransaction.getOriginalSenderInfo().getId() == receivedTransaction.getOriginalSenderInfo().getId() &&
                                        sentTransaction.getOriginalReceiverInfo().getId() == receivedTransaction.getOriginalReceiverInfo().getId()) {
                                    exist = true;
                                    break;
                                }
                            }

                            if (!exist) {
                                AppConfig.timestampedStandardPrint("Info for unprocessed transaction: " + sentTransaction.getMessageText() + " bitcake");
                                int amountNumber = Integer.parseInt(sentTransaction.getMessageText());
                                sum += amountNumber;
                            }
                        }
                    }
                    AppConfig.timestampedStandardPrint("System bitcake count: " + sum);
                    collectedAbValues.clear();
                }
                case AV -> {}//todo print av
                case NONE -> System.out.println("Error snapshot type is null");
                //Shouldn't be able to come here. See constructor.
            }
            collecting.set(false);
        }

    }

    @Override
    public void startCollecting() {
        boolean oldValue = this.collecting.getAndSet(true);

        if (oldValue) {
            AppConfig.timestampedErrorPrint("Tried to start collecting before finished with previous.");
        }
    }

    @Override
    public void stop() {
        working = false;
    }

    @Override
    public boolean isCollecting() {
        return collecting.get();
    }

    @Override
    public Map<String, AbSnapshotResult> getCollectedAbValues() {
        return collectedAbValues;
    }
}
