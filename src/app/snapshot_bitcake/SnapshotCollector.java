package app.snapshot_bitcake;

import app.Cancellable;
import app.snapshot_bitcake.ab.AbSnapshotResult;

/**
 * Describes a snapshot collector. Made not-so-flexibly for readability.
 *
 * @author bmilojkovic
 */
public interface SnapshotCollector extends Runnable, Cancellable {

    BitcakeManager getBitcakeManager();

    void addAbSnapshotInfo(int id, AbSnapshotResult abSnapshotResult);

    void startCollecting();

//	void addDoneMessage(int id);

//	void setTerminateNotArrived();

    boolean isCollecting();
}