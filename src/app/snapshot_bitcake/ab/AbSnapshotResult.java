package app.snapshot_bitcake.ab;

import servent.message.Message;

import java.io.Serializable;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class AbSnapshotResult implements Serializable {

    private final int serventId;
    private final int bitCakeAmount;
    private final List<Message> sentTransactions;
    private final List<Message> receivedTransactions;

    public AbSnapshotResult(int serventId, int bitCakeAmount, List<Message> sentTransactions, List<Message> receivedTransactions) {
        this.serventId = serventId;
        this.bitCakeAmount = bitCakeAmount;
        this.sentTransactions = new CopyOnWriteArrayList<>(sentTransactions);
        ;
        this.receivedTransactions = new CopyOnWriteArrayList<>(receivedTransactions);
        ;
    }

    public int getServentId() {
        return serventId;
    }

    public int getBitCakeAmount() {
        return bitCakeAmount;
    }

    public List<Message> getSentTransactions() {
        return sentTransactions;
    }

    public List<Message> getReceivedTransactions() {
        return receivedTransactions;
    }
}
