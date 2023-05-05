package app.snapshot_bitcake;

import app.snapshot_bitcake.ab.AbSnapshotResult;

import java.util.Map;

/**
 * This class is used if the user hasn't specified a snapshot type in config.
 *
 * @author bmilojkovic
 */
public class NullSnapshotCollector implements SnapshotCollector {

    @Override
    public void run() {
    }

    @Override
    public void stop() {
    }

    @Override
    public BitcakeManager getBitcakeManager() {
        return null;
    }


    @Override
    public void startCollecting() {
    }

    @Override
    public void addDoneMessage(int id) {

    }

    @Override
    public void clearCollectedDoneValues() {

    }

    @Override
    public Map<String, AbSnapshotResult> getCollectedAbValues() {
        return null;
    }

}
