package app.snapshot_bitcake;

import app.AppConfig;
import app.snapshot_bitcake.ab.AbBitCakeManager;
import app.snapshot_bitcake.ab.AbSnapshotResult;
import app.snapshot_bitcake.av.AvBitCakeManager;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
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

    private final Map<String, Integer> collectedNaiveValues = new ConcurrentHashMap<>();
    private final Map<Integer, AbSnapshotResult> collectedABValues = new ConcurrentHashMap<>();
    private final List<Integer> collectedDoneMessages = new CopyOnWriteArrayList<>();

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
    public void addAbSnapshotInfo(int id, AbSnapshotResult abSnapshotResult) {

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

            //1 send asks
            //todo dodaj casove za AB i AV snapshotove
            if (Objects.requireNonNull(snapshotType) == SnapshotType.NONE) {//Shouldn't be able to come here. See constructor.
            }

            //2 wait for responses or finish
            boolean waiting = true;
            while (waiting) {
                switch (snapshotType) {

                    case AB -> {
                    } //todo AB snapshot collector
                    case AV -> {
                    } //todo AV snapshot collector
                    case NONE -> System.out.println("CRITICAL ERROR");

                    //Shouldn't be able to come here. See constructor.
                }

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                if (!working) {
                    return;
                }
            }

            //print
            int sum;
            switch (snapshotType) {
                case NAIVE:
                    sum = 0;
                    for (Entry<String, Integer> itemAmount : collectedNaiveValues.entrySet()) {
                        sum += itemAmount.getValue();
                        AppConfig.timestampedStandardPrint(
                                "Info for " + itemAmount.getKey() + " = " + itemAmount.getValue() + " bitcake");
                    }

                    AppConfig.timestampedStandardPrint("System bitcake count: " + sum);

                    collectedNaiveValues.clear(); //reset for next invocation
                    break;


                case NONE:
                    //Shouldn't be able to come here. See constructor.
                    break;
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

}
