package servent.message.av;

import app.ServentInfo;
import servent.message.BasicMessage;
import servent.message.MessageType;

import java.io.Serial;
import java.util.Map;

public class AvDoneMessage extends BasicMessage {

    @Serial
    private static final long serialVersionUID = 604964775378108409L;
    public int initiatorId;

    public AvDoneMessage(ServentInfo sender, ServentInfo receiver, ServentInfo neighbor, Map<Integer, Integer> senderVectorClock, int initiatorId) {
        super(MessageType.AV_DONE, sender, receiver, neighbor, senderVectorClock);
        this.initiatorId = initiatorId;
//        System.out.println(this.initiatorId);
//        System.out.println(this.hashCode());
    }


    public int getInitiatorId() {
        return initiatorId;
    }

}
