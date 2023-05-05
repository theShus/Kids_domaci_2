package servent.message;

public enum MessageType {
    POISON, TRANSACTION, CAUSAL_BROADCAST,
    AB_ASK, AB_TELL,
    AV_ASK, AV_DONE, AV_TERMINATE
}
