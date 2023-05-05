package app;

import app.snapshot_bitcake.SnapshotCollector;
import servent.handler.TransactionHandler;
import servent.handler.ab.AbAskTokenHandler;
import servent.handler.ab.AbTellHandler;
import servent.handler.av.AvAskTokenHandler;
import servent.handler.av.AvDoneHandler;
import servent.handler.av.AvTerminateHandler;
import servent.message.BasicMessage;
import servent.message.Message;

import java.util.*;
import java.util.concurrent.*;
import java.util.function.BiFunction;


public class CausalBroadcastShared {
    private static final Map<Integer, Integer> vectorClock = new ConcurrentHashMap<>();
    private static final Queue<Message> pendingMessages = new ConcurrentLinkedQueue<>();
    private static final Object pendingMessagesLock = new Object();
    private static final ExecutorService committedMessagesThreadPool = Executors.newWorkStealingPool();
    private static SnapshotCollector snapshotCollector;

    //Cuvane transakcije za AB snapshot
    private static final List<Message> sentTransactions = new CopyOnWriteArrayList<>();
    private static final List<Message> receivedTransactions = new CopyOnWriteArrayList<>();
    private static final Set<Message> receivedAbAsk = Collections.newSetFromMap(new ConcurrentHashMap<>());

    //Cuvane transakcije za AV snapshot
    public static int recordedAmount;
    public static int initiatorId;
    public static Map<Integer, Integer> tokenVectorClock = null;
    public static final Map<Integer, Integer> getChannel = new ConcurrentHashMap<>();
    public static final Map<Integer, Integer> giveChannel = new ConcurrentHashMap<>();

    private static boolean otherClockGreater(Map<Integer, Integer> clock1, Map<Integer, Integer> clock2) {
        if (clock1.size() != clock2.size()) {
            throw new IllegalArgumentException("Clocks are not same size how why");
        }

        for (int i = 0; i < clock1.size(); i++) {
            if (clock2.get(i) > clock1.get(i)) {
                return true;
            }
        }

        return false;
    }

    public static void causalClockIncrement(Message newMessage) {
        AppConfig.timestampedStandardPrint("Committing # " + newMessage);
        incrementClock(newMessage.getOriginalSenderInfo().getId());
        checkPendingMessages();
    }


    public static void recordGetTransaction(Map<Integer, Integer> senderVectorClock, int neighbor, int amount) {
        if (tokenVectorClock != null) {
            if (senderVectorClock.get(initiatorId) <= tokenVectorClock.get(initiatorId)) {
                int oldAmount;
                if (getChannel.get(neighbor) == null) oldAmount = 0;
                else oldAmount = getChannel.get(neighbor);
                getChannel.put(neighbor, oldAmount + amount);
            }
        }
    }

    public static void recordGiveTransaction(Map<Integer, Integer> senderVectorClock, int neighbor, int amount) {
        if (tokenVectorClock != null) {
            if (senderVectorClock.get(initiatorId) <= tokenVectorClock.get(initiatorId)) {
                int oldAmount;
                if (getChannel.get(neighbor) == null) oldAmount = 0;
                else oldAmount = getChannel.get(neighbor);
                getChannel.put(neighbor, oldAmount + amount);
            }
        }
    }

    public static void checkPendingMessages() {
        boolean gotWork = true;

        while (gotWork) {
            gotWork = false;

            synchronized (pendingMessagesLock) {
                Iterator<Message> iterator = pendingMessages.iterator();
                Map<Integer, Integer> myVectorClock = getVectorClock();

                while (iterator.hasNext()) {
                    Message pendingMessage = iterator.next();
                    BasicMessage basicMessage = (BasicMessage) pendingMessage;

                    if (!otherClockGreater(myVectorClock, basicMessage.getSenderVectorClock())) {//ako je desni (sender) veci od mene (levi) imamo message
                        gotWork = true;

                        AppConfig.timestampedStandardPrint("Committing " + pendingMessage);
                        incrementClock(pendingMessage.getOriginalSenderInfo().getId());

                        boolean didPut;


                        switch (basicMessage.getMessageType()) {
                            case TRANSACTION -> {
                                if (basicMessage.getOriginalReceiverInfo().getId() == AppConfig.myServentInfo.getId())
                                    committedMessagesThreadPool.submit(new TransactionHandler(basicMessage, snapshotCollector.getBitcakeManager()));
                            }
                            case AB_ASK -> {
                                didPut = receivedAbAsk.add(basicMessage);
                                if (didPut)
                                    committedMessagesThreadPool.submit(new AbAskTokenHandler(basicMessage, snapshotCollector));
                            }
                            case AB_TELL -> {
                                if (basicMessage.getOriginalReceiverInfo().getId() == AppConfig.myServentInfo.getId())
                                    committedMessagesThreadPool.submit(new AbTellHandler(basicMessage, snapshotCollector));
                            }
                            case AV_ASK -> {
//                                if (basicMessage.getOriginalReceiverInfo().getId() == AppConfig.myServentInfo.getId())
                                committedMessagesThreadPool.submit(new AvAskTokenHandler(basicMessage, snapshotCollector.getBitcakeManager().getCurrentBitcakeAmount(), snapshotCollector));
                            }
                            case AV_DONE -> {
                                if (basicMessage.getOriginalReceiverInfo().getId() == AppConfig.myServentInfo.getId()) {
                                    committedMessagesThreadPool.submit(new AvDoneHandler(basicMessage, snapshotCollector));
                                }
                            }
                            case AV_TERMINATE -> {
//                                if (basicMessage.getOriginalReceiverInfo().getId() == AppConfig.myServentInfo.getId())
                                committedMessagesThreadPool.submit(new AvTerminateHandler(basicMessage, snapshotCollector));
                            }
                        }

                        iterator.remove();
                        break;
                    }
                }
            }
        }
    }


    public static void initializeVectorClock(int serventCount) {
        for (int i = 0; i < serventCount; i++) {
            vectorClock.put(i, 0);
        }
    }

    public static void incrementClock(int serventId) {
        vectorClock.computeIfPresent(serventId, new BiFunction<Integer, Integer, Integer>() {

            @Override
            public Integer apply(Integer key, Integer oldValue) {
                return oldValue + 1;
            }
        });
    }

    public static Map<Integer, Integer> getVectorClock() {
        return vectorClock;
    }

    public static SnapshotCollector getSnapshotCollector() {
        return snapshotCollector;
    }

    public static void setSnapshotCollector(SnapshotCollector snapshotCollector) {
        CausalBroadcastShared.snapshotCollector = snapshotCollector;
    }

    public static void addPendingMessage(Message msg) {
        pendingMessages.add(msg);
    }

    public static void addReceivedTransaction(Message receivedTransaction) {
        receivedTransactions.add(receivedTransaction);
    }

    public static List<Message> getReceivedTransactions() {
        return receivedTransactions;
    }

    public static void addSentTransaction(Message sendTransaction) {
        sentTransactions.add(sendTransaction);
    }

    public static List<Message> getSentTransactions() {
        return sentTransactions;
    }

}
