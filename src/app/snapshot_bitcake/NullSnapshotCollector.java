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
    public boolean isCollecting() {
        return false;
    }

    @Override
    public void test(String key, AbSnapshotResult abSnapshotResult) {

    }

    @Override
    public Map<String, AbSnapshotResult> getCollectedAbValues() {
        return null;
    }

}
