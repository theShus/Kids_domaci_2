package app;

import app.snapshot_bitcake.SnapshotCollector;
import servent.handler.TransactionHandler;
import servent.message.BasicMessage;
import servent.message.Message;
import servent.message.MessageType;

import java.util.*;
import java.util.concurrent.*;
import java.util.function.BiFunction;

/**
 * This class contains shared data for the Causal Broadcast implementation:
 * <ul>
 * <li> Vector clock for current instance
 * <li> Commited message list
 * <li> Pending queue
 * </ul>
 * As well as operations for working with all of the above.
 *
 * @author bmilojkovic
 */
public class CausalBroadcastShared {
    private static final Map<Integer, Integer> vectorClock = new ConcurrentHashMap<>();
    private static final List<Message> commitedCausalMessageList = new CopyOnWriteArrayList<>();
    private static final Queue<Message> pendingMessages = new ConcurrentLinkedQueue<>();
    private static final Object pendingMessagesLock = new Object();
    private static final ExecutorService committedMessagesThreadPool = Executors.newWorkStealingPool();
    private static SnapshotCollector snapshotCollector;


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

    public static List<Message> getCommitedCausalMessages() {
        List<Message> toReturn = new CopyOnWriteArrayList<>(commitedCausalMessageList);

        return toReturn;
    }

    public static void commitCausalMessage(Message newMessage) {
        AppConfig.timestampedStandardPrint("Committing " + newMessage);
        commitedCausalMessageList.add(newMessage);
        incrementClock(newMessage.getOriginalSenderInfo().getId());

        checkPendingMessages();
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
                        commitedCausalMessageList.add(pendingMessage);
                        incrementClock(pendingMessage.getOriginalSenderInfo().getId());

                        boolean didPut;//todo stavi za ostale

                        //todo dodaj primanje osalih vrsta poruka
                        if (Objects.requireNonNull(pendingMessage.getMessageType()) == MessageType.TRANSACTION) {
                            if (basicMessage.getOriginalReceiverInfo().getId() == AppConfig.myServentInfo.getId())
                                committedMessagesThreadPool.submit(new TransactionHandler(basicMessage, snapshotCollector.getBitcakeManager()));
                        }
                        iterator.remove();
                        break;
                    }
                }
            }
        }
    }

}
