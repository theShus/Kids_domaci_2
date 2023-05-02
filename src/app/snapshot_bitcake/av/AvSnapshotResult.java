package app.snapshot_bitcake.av;

import java.io.Serializable;
import java.util.Map;

public class AvSnapshotResult implements Serializable {//todo proveri

    private final int serventId;
    private final int recordedAmount;
    private final Map<Integer, Integer> giveHistory;
    private final Map<Integer, Integer> getHistory;
    public AvSnapshotResult(int serventId, int recordedAmount, Map<Integer, Integer> giveHistory, Map<Integer, Integer> getHistory) {
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
