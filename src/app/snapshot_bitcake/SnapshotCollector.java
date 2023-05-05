package app.snapshot_bitcake;

import app.Cancellable;
import app.snapshot_bitcake.ab.AbSnapshotResult;

import java.util.Map;

/**
 * Describes a snapshot collector. Made not-so-flexibly for readability.
 *
 * @author bmilojkovic
 */
public interface SnapshotCollector extends Runnable, Cancellable {

    BitcakeManager getBitcakeManager();

    void startCollecting();

    void addDoneMessage(int id);

    void clearCollectedDoneValues();

    Map<String, AbSnapshotResult> getCollectedAbValues();

}