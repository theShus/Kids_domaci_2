package app;

import app.snapshot_bitcake.SnapshotCollector;
import servent.handler.TransactionHandler;
import servent.message.Message;
import servent.message.TransactionMessage;

import java.util.Iterator;
import java.util.Map;
import java.util.Queue;
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
    private static final Queue<Message> pendingMessages = new ConcurrentLinkedQueue<>();
    private static final Object pendingMessagesLock = new Object();
    private static final ExecutorService committedMessagesPoll = Executors.newCachedThreadPool();


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

    public static void checkPendingMessages(SnapshotCollector snapshotCollector) {
        boolean gotWork = true;

        while (gotWork) {
            gotWork = false;

            synchronized (pendingMessagesLock) {
                Iterator<Message> iterator = pendingMessages.iterator();
                Map<Integer, Integer> myVectorClock = getVectorClock();

                while (iterator.hasNext()) {
                    Message pendingMessage = iterator.next();

                    if (!otherClockGreater(myVectorClock, pendingMessage.getSenderVectorClock())) {//ako je desni (sender) veci od mene (levi) imamo message
                        gotWork = true;


                        //todo dodaj primanje osalih vrsta poruka
                        switch (pendingMessage.getMessageType()) {
                            case TRANSACTION -> {
                                incrementClock(pendingMessage.getOriginalSenderInfo().getId());
//                                if (((TransactionMessage) pendingMessage).getOriginallyIntendedReceiver() == AppConfig.myServentInfo.getId())
                                if (pendingMessage.getOriginalReceiverInfo().getId() == AppConfig.myServentInfo.getId())
                                    committedMessagesPoll.submit(new TransactionHandler(pendingMessage, snapshotCollector.getBitcakeManager()));
                            }

                        }

                        iterator.remove();
                        break;
                    }
                }
            }
        }
    }

}
