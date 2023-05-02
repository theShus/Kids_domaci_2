package app.snapshot_bitcake.ab;

import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class AbSnapshotResult implements Serializable {

    private final int serventId;
    private final int recordedAmount;
    private final Map<Integer, Integer> giveHistory;
    private final Map<Integer, Integer> getHistory;
    public AbSnapshotResult(int serventId, int recordedAmount, Map<Integer, Integer> giveHistory, Map<Integer, Integer> getHistory) {
        this.serventId = serventId;
        this.recordedAmount = recordedAmount;
        this.giveHistory = giveHistory;
        this.getHistory = getHistory;
    }

    public int getServentId() {
        return serventId;
    }
    public int getRecordedAmount() {
        return recordedAmount;
    }
    public Map<Integer, Integer> getGiveHistory() {
        return giveHistory;
    }
    public Map<Integer, Integer> getGetHistory() {
        return getHistory;
    }
}
