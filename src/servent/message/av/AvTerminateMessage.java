package servent.message.av;

import app.AppConfig;
import app.ServentInfo;
import app.snapshot_bitcake.SnapshotCollector;
import servent.message.BasicMessage;
import servent.message.Message;
import servent.message.MessageType;

import java.io.Serial;
import java.util.List;
import java.util.Map;

public class AvTerminateMessage extends BasicMessage {

    @Serial
    private static final long serialVersionUID = 2787944576523041787L;

    public AvTerminateMessage(ServentInfo sender, ServentInfo receiver, ServentInfo neighbor, Map<Integer, Integer> senderVectorClock) {
        super(MessageType.AV_TERMINATE, sender, receiver, neighbor, senderVectorClock);
    }

}
